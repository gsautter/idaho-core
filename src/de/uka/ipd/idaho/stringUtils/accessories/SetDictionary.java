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
package de.uka.ipd.idaho.stringUtils.accessories;

import java.util.Iterator;
import java.util.Set;

import de.uka.ipd.idaho.stringUtils.MutableDictionary;
import de.uka.ipd.idaho.stringUtils.StringIterator;

/**
 * Set backed implementation of a MutableDictionary. Case sensitivity of lookup
 * in Sets is naturally fixed, and in general impossible to tell from the Set
 * proper, so client code has to indicate this property to the constructor.<br/>
 * If the argument Set is modified by client code after constructing an instance
 * of this class around it, this class reflects the changes.
 * 
 * @author sautter
 */
public class SetDictionary implements MutableDictionary {
	private Set content;
	private boolean caseSensitive;
	
	/**
	 * Constructor
	 * @param content the Set containing the entries
	 * @param caseSensitive is the Set case sensitive?
	 */
	public SetDictionary(Set content, boolean caseSensitive) {
		this.content = content;
		this.caseSensitive = caseSensitive;
	}

	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.stringUtils.Dictionary#lookup(java.lang.String)
	 */
	public boolean lookup(String string) {
		return this.lookup(string, this.caseSensitive);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.stringUtils.Dictionary#lookup(java.lang.String, boolean)
	 */
	public boolean lookup(String string, boolean caseSensitive) {
		return ((string != null) && this.content.contains(string));
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.stringUtils.Dictionary#isDefaultCaseSensitive()
	 */
	public boolean isDefaultCaseSensitive() {
		return this.caseSensitive;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.stringUtils.Dictionary#isEmpty()
	 */
	public boolean isEmpty() {
		return this.content.isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.stringUtils.Dictionary#size()
	 */
	public int size() {
		return this.content.size();
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.stringUtils.Dictionary#getEntryIterator()
	 */
	public StringIterator getEntryIterator() {
		final Iterator it = this.content.iterator();
		return new StringIterator() {
			public void remove() {}
			public boolean hasNext() {
				return it.hasNext();
			}
			public Object next() {
				return it.next();
			}
			public boolean hasMoreStrings() {
				return it.hasNext();
			}
			public String nextString() {
				return ((String) it.next());
			}
		};
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.stringUtils.MutableDictionary#supportsAddEntry()
	 */
	public boolean supportsAddEntry() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.stringUtils.MutableDictionary#addEntry(java.lang.String)
	 */
	public void addEntry(String entry) {
		if (entry != null)
			this.content.add(entry);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.stringUtils.MutableDictionary#supportsRemoveEntry()
	 */
	public boolean supportsRemoveEntry() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.stringUtils.MutableDictionary#removeEntry(java.lang.String)
	 */
	public void removeEntry(String entry) {
		if (entry != null)
			this.content.remove(entry);
	}
}
