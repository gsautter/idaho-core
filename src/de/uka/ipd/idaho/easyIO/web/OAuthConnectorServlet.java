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
 *     * Neither the name of the Universit�t Karlsruhe (TH) nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY UNIVERSIT�T KARLSRUHE (TH) / KIT AND CONTRIBUTORS 
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
package de.uka.ipd.idaho.easyIO.web;

/**
 * Servlets of this class register authentication providers with the central
 * authentication facilities of a webapp, namely AuthenticationServlet. They
 * further receive the callbacks from OAuth providers.
 * 
 * @author sautter
 */
public class OAuthConnectorServlet extends WebServlet {
	
	/* TODO:
	 * - include local token LT in login fields (128 bit random string)
	 * - onclick of login button:
	 *   - use embedded iframe to ask servlet to authenticate LT (using GET, with LT last part of path info)
	 *   - open OAuth provider login form window, including LT in state parameter
	 *   - servlet waits to respond to LT authentication request until: (a) callback from OAuth provider come in or (b) some timeout expires
	 *     - in case (a), if OAuth provider indicates approval, start session
	 *     - otherwise, send error message
	 * - read login result from embedded iframe, and
	 *   - on approval, submit login form (backend has session by now)
	 *   - otherwise, display error message (indicating timeout or whatever error was sent from OAuth provider to backend)
	 * - close OAuth provider login form window
	 */
	
	//	TODO implement as http://developers.google.com/accounts/docs/OAuth2Login
	
	//	TODO use this for tests: http://developers.google.com/oauthplayground/
}