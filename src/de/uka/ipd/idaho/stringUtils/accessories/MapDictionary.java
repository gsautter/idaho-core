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
 *     * Neither the name of the Universität Karlsruhe (TH) / KIT nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY UNIVERSITÄT KARLSRUHE (TH) / KIT AND CONTRIBUTORS 
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

import java.util.Map;

/**
 * Implementation of a MutableDictionary backed by the key set of a Map. Case
 * sensitivity of lookup in Maps is naturally fixed, and in general impossible
 * to tell from the Map proper, so client code has to indicate this property to
 * the constructor.<br/>
 * If the argument Map is modified by client code after constructing an instance
 * of this class around it, this class reflects the changes.<br/>
 * This class does support removal of entries, but not addition, as there is no
 * way of specifying a value to the key that an added entry will become.
 * 
 * @author sautter
 */
public class MapDictionary extends SetDictionary {
	
	/**
	 * Constructor
	 * @param content the Map containing the entries as keys
	 * @param caseSensitive is the Map case sensitive?
	 */
	public MapDictionary(Map content, boolean caseSensitive) {
		super(content.keySet(), caseSensitive);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.stringUtils.accessories.SetDictionary#supportsAddEntry()
	 */
	public boolean supportsAddEntry() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.stringUtils.accessories.SetDictionary#addEntry(java.lang.String)
	 */
	public void addEntry(String entry) {
		throw new UnsupportedOperationException("Adding entries not supported, use supportsAddEntry() to check.");
	}
}
