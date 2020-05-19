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
 *     * Neither the name of the Universitaet Karlsruhe (TH) nor the
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
package de.uka.ipd.idaho.stringUtils;


import java.util.TreeMap;

/**
 * A counting index for Strings
 * 
 * @author sautter
 */
public class StringIndex {
	private static final String NULL_KEY = ("_N_U_L_L_" + System.currentTimeMillis() + "_K_E_Y_"); // this should prevent all collisions in practice
	
	private TreeMap content; // TODO figure out how to handle large load of ordered insertions !!!
	private int size = 0;
	
	/**	Constructor
	 */
	public StringIndex() {
		this(true);
	}
	
	/**	Constructor
	 */
	public StringIndex(boolean caseSensitive) {
		this.content = (caseSensitive ? new TreeMap() : new TreeMap(String.CASE_INSENSITIVE_ORDER));
	}
	
	/**	@return		true if and only if this index contains the specified string
	 */
	public boolean contains(String string) {
		return this.content.containsKey(maskNull(string));
	}
	
	/**	@return		the number of times the specified string has been added to this index
	 */
	public int getCount(String string) {
		Int i = ((Int) this.content.get(maskNull(string)));
		return ((i == null) ? 0 : i.value);
	}
	
	/**	add a string to this index, using count 1
	 * @return	true if the specified string was added for the first time, false otherwise
	 */
	public boolean add(String string) {
		string = maskNull(string);
		Int i = ((Int) this.content.get(string));
		this.size++;
		if (i == null) {
			this.content.put(string, new Int(1));
			return true;
		}
		else {
			i.increment();
			return false;
		}
	}
	
	/**	add a string to this index, using a custom count (same as count times adding string, but faster)
	 * @return	true if the specified string was added for the first time, false otherwise
	 */
	public boolean add(String string, int count) {
		string = maskNull(string);
		Int i = ((Int) this.content.get(string));
		this.size += count;
		if (i == null) {
			this.content.put(string, new Int(count));
			return true;
		}
		else {
			i.increment(count);
			return false;
		}
	}
	
	/**	remove a string from this index once, decreasing it's count by 1
	 * @return	true if the specified string was totally removed, false otherwise
	 */
	public boolean remove(String string) {
		string = maskNull(string);
		Int i = ((Int) this.content.get(string));
		if (i == null)
			return false;
		this.size--;
		if (i.value > 1) {
			i.decrement();
			return false;
		}
		else {
			this.content.remove(string);
			return true;
		}
	}
	
	/**	remove a string from this index, using a custom count (same as count times removing string, but faster)
	 * @return	true if the specified string was totally removed, false otherwise
	 */
	public boolean remove(String string, int count) {
		string = maskNull(string);
		Int i = ((Int) this.content.get(string));
		if (i == null)
			return false;
		if (i.value > count) {
			this.size -= count;
			i.decrement(count);
			return false;
		}
		else {
			this.size -= i.value;
			this.content.remove(string);
			return true;
		}
	}
	
	/**	remove a string from this index totally, setting it's count to 0
	 */
	public void removeAll(String string) {
		string = maskNull(string);
		Int i = ((Int) this.content.get(string));
		if (i != null) {
			this.size -= i.value;
			this.content.remove(string);
		}
	}
	
	/**	totally clear this index
	 */
	public void clear() {
		this.content.clear();
		this.size = 0;
	}
	
	/**	@return		true if this index's case sensitivity property has been initialized as true
	 */
	public boolean isCaseSensitive() {
		return (this.content.comparator() == String.CASE_INSENSITIVE_ORDER);
	}
	
	/**	@return		the number of strings that have been added to this index so far
	 */
	public int size() {
		return this.size;
	}
	
	/**	@return		the number of distinct strings that have been added to this index so far
	 */
	public int distinctSize() {
		return this.content.size();
	}
	
	private static String maskNull(String str) {
		return ((str == null) ? NULL_KEY : str);
	}
	
	private class Int {
		int value;
		Int(int val) {
			this.value = val;
		}
		void increment() {
			this.value ++;
		}
		void increment(int i) {
			this.value += i;
		}
		void decrement() {
			this.value --;
		}
		void decrement(int i) {
			this.value = ((this.value > i) ? (this.value - i) : 0);
		}
	}
}
