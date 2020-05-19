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
package de.uka.ipd.idaho.stringUtils;

/**
 * Interface for a lookup dictionary that allows addition and/or removal of
 * entries.
 * 
 * @author sautter
 */
public interface MutableDictionary extends Dictionary {
	
	/**
	 * Check whether or not the Dictionary supports adding entries. If this
	 * method returns false, <code>addEntry()</code> may throw an
	 * <code>UnsupportedOperationException</code> or simply ignore calls.
	 * @return true if the Dictionary supports addition of entries
	 */
	public abstract boolean supportsAddEntry();
	
	/**
	 * Add an entry to the Dictionary.
	 * @param entry the entry to add
	 */
	public abstract void addEntry(String entry);
	
	/**
	 * Check whether or not the Dictionary supports removing entries. If this
	 * method returns false, <code>removeEntry()</code> may throw an
	 * <code>UnsupportedOperationException</code> or simply ignore calls.
	 * @return true if the Dictionary supports removal of entries
	 */
	public abstract boolean supportsRemoveEntry();
	
	/**
	 * Remove an entry from the Dictionary.
	 * @param entry the entry to remove
	 */
	public abstract void removeEntry(String entry);
}
