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
package de.uka.ipd.idaho.gamta.util.gPath.types;


/**
 * @author sautter
 *
 * TODO document this class
 */
public class GPathNumber extends GPathObject {
	
	public final double value;
	
	/**
	 * @param value
	 */
	public GPathNumber(double value) {
		this.value = value;
	}
	
	/** @see de.uka.ipd.idaho.gamta.util.gPath.types.GPathObject#asBoolean()
	 */
	public GPathBoolean asBoolean() {
		return new GPathBoolean((this.value != 0) && !Double.isNaN(this.value));
	}
	
	/** @see de.uka.ipd.idaho.gamta.util.gPath.types.GPathObject#asNumber()
	 */
	public GPathNumber asNumber() {
		return this;
	}
	
	/** @see de.uka.ipd.idaho.gamta.util.gPath.types.GPathObject#asString()
	 */
	public GPathString asString() {
		String asString = ("" + this.value);
		if (asString.endsWith(".0"))
			asString = asString.substring(0, (asString.length() - ".0".length()));
		return new GPathString(asString);
	}
}
