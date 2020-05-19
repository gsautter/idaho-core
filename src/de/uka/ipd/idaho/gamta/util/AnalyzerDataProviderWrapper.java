package de.uka.ipd.idaho.gamta.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import de.uka.ipd.idaho.stringUtils.StringVector;

/**
 * Generic wrapper for data providers, looping all methods through to the
 * wrapped data provider.
 * 
 * @author sautter
 */
public abstract class AnalyzerDataProviderWrapper extends AbstractAnalyzerDataProvider {
	protected final AnalyzerDataProvider dataProvider;
	
	/** Constructor
	 * @param dataProvider the data provider to wrap
	 */
	protected AnalyzerDataProviderWrapper(AnalyzerDataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}
	
	public String[] getDataNames() {
		String[] dataNames = dataProvider.getDataNames();
		StringVector dataNameList = new StringVector();
		for (int d = 0; d < dataNames.length; d++) {
			if (dataNames[d].indexOf("cache/") == -1)
				dataNameList.addElementIgnoreDuplicates(dataNames[d]);
		}
		return dataNameList.toStringArray();
	}
	public boolean isDataAvailable(String dataName) {
		return this.dataProvider.isDataAvailable(dataName);
	}
	public InputStream getInputStream(String dataName) throws IOException {
		return this.dataProvider.getInputStream(dataName);
	}
	public URL getURL(String dataName) throws IOException {
		return this.dataProvider.getURL(dataName);
	}
	public boolean isDataEditable() {
		return this.dataProvider.isDataEditable();
	}
	public boolean isDataEditable(String dataName) {
		return this.dataProvider.isDataEditable(dataName);
	}
	public OutputStream getOutputStream(String dataName) throws IOException {
		return this.dataProvider.getOutputStream(dataName);
	}
	public boolean deleteData(String name) {
		return this.dataProvider.deleteData(name);
	}
	public String getAbsolutePath() {
		return this.dataProvider.getAbsolutePath();
	}
}
