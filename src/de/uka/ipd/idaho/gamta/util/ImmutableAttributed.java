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
package de.uka.ipd.idaho.gamta.util;


import de.uka.ipd.idaho.gamta.Attributed;

/**
 * An immutable wrapper implementation of the Attributed interface. Any attempt
 * of changing an attribute of the wrapped attributed object will result in an
 * exception being thrown.
 * 
 * @author sautter
 */
public class ImmutableAttributed implements Attributed {
	private Attributed data; // the attributed object to wrap
	
	/** Constructor
	 * @param data the attributed object to make immutable
	 */
	public ImmutableAttributed(Attributed data) {
		this.data = data;
	}
	
	/* (non-Javadoc)
	 * @see de.gamta.defaultImplementation.AbstractAttributed#clearAttributes()
	 */
	public void clearAttributes() {
		throw new RuntimeException("Illegal modification of attributes.");
	}
	
	/* (non-Javadoc)
	 * @see de.gamta.defaultImplementation.AbstractAttributed#copyAttributes(de.gamta.Attributed)
	 */
	public void copyAttributes(Attributed source) {
		throw new RuntimeException("Illegal modification of attributes.");
	}
	
	/* (non-Javadoc)
	 * @see de.gamta.defaultImplementation.AbstractAttributed#getAttribute(java.lang.String, java.lang.Object)
	 */
	public Object getAttribute(String name, Object def) {
		return this.data.getAttribute(name, def);
	}
	
	/* (non-Javadoc)
	 * @see de.gamta.defaultImplementation.AbstractAttributed#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) {
		return this.data.getAttribute(name);
	}
	
	/* (non-Javadoc)
	 * @see de.gamta.defaultImplementation.AbstractAttributed#getAttributeNames()
	 */
	public String[] getAttributeNames() {
		return this.data.getAttributeNames();
	}
	
	/* (non-Javadoc)
	 * @see de.gamta.defaultImplementation.AbstractAttributed#hasAttribute(java.lang.String)
	 */
	public boolean hasAttribute(String name) {
		return this.data.hasAttribute(name);
	}
	
	/* (non-Javadoc)
	 * @see de.gamta.defaultImplementation.AbstractAttributed#removeAttribute(java.lang.String)
	 */
	public Object removeAttribute(String name) {
		throw new RuntimeException("Illegal modification of attributes.");
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.Attributed#setAttribute(java.lang.String)
	 */
	public void setAttribute(String name) {
		throw new RuntimeException("Illegal modification of attributes.");
	}
	
	/* (non-Javadoc)
	 * @see de.gamta.defaultImplementation.AbstractAttributed#setAttribute(java.lang.String, java.lang.Object)
	 */
	public Object setAttribute(String name, Object value) {
		throw new RuntimeException("Illegal modification of attributes.");
	}
}