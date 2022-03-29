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
package de.uka.ipd.idaho.easyIO.utilities;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Basic implementation of a minimal server, taking connections from a server
 * socket, providing a thread pool to manage incoming connections, and ensuring
 * clean shutdown. Soon as the server is started, it accepts incoming
 * connections on the port handed to the constructor.
 * 
 * @author sautter
 */
public abstract class TinyServer extends Thread {
	ServerSocket srvSock;
	
	/** the name of the server (e.g. for use in log messages) */
	protected final String name;
	
	/** verbose mode (helpful for troubleshooting) */
	protected boolean verbose = false;
	
	/** Constructor
	 * @param port the port to listen on
	 * @throws IOException if the argument port is invalid or occupied
	 */
	protected TinyServer(int port) throws IOException {
		this(null, port);
	}
	
	/** Constructor
	 * @param name the name of the server (e.g. in log messages)
	 * @param port the port to listen on
	 * @throws IOException if the argument port is invalid or occupied
	 */
	protected TinyServer(String name, int port) throws IOException {
		super(((name == null) ? "TinyServer" : name) + "Main" + port);
		this.name = ((name == null) ? "TinyServer" : name);
		
		//	open server socket
		this.srvSock = new ServerSocket(port);
	}
	
	/**
	 * Set verbose mode to true or false to activate or silence debug output.
	 * @param verbose work in verbose mode?
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	/**
	 * This implementation accepts requests on the wrapped server socket until
	 * the first call to the <code>close()</code> method.
	 */
	public final void run() {
		if (this.verbose) System.out.println(this.name + ": listening on port " + this.srvSock.getLocalPort());
		
		//	serve requests until closed
		while (this.srvSock != null) try {
			Socket sock = this.srvSock.accept();
			if (this.verbose) System.out.println(this.name + ": got request from " + sock.getRemoteSocketAddress());
			ServiceThread st = this.getServiceThread();
			if (this.verbose) System.out.println(this.name + ": got service thread");
			st.service(sock);
			if (this.verbose) System.out.println(this.name + ": request handed over");
		}
		catch (Exception e) {
			if (this.srvSock == null)
				continue; // silence inevitable 'socket closed' exception on shutdown
			System.out.println(this.name + ": error serving resquest: " + e.getMessage());
			e.printStackTrace(System.out);
		}
		if (this.verbose) System.out.println(this.name + ": shutting down");
	}
	
	/**
	 * Serve a request. Implementations are supposed to read any incoming data
	 * from the argument socket and send back some response. They need not
	 * close the socket, however.<br>
	 * Implementations should not be synchronized, as this effectively prevents
	 * parallel handling of requests.
	 * @param sock the socket to work with
	 * @throws Exception
	 */
	protected abstract void service(Socket sock) throws Exception;
	
	/**
	 * Close the server, i.e., close the server socket and ensure all pooled
	 * threads terminate.
	 */
	public void close() {
		
		//	close server socket
		ServerSocket srvSock = this.srvSock;
		this.srvSock = null;
		try {
			srvSock.close();
		}
		catch (IOException ioe) {
			System.out.println(this.name + ": error closing server socket: " + ioe.getMessage());
			ioe.printStackTrace(System.out);
		}
		
		//	terminate service threads
		ServiceThread[] sts;
		synchronized (this.idleServiceThreads) {
			sts = ((ServiceThread[]) this.allServiceThreads.toArray(new ServiceThread[this.allServiceThreads.size()]));
		}
		for (int t = 0; t < sts.length; t++) {
			sts[t].shutdown();
			try {
				sts[t].join();
			} catch (InterruptedException ie) {}
		}
	}
	
	private HashSet allServiceThreads = new HashSet();
	private LinkedList idleServiceThreads = new LinkedList();
	private ServiceThread getServiceThread() {
		synchronized (this.idleServiceThreads) {
			if (this.idleServiceThreads.isEmpty()) {
				ServiceThread st = new ServiceThread(this, (this.allServiceThreads.size() + 1));
				this.allServiceThreads.add(st);
				return st;
			}
			else return ((ServiceThread) this.idleServiceThreads.removeFirst());
		}
	}
	void returnServiceThread(ServiceThread st) {
		synchronized (this.idleServiceThreads) {
			this.idleServiceThreads.addLast(st);
		}
	}
	void removeServiceThread(ServiceThread st) {
		synchronized (this.idleServiceThreads) {
			this.allServiceThreads.remove(st);
		}
	}
	private static class ServiceThread extends Thread {
		private TinyServer srv;
		private int nr;
		private Socket sock = null;
		ServiceThread(TinyServer srv, int nr) {
			super(srv.name + ".Thread" + nr);
			this.srv = srv;
			this.nr = nr;
			synchronized (this) {
				this.start();
				if (this.srv.verbose) System.out.println(this.srv.name + ".Thread" + this.nr + ": creator handshake initialized");
				try {
					this.wait();
				} catch (InterruptedException ie) {}
				if (this.srv.verbose) System.out.println(this.srv.name + ".Thread" + this.nr + ": creator handshake completed");
			}
		}
		public void run() {
			boolean handshake = true;
			
			//	service requests
			while (this.srv.srvSock != null) try {
				
				//	wait for request
				Socket sock;
				synchronized (this) {
					if (handshake) {
						this.notify();
						handshake = false;
					}
					if (this.srv.verbose) System.out.println(this.srv.name + ".Thread" + this.nr + ": awaiting request");
					this.wait();
					sock = this.sock;
					if (this.srv.verbose) System.out.println(this.srv.name + ".Thread" + this.nr + ": request taken over");
				}
				
				//	handle request
				if (sock != null) {
					if (this.srv.verbose) System.out.println(this.srv.name + ".Thread" + this.nr + ": start handling request");
					this.srv.service(sock);
					if (this.srv.verbose) System.out.println(this.srv.name + ".Thread" + this.nr + ": finished handling request");
				}
				else if (this.srv.verbose) System.out.println(this.srv.name + ".Thread" + this.nr + ": ignoring empty request");
			}
			catch (Exception e) {
				System.out.println(this.srv.name + ".Thread" + this.nr + ": error serving resquest: " + e.getMessage());
				e.printStackTrace(System.out);
			}
			finally {
				if (this.sock != null) try {
					this.sock.close();
				}
				catch (IOException ioe) {
					System.out.println(this.srv.name + ".Thread" + this.nr + ": error closing socket: " + ioe.getMessage());
					ioe.printStackTrace(System.out);
				}
				this.sock = null;
				this.srv.returnServiceThread(this);
			}
			
			//	remove from parent server
			this.srv.removeServiceThread(this);
			if (this.srv.verbose) System.out.println(this.srv.name + ".Thread" + this.nr + ": shutting down");
		}
		synchronized void service(Socket sock) {
			this.sock = sock;
			this.notify();
		}
		synchronized void shutdown() {
			this.notify();
		}
	}
}
