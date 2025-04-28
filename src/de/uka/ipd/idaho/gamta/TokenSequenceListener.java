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
package de.uka.ipd.idaho.gamta;

import java.lang.ref.WeakReference;

import de.uka.ipd.idaho.gamta.MutableTokenSequence.TokenSequenceEvent;


/**
 * @author sautter
 *
 * TODO document this class
 */
public interface TokenSequenceListener {
	
	/**	notify this listener that a sequence of chars has been changed in a MutableTokenSequence
	 * @param	change	a TokenSequenceEvent object holding the details of the change
	 */
	public abstract void tokenSequenceChanged(MutableTokenSequence.TokenSequenceEvent change);
	
	/**
	 * Weak reference wrapper for token sequence listeners. Client code that
	 * needs to be eligible for reclaiming by GC despite a sole strong
	 * reference to it still existing in a listener added to an token sequence
	 * it wants to observe can use this class to add a weak reference link to
	 * the actual listener.
	 * 
	 * @author sautter
	 */
	public static class WeakTokenSequenceListener implements TokenSequenceListener {
		private WeakReference tslWeakRef;
		private MutableTokenSequence tokens;
		
		/** Constructor
		 * @param tsl the token sequence listener to wrap
		 * @param tokens the token sequence observed by the argument listener
		 */
		public WeakTokenSequenceListener(TokenSequenceListener tsl, MutableTokenSequence tokens) {
			this.tslWeakRef = new WeakReference(tsl);
			this.tokens = tokens;
		}
		
		private TokenSequenceListener getTokenSequenceListener() {
			TokenSequenceListener tsl = ((TokenSequenceListener) this.tslWeakRef.get());
			if (tsl == null) {
				if (this.tokens != null)
					this.tokens.removeTokenSequenceListener(this);
				this.tokens = null;
			}
			return tsl;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.gamta.TokenSequenceListener#tokenSequenceChanged(de.uka.ipd.idaho.gamta.MutableTokenSequence.TokenSequenceEvent)
		 */
		public void tokenSequenceChanged(TokenSequenceEvent change) {
			TokenSequenceListener tsl = this.getTokenSequenceListener();
			if (tsl != null)
				tsl.tokenSequenceChanged(change);
		}
	}
}
