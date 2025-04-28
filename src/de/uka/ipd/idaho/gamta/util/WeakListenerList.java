///*
// * Copyright (c) 2006-, IPD Boehm, Universitaet Karlsruhe (TH) / KIT, by Guido Sautter
// * All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are met:
// *
// *     * Redistributions of source code must retain the above copyright
// *       notice, this list of conditions and the following disclaimer.
// *     * Redistributions in binary form must reproduce the above copyright
// *       notice, this list of conditions and the following disclaimer in the
// *       documentation and/or other materials provided with the distribution.
// *     * Neither the name of the Universitaet Karlsruhe (TH) / KIT nor the
// *       names of its contributors may be used to endorse or promote products
// *       derived from this software without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY UNIVERSITAET KARLSRUHE (TH) / KIT AND CONTRIBUTORS 
// * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
// * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
// * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//package de.uka.ipd.idaho.gamta.util;
//
//import java.lang.ref.WeakReference;
//import java.util.Arrays;
//
///**
// * Dedicated list for listeners that internally uses weak references to ensure
// * listeners registered on some document do not prevent their surrounding
// * objects from getting re-claimed by GC. The list keeps itself duplicate free
// * and rejects/ignores null entries. Listeners are ordered by time of first
// * addition to the list. Read access is intended to get the listeners into a
// * strong reference (local to calling code) via the <code>getListeners()</code>
// * method and then iterate over that array.
// * 
// * @author sautter
// */
////	caNNOT DO LISTENERS THIS WAY, as anonymously constructed ones might end up reclaimed while surrounding client code still reliant on them
//public class WeakListenerList {
//	private static final Object[] noListeners = new Object[0];
//	
//	private WeakReference[] weakListeners = null;
//	private int weakSize = 0;
//	
//	private WeakReference strongListeners = null;
//	
//	/* Basic idea: long as strong reference array holds some listener, GC
//	 * cannot reclaim it, and therefore associated weak reference will remain
//	 * valid. Thus, GC must reclaim strong reference array proper before being
//	 * able to reclaim weak reference proper. Holding on to strong reference
//	 * array mainly serves to reduce number of times it needs recreating. */
//	
//	/** no arguments needed here */
//	public WeakListenerList() {}
//	
//	/**
//	 * Add a listener to the list. If the list already contains the argument
//	 * listener, this method does not change that at all, which also means not
//	 * to add it a second time.
//	 * @param listener the listener to add
//	 */
//	public void add(Object listener) {
//		
//		//	reject null (saves respective checks in client code)
//		if (listener == null)
//			return;
//		
//		//	nothing to check when adding first listener
//		if (this.weakListeners == null) {
//			this.weakListeners = new WeakReference[4];
//			this.weakListeners[this.weakSize++] = new WeakReference(listener);
//			return;
//		}
//		
//		//	sweep GCed listeners and check if we already have incoming one
//		int removed = 0;
//		for (int l = 0; l < this.weakSize; l++) {
//			Object lstnr = this.weakListeners[l].get();
//			if (listener == lstnr)
//				listener = null; // already have this one
//			if (lstnr == null)
//				removed++; // reclaimed by GC, move up rest
//			else if (removed != 0)
//				this.weakListeners[l - removed] = this.weakListeners[l];
//		}
//		if (removed != 0) {
//			Arrays.fill(this.weakListeners, (this.weakSize - removed), this.weakSize, null);
//			this.weakSize -= removed;
//			this.strongListeners = null; // mark strong reference array as stale (even though contained array should have been GCed before any listeners contained in it)
//		}
//		if (listener == null)
//			return; // found to be duplicate, we're done
//		
//		//	add new listener to end
//		if (this.weakSize == this.weakListeners.length) {
//			WeakReference[] cWeakListeners = new WeakReference[this.weakListeners.length * 2];
//			System.arraycopy(this.weakListeners, 0, cWeakListeners, 0, this.weakListeners.length);
//			this.weakListeners = cWeakListeners;
//		}
//		this.weakListeners[this.weakSize++] = new WeakReference(listener);
//		this.strongListeners = null; // mark strong reference array as stale
//	}
//	
//	/**
//	 * Remove a listener from the list. If the list does not contain the
//	 * argument listener, this method does not change that at all.
//	 * @param listener the listener to remove
//	 */
//	public void remove(Object listener) {
//		
//		//	ignore null (we never accept this one in anyway)
//		if (listener == null)
//			return;
//		
//		//	anything to remove at all?
//		if (this.weakListeners == null)
//			return;
//		
//		//	sweep GCed listeners as well as argument one
//		int removed = 0;
//		for (int l = 0; l < this.weakSize; l++) {
//			Object lstnr = this.weakListeners[l].get();
//			if (lstnr == null)
//				removed++; // reclaimed by GC, move up rest
//			else if (lstnr == listener)
//				removed++; // this is the one we're supposed to remove
//			else if (removed != 0)
//				this.weakListeners[l - removed] = this.weakListeners[l];
//		}
//		if (removed != 0) {
//			Arrays.fill(this.weakListeners, (this.weakSize - removed), this.weakSize, null);
//			this.weakSize -= removed;
//			this.strongListeners = null; // mark strong reference array as stale (even though contained array should have been GCed before any listeners contained in it)
//		}
//	}
//	
//	/**
//	 * Clear the list.
//	 */
//	public void clear() {
//		this.weakListeners = null;
//		this.weakSize = 0;
//		this.strongListeners = null;
//	}
//	
//	/**
//	 * Retrieve the contained listeners as an array. The returned array is
//	 * guaranteed to be free from null elements or duplicates (unless client
//	 * code maliciously changes that). However, the length of the array might
//	 * be less than indicated by the <code>size()</code> method immediately
//	 * before the call to this method, as this method sweeps any listeners that
//	 * GC has reclaimed, thus reducing the number.
//	 * @return an array holding the listeners
//	 */
//	public Object[] getListeners() {
//		
//		//	anything to return at all?
//		if (this.weakSize == 0)
//			return noListeners;
//		
//		//	check strong reference array first
//		Object[] strongListeners = ((this.strongListeners == null) ? null : ((Object[]) this.strongListeners.get()));
//		if (strongListeners != null)
//			return strongListeners;
//		
//		//	(re-) create strong reference array
//		strongListeners = new Object[this.weakSize];
//		int strongSize = 0;
//		int removed = 0;
//		for (int l = 0; l < this.weakSize; l++) {
//			Object lstnr = this.weakListeners[l].get();
//			if (lstnr == null)
//				removed++; // reclaimed by GC, move up rest
//			else {
//				strongListeners[strongSize++] = lstnr;
//				if (removed != 0)
//					this.weakListeners[l - removed] = this.weakListeners[l];
//			}
//		}
//		if (removed != 0) {
//			Arrays.fill(this.weakListeners, (this.weakSize - removed), this.weakSize, null);
//			this.weakSize -= removed;
//		}
//		
//		//	trim, store, and return strong reference array
//		if (strongSize < strongListeners.length)
//			strongListeners = Arrays.copyOf(strongListeners, strongSize);
//		this.strongListeners = new WeakReference(strongListeners);
//		return strongListeners;
//	}
//	
//	/**
//	 * Retrieve the number of listeners in the list. The returned number might
//	 * be larger than, but is never less than, the length an the array obtained
//	 * from the <code>getListeners()</code> immediately after calling this
//	 * method. This is because the latter method sweeps any reclaimed entries
//	 * while populating the retired array, potentially reducing the length.
//	 * @return the size of the list
//	 */
//	public int size() {
//		return this.weakSize; // upper bound, not meant for iteration
//	}
//	
//	/**
//	 * Check whether or not the list is empty. This method might return false
//	 * despite the last contained listener having been reclaimed by GC, but it
//	 * never can erroneously return true.
//	 * @return true if the list is definitively empty
//	 */
//	public boolean isEmpty() {
//		return (this.weakSize == 0); // might still be empty, but check immediately after removal of some listener should be accurate enough
//	}
//}
