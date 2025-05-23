/*
 * Copyright (c) 2006-, IPD Boehm, Universitaet Karlsruhe (TH) / KIT, by Guido Sautter
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Universitaet Karlsruhe (TH) / KIT nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY UNIVERSITAET KARLSRUHE (TH) / KIT AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.uka.ipd.idaho.gamta.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class provides the facilities to run a chunk of code on multiple CPU
 * cores in parallel. This helps doing computationally intensive operations
 * faster on modern multi-core machines. Synchronization of data structures
 * shared between multiple executions of the parallelized code is up to the
 * implementation of the latter.
 * 
 * @author sautter
 */
public class ParallelJobRunner {
	
	//	we don't want to be instantiated
	private ParallelJobRunner() {}
	
	private static boolean runLinear = false;
	private static int maxCoresPerJob = -1;
	private static int jobThreadTraceInterval = -1;
	
	/**
	 * Test if parallel job execution is switched on or off.
	 * @return the linear property
	 */
	public static boolean isLinear() {
		return runLinear;
	}
	
	/**
	 * Switch on or off all parallelization. Setting the 'linear' property to
	 * true forces all parallel operations to actually run linear, independent
	 * of the number of available CPU cores. This is helpful in situations of
	 * running multiple potentially CPU intensive JVMs on a single machine at
	 * the same time. Namely, it prevents each single JVM from occupying all
	 * the available resources for itself. However, this only applies to code
	 * parallelized by means of this class.
	 * @param linear the linear property to set
	 */
	public static void setLinear(boolean linear) {
		runLinear = linear;
	}
	
	/**
	 * Get the maximum number of CPU cores to use on any single job.
	 * @return the maximum number of cores to use
	 */
	public static int getMaxCores() {
		return maxCoresPerJob;
	}
	
	/**
	 * Set the maximum number of CPU cores to use on any single job. This means
	 * of limitation is helpful in situations of running multiple potentially
	 * CPU intensive JVMs on a single machine at the same time. Namely, it
	 * prevents each single JVM from occupying all the available resources for
	 * itself. However, this only applies to code parallelized by means of this
	 * class. Setting the maximum to -1 switches off the limitation; setting it
	 * to any value larger or equal to <code>Runtime.getRuntime().availableProcessors()</code>
	 * effectively also lifts all limitations, as the return value of that
	 * latter method represents the hardware imposed limit.
	 * @param maxCores the maximum number of cores to set
	 */
	public static void setMaxCores(int maxCores) {
		maxCoresPerJob = maxCores;
	}
	
	private static int checkMaxCores(int maxCores) {
		if (runLinear)
			return 1; // nothing to compute here
		if (maxCores < 1)
			maxCores = Integer.MAX_VALUE; // handle incoming 'unlimited'
		if (0 < maxCoresPerJob)
			maxCores = Math.min(maxCores, maxCoresPerJob); // impose global limitation if present
		return Math.min(maxCores, (Runtime.getRuntime().availableProcessors() - 1)); // impose hardware limitation, leaving one thread for JVM proper, UI, etc.
	}
	
	/**
	 * Get the number of milliseconds between two rounds of stack trace prints
	 * for the threads working on a job. A zero or negative interval indicates
	 * worker tracing is inactive. This feature is mostly intended for debug
	 * purposes, namely to observe threads via logging rather than breakpoints,
	 * which might be advantageous for investigating race conditions that might
	 * not even occur when a debugger interrupts or suspends worker threads on
	 * some breakpoints, or also in scenarios that would require a lot of
	 * stepping before reaching the problematic instructions.
	 * @return the current trace interval
	 */
	public static int getTraceInterval() {
		return jobThreadTraceInterval;
	}
	
	/**
	 * Set the number of milliseconds between two rounds of stack trace prints
	 * for the threads working on a job. Setting a zero or negative interval
	 * deactivates worker tracing. This feature is mostly intended for debug
	 * purposes, namely to observe threads via logging rather than breakpoints,
	 * which might be advantageous for investigating race conditions that might
	 * not even occur when a debugger interrupts or suspends worker threads on
	 * some breakpoints, or also in scenarios that would require a lot of
	 * stepping before reaching the problematic instructions.
	 * @param traceInterval the trace interval to set
	 */
	public static void setTraceInterval(int traceInterval) {
		jobThreadTraceInterval = traceInterval;
	}
	
	/**
	 * Execute a <code>Runnable</code> in multiple threads in parallel. If
	 * the <code>maxCores</code> parameter is set to a value less than 1, the
	 * job runs in as many parallel threads as possible, which is the number of
	 * available CPU cores less 1; the latter is the core running the thread
	 * that called this method. This method creates a new <code>Thread</code>
	 * for every parallel execution, which incurs some overhead. Thus, client
	 * code should only make use of this method if the overhead is offset by
	 * the performance it gains from parallel execution.
	 * @param job the job to execute
	 * @param maxCores the maximum number of CPU cores to use
	 */
	public static void runParallelJob(Runnable job, int maxCores) {
		maxCores = checkMaxCores(maxCores);
		if (maxCores <= 1) {
			job.run(); // execute right away in single thread mode
			return;
		}
		
		Thread[] threads = new Thread[maxCores];
		for (int t = 0; t < threads.length; t++)
			threads[t] = new Thread(job);
		for (int t = 0; t < threads.length; t++)
			threads[t].start();
		TracerThread tracer = null;
		if (jobThreadTraceInterval > 0)
			tracer = new TracerThread(threads);
		for (int t = 0; t < threads.length; t++) try {
			threads[t].join();
		} catch (InterruptedException ie) {t--; /* we have to make sure all threads are finished before returning */}
		if (tracer != null)
			tracer.shutdown();
	}
	
	private static class TracerThread extends Thread {
		private int traceInterval = jobThreadTraceInterval;
		private Thread[] threads;
		TracerThread(Thread[] threads) {
			this.threads = threads;
			this.start();
		}
		public void run() {
			while (this.traceInterval > 0) {
				for (int t = 0; t < this.threads.length; t++) {
					StackTraceElement[] stes = this.threads[t].getStackTrace();
					System.out.println(this.threads[t].getName() + ":");
					for (int e = 0; e < stes.length; e++)
						System.out.println("  at " + stes[e].toString());
				}
				try {
					sleep(this.traceInterval);
				} catch (InterruptedException ie) {}
			}
		}
		void shutdown() {
			this.traceInterval = -1;
		}
	}
	
	private static abstract class ParallelLoop {
		private Exception loopBodyException = null;
		private boolean breakLoop = false;
		
		/**
		 * Check if an exception has occurred in one of the parallel executions
		 * of the loop.
		 * @return true if there is an exception, false otherwise
		 */
		public synchronized boolean hasException() {
			return (this.loopBodyException != null);
		}
		
		/**
		 * Retrieve an exception that has occurred in one of the parallel
		 * executions of the loop.
		 * @return the exception
		 */
		public synchronized Exception getException() {
			return this.loopBodyException;
		}
		
		/**
		 * Check if an exception has occurred in one of the parallel executions
		 * of the loop, and throw it if throw it if there is one.
		 * @throws Exception
		 */
		public synchronized void checkException() throws Exception {
			if (this.loopBodyException != null)
				throw this.loopBodyException;
		}
		
		synchronized void setException(Exception e) {
			this.loopBodyException = e;
		}
		
		/**
		 * Break loop execution from the loop method, or even externally. This
		 * acts akin to a <code>break</code> statement in a normal loop. All
		 * execution threads will finish their current run through the loop
		 * body and then finish.
		 */
		public synchronized void breakLoop() {
			this.breakLoop = true;
		}
		
		synchronized boolean checkBreakLoop() {
			return this.breakLoop;
		}
	}
	
	/**
	 * Execute a <code>ParallelFor</code> in multiple threads in parallel. If
	 * the <code>maxCores</code> parameter is set to a value less than 1, the
	 * job runs in as many parallel threads as possible, which is the number of
	 * available CPU cores less 1; the latter is the core running the thread
	 * that called this method. This method creates a new <code>Thread</code>
	 * for every parallel execution, which incurs some overhead. Thus, client
	 * code should only make use of this method if the overhead is offset by
	 * the performance it gains from parallel execution.
	 * The <code>doFor()</code> method of the argument <code>ParallelFor</code>
	 * is called exactly once for each integer between 0 (inclusive) and
	 * <code>count</code> (exclusive). The numbers are generally in increasing
	 * order, but not strictly due to concurrency. Implementations of the
	 * <code>doFor()</code> method must thus work on one index only, without
	 * looking backward or forward. It is best to have the loop body work on an
	 * array; if it works on a <code>List</code>, it must not modify the latter.
	 * @param loop the for loop body to execute
	 * @param count the number of times to run through the loop body
	 * @param maxCores the maximum number of CPU cores to use
	 */
	public static void runParallelFor(ParallelFor loop, int count, int maxCores) {
		runParallelFor(loop, 0, count, maxCores);
	}
	
	/**
	 * Execute a <code>ParallelFor</code> in multiple threads in parallel. If
	 * the <code>maxCores</code> parameter is set to a value less than 1, the
	 * job runs in as many parallel threads as possible, which is the number of
	 * available CPU cores less 1; the latter is the core running the thread
	 * that called this method. This method creates a new <code>Thread</code>
	 * for every parallel execution, which incurs some overhead. Thus, client
	 * code should only make use of this method if the overhead is offset by
	 * the performance it gains from parallel execution.
	 * The <code>doFor()</code> method of the argument <code>ParallelFor</code>
	 * is called exactly once for each integer between <code>from</code>
	 * (inclusive) and <code>to</code> (exclusive). The numbers are generally
	 * in increasing order, but not strictly due to concurrency. Implementations
	 * of the <code>doFor()</code> method must thus work on one index only,
	 * without looking backward or forward. It is best to have the loop body
	 * work on an array; if it works on a <code>List</code>, it must not modify
	 * the latter.
	 * @param loop the for loop body to execute
	 * @param from the first number to run through the loop body
	 * @param to the first number not to to run through the loop body
	 * @param maxCores the maximum number of CPU cores to use
	 */
	public static void runParallelFor(ParallelFor loop, int from, int to, int maxCores) {
		if (to <= from)
			return;
		if (((to - from) != 1) && (maxCores != 1) && !runLinear)
			runParallelJob(new ParallelForJob(loop, from, to), Math.min(maxCores, (to - from)));
		else try {
			if ((to - from) == 1)
				loop.doFor(from);
			else for (int i = from; i < to; i++)
				loop.doFor(i);
		}
		catch (Exception e) {
			loop.setException(e);
		}
	}
	
	/**
	 * The body of a counter based <b>for</b> loop to execute in parallel. Each
	 * invocation of the <code>doFor()</code> method represents a single run
	 * through the loop body.
	 * 
	 * @author sautter
	 */
	public static abstract class ParallelFor extends ParallelLoop {
		
		/**
		 * Execute the loop body. Synchronization of data structures shared
		 * between multiple executions of the code parallelized in this method
		 * is up to implementations.
		 * @param index the counter of the for loop
		 * @throws Exception
		 */
		public abstract void doFor(int index) throws Exception;
	}
	
	private static class ParallelForJob implements Runnable {
		private ParallelFor loop;
		private LinkedList indices = new LinkedList();
		ParallelForJob(ParallelFor loop, int from, int to) {
			this.loop = loop;
			for (int i = from; i < to; i++)
				this.indices.addLast(new Integer(i));
		}
		public void run() {
			while (true) {
				
				//	check for exception in parallel executions
				if (this.loop.hasException())
					return;
				
				//	check for cross-thread 'break' call
				if (this.loop.checkBreakLoop())
					return;
				
				//	get next index
				int index;
				synchronized (this.indices) {
					if (this.indices.isEmpty())
						return;
					else index = ((Integer) this.indices.removeFirst()).intValue();
				}
				
				//	do the work
				try {
					if (jobThreadTraceInterval > 0)
						System.out.println(Thread.currentThread().getName() + ": processing index " + index);
					this.loop.doFor(index);
				}
				catch (Exception t) {
					this.loop.setException(t);
					return;
				}
			}
		}
	}
	
	/**
	 * Execute a <code>ParallelFor</code> in multiple threads in parallel. If
	 * the <code>maxCores</code> parameter is set to a value less than 1, the
	 * job runs in as many parallel threads as possible, which is the number of
	 * available CPU cores less 1; the latter is the core running the thread
	 * that called this method. This method creates a new <code>Thread</code>
	 * for every parallel execution, which incurs some overhead. Thus, client
	 * code should only make use of this method if the overhead is offset by
	 * the performance it gains from parallel execution.
	 * The <code>doIteration()</code> method of the argument
	 * <code>ParallelIteration</code> is called exactly once for each object in
	 * the argument array, generally in increasing order. The runtime type of
	 * the objects handed to the <code>doIteration()</code> method corresponds
	 * to that of the elements in the argument array.
	 * @param loop the loop body to execute
	 * @param objects an array holding the objects to process
	 * @param maxCores the maximum number of CPU cores to use
	 */
	public static void runParallelIteration(ParallelIteration loop, Object[] objects, int maxCores) {
		if (objects.length == 0)
			return;
		if ((objects.length != 1) && (maxCores != 1) && !runLinear)
			runParallelIteration(loop, Arrays.asList(objects).iterator(), Math.min(maxCores, objects.length));
		else try {
			if (objects.length == 1)
				loop.doIteration(objects[0]);
			else for (int o = 0; o < objects.length; o++)
				loop.doIteration(objects[o]);
		}
		catch (Exception e) {
			loop.setException(e);
		}
	}
	
	/**
	 * Execute a <code>ParallelFor</code> in multiple threads in parallel. If
	 * the <code>maxCores</code> parameter is set to a value less than 1, the
	 * job runs in as many parallel threads as possible, which is the number of
	 * available CPU cores less 1; the latter is the core running the thread
	 * that called this method. This method creates a new <code>Thread</code>
	 * for every parallel execution, which incurs some overhead. Thus, client
	 * code should only make use of this method if the overhead is offset by
	 * the performance it gains from parallel execution.
	 * The <code>doIteration()</code> method of the argument
	 * <code>ParallelIteration</code> is called exactly once for each element
	 * of the argument <code>List</code>, generally in increasing order. The
	 * runtime type of the objects handed to the <code>doIteration()</code>
	 * method corresponds to that of the elements in the argument <code>List</code>.
	 * @param loop the loop body to execute
	 * @param list a list whose elements to process
	 * @param maxCores the maximum number of CPU cores to use
	 */
	public static void runParallelIteration(ParallelIteration loop, List list, int maxCores) {
		if (list.isEmpty())
			return;
		if (list.size() != 1)
			runParallelIteration(loop, list.iterator(), Math.min(maxCores, list.size()));
		else try {
			loop.doIteration(list.get(0));
		}
		catch (Exception e) {
			loop.setException(e);
		}
	}
	
	/**
	 * Execute a <code>ParallelFor</code> in multiple threads in parallel. If
	 * the <code>maxCores</code> parameter is set to a value less than 1, the
	 * job runs in as many parallel threads as possible, which is the number of
	 * available CPU cores less 1; the latter is the core running the thread
	 * that called this method. This method creates a new <code>Thread</code>
	 * for every parallel execution, which incurs some overhead. Thus, client
	 * code should only make use of this method if the overhead is offset by
	 * the performance it gains from parallel execution.
	 * The <code>doIteration()</code> method of the argument
	 * <code>ParallelIteration</code> is called exactly once for each element
	 * returned by <code>next()</code> method of the argument <code>Iterator</code>.
	 * The runtime type of the objects handed to the <code>doIteration()</code>
	 * method corresponds to that of the elements in the argument
	 * <code>Iterator</code>.
	 * @param loop the loop body to execute
	 * @param iterator an <code>Iterator</code> over the elements to process
	 * @param maxCores the maximum number of CPU cores to use
	 */
	public static void runParallelIteration(ParallelIteration loop, Iterator iterator, int maxCores) {
		if (!iterator.hasNext())
			return;
		if ((maxCores != 1) && !runLinear)
			runParallelJob(new ParallelIterationJob(loop, iterator), maxCores);
		else try {
			while (iterator.hasNext())
				loop.doIteration(iterator.next());
		}
		catch (Exception e) {
			loop.setException(e);
		}
	}
	
	/**
	 * The body of an <code>Iterator</code> based <b>for</b> loop to execute in
	 * parallel. Each invocation of the <code>doIteration()</code> method
	 * represents a single run through the loop body.
	 * 
	 * @author sautter
	 */
	public static abstract class ParallelIteration extends ParallelLoop {
		
		/**
		 * Execute the loop body. Synchronization of data structures shared
		 * between multiple executions of the code parallelized in this method
		 * is up to implementations.
		 * @param obj the object to handle (from the shared iterator)
		 * @throws Exception
		 */
		public abstract void doIteration(Object obj) throws Exception;
	}
	
	private static class ParallelIterationJob implements Runnable {
		private ParallelIteration loop;
		private Iterator iterator;
		ParallelIterationJob(ParallelIteration loop, Iterator iterator) {
			this.loop = loop;
			this.iterator = iterator;
		}
		public void run() {
			while (true) {
				
				//	check for exception in parallel executions
				if (this.loop.hasException())
					return;
				
				//	check for cross-thread 'break' call
				if (this.loop.checkBreakLoop())
					return;
				
				//	get next object
				Object object;
				synchronized (this.iterator) {
					if (this.iterator.hasNext())
						object = this.iterator.next();
					else return;
				}
				
				//	do the work
				try {
					if (jobThreadTraceInterval > 0)
						System.out.println(Thread.currentThread().getName() + ": processing object " + object);
					this.loop.doIteration(object);
				}
				catch (Exception t) {
					this.loop.setException(t);
					return;
				}
			}
		}
	}
	
	/**
	 * Execute a <code>ParallelWhile</code> in multiple threads in parallel. If
	 * the <code>maxCores</code> parameter is set to a value less than 1, the
	 * job runs in as many parallel threads as possible, which is the number of
	 * available CPU cores less 1; the latter is the core running the thread
	 * that called this method. This method creates a new <code>Thread</code>
	 * for every parallel execution, which incurs some overhead. Thus, client
	 * code should only make use of this method if the overhead is offset by
	 * the performance it gains from parallel execution.
	 * @param loop the loop body to execute
	 * @param maxCores the maximum number of CPU cores to use
	 */
	public static void runParallelWhile(ParallelWhile loop, int maxCores) {
		if ((maxCores != 1) && !runLinear)
			runParallelJob(new ParallelWhileJob(loop), maxCores);
		else try {
			while (loop.doWhile()) {}
		}
		catch (Exception e) {
			loop.setException(e);
		}
	}
	
	/**
	 * The body of a <b>while</b> loop to execute in parallel. Each invocation
	 * of the <code>doWhile()</code> method represents a single run through the
	 * loop body. The <code>doWhile()</code> method is called until it returns
	 * <code>false</code>.
	 * 
	 * @author sautter
	 */
	public static abstract class ParallelWhile extends ParallelLoop {
		
		/**
		 * Execute the loop body. Synchronization of data structures shared
		 * between multiple executions of the code parallelized in this method
		 * is up to implementations.
		 * @return true if more work is to be done, false otherwise
		 * @throws Exception
		 */
		public abstract boolean doWhile() throws Exception;
	}
	
	private static class ParallelWhileJob implements Runnable {
		private ParallelWhile loop;
		ParallelWhileJob(ParallelWhile loop) {
			this.loop = loop;
		}
		public void run() {
			while (true) {
				
				//	check for exception in parallel executions
				if (this.loop.hasException())
					return;
				
				//	check for cross-thread 'break' call
				if (this.loop.checkBreakLoop())
					return;
				
				//	do the work
				try {
					if (!this.loop.doWhile())
						return;
				}
				catch (Exception t) {
					this.loop.setException(t);
					return;
				}
			}
		}
	}
}