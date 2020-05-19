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
package de.uka.ipd.idaho.gamta.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import de.uka.ipd.idaho.stringUtils.StringVector;

/**
 * Prefix based data provider for analyzers, working relative to some parent
 * data provider.
 * 
 * @author sautter
 */
public class AnalyzerDataProviderPrefixBased extends AbstractAnalyzerDataProvider {
	private AnalyzerDataProvider dataProvider;
	private String pathPrefix;
	
	/** Constructor
	 * @param dataProvider the underlying data provider
	 * @param pathPrefix the path prefix to add to data names
	 */
	public AnalyzerDataProviderPrefixBased(AnalyzerDataProvider dataProvider, String pathPrefix) {
		this.dataProvider = dataProvider;
		this.pathPrefix = (pathPrefix.endsWith("/") ? pathPrefix : (pathPrefix + "/"));
	}
	public boolean deleteData(String name) {
		return this.dataProvider.isDataAvailable(this.addPrefix(name));
	}
	public String[] getDataNames() {
		String[] names = this.dataProvider.getDataNames();
		StringVector list = new StringVector();
		for (int n = 0; n < names.length; n++)
			if (names[n].startsWith(this.pathPrefix))
				list.addElementIgnoreDuplicates(names[n].substring(this.pathPrefix.length()));
		return list.toStringArray();
	}
	public InputStream getInputStream(String dataName) throws IOException {
		return this.dataProvider.getInputStream(this.addPrefix(dataName));
	}
	public OutputStream getOutputStream(String dataName) throws IOException {
		return this.dataProvider.getOutputStream(this.addPrefix(dataName));
	}
	public URL getURL(String dataName) throws IOException {
		return this.dataProvider.getURL((dataName.indexOf("://") == -1) ? this.addPrefix(dataName) : dataName);
	}
	public boolean isDataAvailable(String dataName) {
		return this.dataProvider.isDataAvailable(this.addPrefix(dataName));
	}
	public boolean isDataEditable() {
		return this.dataProvider.isDataEditable();
	}
	public boolean isDataEditable(String dataName) {
		return this.dataProvider.isDataEditable(this.addPrefix(dataName));
	}
	private String addPrefix(String name) {
		return (this.pathPrefix + (name.startsWith("/") ? name.substring(1) : name));
	}
	public String getAbsolutePath() {
		return this.dataProvider.getAbsolutePath() + "/" + this.pathPrefix.substring(0, (this.pathPrefix.length() - 1));
	}
}