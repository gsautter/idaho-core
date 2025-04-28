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

import de.uka.ipd.idaho.gamta.MutableCharSequence.CharSequenceEvent;


/**
 * A listener observing the changes to a MutableCharSequence
 * 
 * @author sautter
 */
public interface CharSequenceListener {
	
	/**	notify this listener that a sequence of chars has been changed in a MutableCharSequence
	 * @param	change	a CharSequenceEvent object holding the details of the change
	 */
	public abstract void charSequenceChanged(MutableCharSequence.CharSequenceEvent change);
	
	/**
	 * Weak reference wrapper for char sequence listeners. Client code that
	 * needs to be eligible for reclaiming by GC despite a sole strong
	 * reference to it still existing in a listener added to an char sequence
	 * it wants to observe can use this class to add a weak reference link to
	 * the actual listener.
	 * 
	 * @author sautter
	 */
	public static class WeakCharSequenceListener implements CharSequenceListener {
		private WeakReference cslWeakRef;
		private MutableCharSequence chars;
		
		/** Constructor
		 * @param csl the char sequence listener to wrap
		 * @param chars the char sequence observed by the argument listener
		 */
		public WeakCharSequenceListener(CharSequenceListener csl, MutableCharSequence chars) {
			this.cslWeakRef = new WeakReference(csl);
			this.chars = chars;
		}
		
		private CharSequenceListener getCharSequenceListener() {
			CharSequenceListener csl = ((CharSequenceListener) this.cslWeakRef.get());
			if (csl == null) {
				if (this.chars != null)
					this.chars.removeCharSequenceListener(this);
				this.chars = null;
			}
			return csl;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.gamta.CharSequenceListener#charSequenceChanged(de.uka.ipd.idaho.gamta.MutableCharSequence.CharSequenceEvent)
		 */
		public void charSequenceChanged(CharSequenceEvent change) {
			CharSequenceListener tsl = this.getCharSequenceListener();
			if (tsl != null)
				tsl.charSequenceChanged(change);
		}
	}
}
