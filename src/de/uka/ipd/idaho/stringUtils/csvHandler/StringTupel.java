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
package de.uka.ipd.idaho.stringUtils.csvHandler;


import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;

import de.uka.ipd.idaho.stringUtils.StringUtils;
import de.uka.ipd.idaho.stringUtils.StringVector;

/**
 * An individual tupel in a StringRelation, i.e. an individual line of a CSV
 * file. This class basically wraps CSV specific functionality around a map.
 * 
 * @author sautter
 */
public class StringTupel {
	private LinkedHashMap data = new LinkedHashMap();
	private int hash = 0;
	
	/**	add a key/value pair to this StringTupel
	 * @param	key		the key to add the specified value for (must not be null)
	 * @param	value	the value to assign to the specified key
	 * @return the value previously assigned to the specified key, or null if there was no such value
	 */
	public String setValue(String key, String value) {
		if (key == null)
			return value;
//		if (value == null) // we cannot do this, as otherwise we'd lose empty columns
//			return this.removeValue(key);
		String old = ((String) this.data.put(key, value));
//		return ((old == null) ? null : old.toString());
		if (notEqual(value, old))
			this.hash = 0;
		return old;
	}
	
	/**	@return	all the keys this StringTupel contains a value for
	 */
	public StringVector getKeys() {
		StringVector sv = new StringVector();
		this.getKeys(sv);
		return sv;
	}
	void getKeys(StringVector sv) {
		for (Iterator kit = this.data.keySet().iterator(); kit.hasNext();)
			sv.addElementIgnoreDuplicates((String) kit.next());
	}
	
	/**	@return	all the keys this StringTupel contains a value for
	 */
	public String[] getKeyArray() {
		String[] keys = new String[this.data.size()];
		this.getKeys(keys);
		return keys;
	}
	void getKeys(String[] keys) {
		int i = 0;
		for (Iterator kit = this.data.keySet().iterator(); kit.hasNext();)
			keys[i++] = ((String) kit.next());
	}
	
	/**	retrieve the value assigned to a given key
	 * @param	key		the key of the desired value
	 * @return the value assigned to the specified key, or null if there is no such value
	 */
	public String getValue(String key) {
		return this.getValue(key, null);
	}
	
	/**	retrieve the value assigned to a given key
	 * @param	key		the key of the desired value
	 * @param	def		a default value to be returned if there is no value assigned to the specified key
	 * @return the value assigned to the specified key, or def if there is no such value
	 */
	public String getValue(String key, String def) {
		String value = ((String) this.data.get(key));
		return ((value == null) ? def : value);
	}
	
	/**	rename a key
	 * @param	key		the key to replace
	 * @param	newKey	the String to replace the key with
	 */
	public void renameKey(String key, String newKey) {
//		if ((key != null) && (newKey != null)) {
//			String value = this.removeValue(key);
//			if (value != null)
//				this.setValue(newKey, value);
//		}
		if (key == null)
			return;
		String value = this.removeValue(key);
		if (newKey != null)
			this.setValue(newKey, value);
	}
	
	/**	remove the value assigned to a given key
	 * @param	key		the key of the desired value
	 * @return the value assigned to the specified key, or null if there is no such value
	 */
	public String removeValue(String key) {
		Object old = this.data.remove(key);
//		return ((old == null) ? null : old.toString());
		if (old != null)
			this.hash = 0;
		return ((String) old);
	}
	
	/**	@return	the size of this StringTupel, i.e. the number of key/value pairs contained in it
	 */
	public int size() {
		return this.data.size();
	}
	
	/**	clear this StringTupel, remove all key/value pairs
	 */
	public void clear() {
		this.data.clear();
		this.hash = 0;
	}
	
	/**	project this StringTupel to a given set of keys
	 * @param	keys	the keys to retain
	 * @return a new StringTupel containing only the values of the specified keys
	 */
	public StringTupel project(StringVector keys) {
//		StringVector remainingKeys = this.getKeys().intersect(keys);
//		StringTupel st = new StringTupel();
//		for (int k = 0; k < remainingKeys.size(); k++)
//			st.setValue(remainingKeys.get(k), this.getValue(remainingKeys.get(k)));
//		return st;
		StringTupel st = new StringTupel();
		for (int k = 0; k < keys.size(); k++) {
			String key = keys.get(k);
			if (this.data.containsKey(key))
				st.data.put(key, this.data.get(key));
		}
		return st;
	}
	
	/**	project this StringTupel to a given set of keys
	 * @param	keys	the keys to retain
	 * @return a new StringTupel containing only the values of the specified keys
	 */
	public StringTupel project(String[] keys) {
		StringTupel st = new StringTupel();
		for (int k = 0; k < keys.length; k++) {
			if (this.data.containsKey(keys[k]))
				st.data.put(keys[k], this.data.get(keys[k]));
		}
		return st;
	}
	
	/**	check if this StringTupel matches a given filter
	 * @param	filter	the StringTupel to use as the filter
	 * @return true if and only if this StringTupel contains the same values as the filter for all keys contained in the filter
	 */
	public boolean matches(StringTupel filter) {
//		if ((filter == null) || (filter.size() == 0)) return true;
//		StringVector ownKeys = this.getKeys();
//		StringVector filterKeys = filter.getKeys();
//		if (!ownKeys.contains(filterKeys)) return false;
//		for (int k = 0; k < filterKeys.size(); k++) {
//			String ownValue = this.getValue(filterKeys.get(k), "");
//			String filterValue = filter.getValue(filterKeys.get(k), "");
//			if (!ownValue.equals(filterValue)) return false;
//		}
//		return true;
		if ((filter == null) || (filter.size() == 0))
			return true;
		for (Iterator kit = filter.data.keySet().iterator(); kit.hasNext();) {
			String key = ((String) kit.next());
			String ownVal = ((String) this.data.get(key));
			String filterVal = ((String) filter.data.get(key));
			if (notEqual(ownVal, filterVal))
				return false;
		}
		return true;
	}
	
	/**	join this StringTupel with another one
	 * @param	toJoin	the StringTupel to join this one with
	 * @return a new StringTupel containing the key/value pairs from both this and the argument StringTupel
	 * Note: if the values for a key differ, the new StringTupel will contain the value from this StringTupel
	 */
	public StringTupel join(StringTupel toJoin) {
//		if ((toJoin == null) || (toJoin == this)) return this;
//		StringVector joinKeys = this.getKeys().union(toJoin.getKeys());
//		StringTupel st = new StringTupel();
//		for (int k = 0; k < joinKeys.size(); k++)
//			st.setValue(joinKeys.get(k), this.getValue(joinKeys.get(k), toJoin.getValue(joinKeys.get(k))));
//		return st;
		if ((toJoin == null) || (toJoin == this))
			return this;
		StringTupel st = new StringTupel();
		st.data.putAll(toJoin.data);
		st.data.putAll(this.data); // own values replace ones from argument
		return st;
	}
	
	/** @see java.lang.Object#toString()
	 */
	public String toString() {
//		StringVector keys = this.getKeys();
//		StringVector values = new StringVector();
//		for (int k = 0; k < keys.size(); k++)
//			values.addElement(this.getValue(keys.get(k), ""));
//		return values.concatStrings("; ");
		String[] keys = this.getKeyArray();
		StringBuffer string = new StringBuffer();
		for (int k = 0; k < keys.length; k++) {
			if (k != 0)
				string.append("; ");
			string.append(this.getValue(keys[k], ""));
		}
		return string.toString();
	}

	/**
	 * Convert the data in this StringTupel to a line for a CSV file, using the
	 * default separator and value delimiter.
	 * @return a String concatenated from the values of the specified keys, in
	 *         the order of the keys
	 */
	public String toCsvString() {
		return this.toCsvString(StringRelation.DEFAULT_SEPARATOR, StringRelation.DEFAULT_VALUE_DELIMITER, this.getKeys());
	}

	/**
	 * Convert the data in this StringTupel to a line for a CSV file, using the
	 * default separator.
	 * @param valueDelimiter the character to use as the value delimiter (will
	 *            be escaped with itself if occurring in value)
	 * @return a String concatenated from the values of the specified keys, in
	 *         the order of the keys
	 */
	public String toCsvString(char valueDelimiter) {
		return this.toCsvString(StringRelation.DEFAULT_SEPARATOR, valueDelimiter, this.getKeys());
	}
	
	/**
	 * Convert the data in this StringTupel to a line for a CSV file, using the
	 * default separator and value delimiter.
	 * @param keys a StringVector containing the keys whose values to include
	 * @return a String concatenated from the values of the specified keys, in
	 *         the order of the keys
	 */
	public String toCsvString(StringVector keys) {
		return this.toCsvString(StringRelation.DEFAULT_SEPARATOR, StringRelation.DEFAULT_VALUE_DELIMITER, keys);
	}
	
	/**
	 * Convert the data in this StringTupel to a line for a CSV file, using the
	 * default separator and value delimiter.
	 * @param keys a StringVector containing the keys whose values to include
	 * @return a String concatenated from the values of the specified keys, in
	 *         the order of the keys
	 */
	public String toCsvString(String[] keys) {
		return this.toCsvString(StringRelation.DEFAULT_SEPARATOR, StringRelation.DEFAULT_VALUE_DELIMITER, keys);
	}
	
	/**
	 * Convert the data in this StringTupel to a line for a CSV file, using the
	 * default separator.
	 * @param valueDelimiter the character to use as the value delimiter (will
	 *            be escaped with itself if occurring in value)
	 * @param keys a StringVector containing the keys whose values to include
	 * @return a String concatenated from the values of the specified keys, in
	 *         the order of the keys
	 */
	public String toCsvString(char valueDelimiter, StringVector keys) {
		return this.toCsvString(StringRelation.DEFAULT_SEPARATOR, valueDelimiter, keys);
	}
	
	/**
	 * Convert the data in this StringTupel to a line for a CSV file, using the
	 * default separator.
	 * @param valueDelimiter the character to use as the value delimiter (will
	 *            be escaped with itself if occurring in value)
	 * @param keys a StringVector containing the keys whose values to include
	 * @return a String concatenated from the values of the specified keys, in
	 *         the order of the keys
	 */
	public String toCsvString(char valueDelimiter, String[] keys) {
		return this.toCsvString(StringRelation.DEFAULT_SEPARATOR, valueDelimiter, keys);
	}
	
	/**
	 * Convert the data in this StringTupel to a line for a CSV file.
	 * @param separator the value separator character
	 * @param valueDelimiter the character to use as the value delimiter (will
	 *            be escaped with itself if occuring in value)
	 * @return a String concatenated from the values of the specified keys, in
	 *         the order of the keys
	 */
	public String toCsvString(char separator, char valueDelimiter) {
		return this.toCsvString(separator, valueDelimiter, this.getKeys());
	}
	
	/**
	 * Convert the data in this StringTupel to a line for a CSV file.
	 * @param separator the value separator character
	 * @param valueDelimiter the character to use as the value delimiter (will
	 *            be escaped with itself if occurring in value)
	 * @param keys a StringVector containing the keys whose values to include
	 * @return a String concatenated from the values of the specified keys, in
	 *         the order of the keys
	 */
	public String toCsvString(char separator, char valueDelimiter, StringVector keys) {
		String delimiter = ("" + valueDelimiter);
		StringVector values = new StringVector();
		for (int k = 0; k < keys.size(); k++)
			values.addElement(delimiter + StringUtils.replaceAll(this.getValue(keys.get(k), ""), delimiter, (delimiter + delimiter)) + delimiter);
		return values.concatStrings("" + separator);
	}
	
	/**
	 * Convert the data in this StringTupel to a line for a CSV file.
	 * @param separator the value separator character
	 * @param valueDelimiter the character to use as the value delimiter (will
	 *            be escaped with itself if occurring in value)
	 * @param keys a StringVector containing the keys whose values to include
	 * @return a String concatenated from the values of the specified keys, in
	 *         the order of the keys
	 */
	public String toCsvString(char separator, char valueDelimiter, String[] keys) {
		String delimiter = ("" + valueDelimiter);
		String escapedDelimiter = (valueDelimiter + "" + valueDelimiter);
		StringBuffer csv = new StringBuffer();
		for (int k = 0; k < keys.length; k++) {
			if (k != 0)
				csv.append(separator);
			String value = this.getValue(keys[k]);
			if (value == null)
				value = "";
			if (separator == '\t')
				csv.append(value.replace('\t', ' ').replace('\r', ' ').replace('\n', ' '));
			else {
				csv.append(valueDelimiter);
				csv.append(StringUtils.replaceAll(value, delimiter, escapedDelimiter));
				csv.append(valueDelimiter);
			}
		}
		return csv.toString();
	}
	
	/** @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
//		if (obj == null) return false;
//		if (obj == this) return true;
//		if (!(obj instanceof StringTupel))
//			return super.equals(obj);
//		StringTupel st = ((StringTupel) obj);
//		if (st.size() != this.size())
//			return false;
//		StringVector keys = this.getKeys().union(st.getKeys());
//		return this.toCsvString(keys).equals(st.toCsvString(keys));
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof StringTupel))
			return false;
		StringTupel st = ((StringTupel) obj);
		if (st.size() != this.size())
			return false;
		for (Iterator kit = this.data.keySet().iterator(); kit.hasNext();) {
			String key = ((String) kit.next());
			String ownVal = this.getValue(key);
			String objVal = st.getValue(key);
			if (notEqual(ownVal, objVal))
				return false;
		}
		return true;
	}
	
	/** @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
//		StringVector keys = this.getKeys();
//		keys.sortLexicographically();
//		String csv = this.toCsvString(keys);
//		return csv.hashCode();
		if (this.data.isEmpty())
			return 0;
		if (this.hash == 0) {
			String[] keys = this.getKeyArray();
			Arrays.sort(keys);
			String csv = this.toCsvString(keys);
			this.hash = csv.hashCode();
		}
		return this.hash;
	}
	
	private static boolean notEqual(String str1, String str2) {
		if (str1 == str2)
			return false; // also catches twice null
		if ((str1 == null) || (str2 == null))
			return true;
		return !str1.equals(str2);
	}
}
