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
package de.uka.ipd.idaho.easyIO.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.security.AccessControlException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * This class loader loads classes from JAR files, which can be added in a
 * variety of ways, in particular as <code>File</code>s, <code>URL</code>s,
 * and arbitrary <code>InputStream</code>s. <code>ComponentClassLoader</code>
 * is intended to take the place of a <code>URLClassLoader</code> where the
 * surrounding code needs to access JAR files that cannot easily be provided
 * as <code>URL</code>s. Instances of this class cache all added JARs in memory
 * in the form of byte arrays, indexed by the class names the individual JARs
 * provide. Once a class has been loaded, the latter is cached as well. This
 * prevents classes from being loaded more than once, preventing problems with
 * static members.
 * 
 * @author sautter
 */
public class ComponentClassLoader extends JarClassLoader {
	
	/**
	 * An <code>InputStreamProvider</code> resolves data names and provides
	 * <code>InputStream</code>s to read them. This enables component class
	 * loaders to work in a variety of different scenarios.
	 * 
	 * @author sautter
	 */
	public static interface InputStreamProvider {
		
		/**
		 * Obtain an <code>InputStream</code> for an arbitrary data object. The
		 * invoking component is responsible for closing the stream after done
		 * with it.
		 * @param dataName the name of the data object
		 * @return an <code>InputStream</code> pointing to the data object with
		 *         the specified name, or null, if there is no such data object
		 */
		public abstract InputStream getInputStream(String dataName) throws IOException;
	}
	
	/**
	 * A <code>ComponentInitializer</code> can be used to do initialization of
	 * components right after loading, with the information which JAR file a
	 * given component was loaded from still available. This is useful if
	 * components use data located in places associated with the JAR names.
	 * 
	 * @author sautter
	 */
	public static interface ComponentInitializer {
		
		/**
		 * Initialize a component. The runtime type of the component objects
		 * handed to this method will be sub classes of the component class
		 * specified to the class loading method.
		 * @param component the component to initialize
		 * @param componentJarName the name of the jar file the component was
		 *            loaded from (file name only, since the path of the jar
		 *            file is the folder handed to the loading method)
		 */
		public abstract void initialize(Object component, String componentJarName) throws Throwable;
	}
	
	/**
	 * A <code>ComponentLoadErrorLogger can be used to collect errors that
	 * occur during loading, instantiation, and initialization of components.
	 * In particular, it facilitates collecting and reacting to all exceptions,
	 * as opposed to only the first one to be thrown.
	 * 
	 * @author sautter
	 */
	public static class ComponentLoadErrorLogger {
		
		/**
		 * A single error that occurred.
		 * 
		 * @author sautter
		 */
		public static class ComponentLoadError {
			
			/** the name of the class whose loading, instantiation, or initialization failed */
			public final String className;
			
			/** the error that occurred */
			public final Throwable error;
			
			/** the phase the error happened in, i.e., loading, instantiation, or initialization */
			public final String phase;
			
			/** Constructor
			 * @param className the name of the component class the error occurred with
			 * @param error the error proper
			 * @param phase the component life cycle phase the error occurred in
			 */
			public ComponentLoadError(String className, Throwable error, String phase) {
				this.className = className;
				this.error = error;
				this.phase = phase;
			}
		}
		private Map errorsByClassName = new TreeMap();
		
		/**
		 * Log a component load error.
		 * @param className the name of the component class the error occurred with
		 * @param error the error proper
		 * @param phase the component life cycle phase the error occurred in
		 */
		public void logComponentLoadError(String className, Throwable error, String phase) {
			this.errorsByClassName.put(className, new ComponentLoadError(className, error, phase));
		}
		
		/**
		 * Get the number of logged errors.
		 * @return the number of logged errors
		 */
		public int getErrorCount() {
			return this.errorsByClassName.size();
		}
		
		/**
		 * Get the logged errors.
		 * @return an array holding the logged errors
		 */
		public ComponentLoadError[] getErrors() {
			return ((ComponentLoadError[]) this.errorsByClassName.values().toArray(new ComponentLoadError[this.errorsByClassName.size()]));
		}
	}
	
	/**
	 * Load and instantiate components from the jar files residing in some
	 * folder. The runtime type of the component objects returned will be the
	 * specified component class or a sub class of it. If a component requires
	 * extra jars that are not on the surrounding application's class path,
	 * deposit them in '&lt;componentFolder&gt;/&lt;componentJarName&gt;Lib',
	 * where &lt;componentFolder&gt; is the specified folder and
	 * &lt;componentJarName&gt; is the name of the jar file the component was
	 * loaded from, without the '.jar' file extension.<br>
	 * <br>
	 * Example: Suppose the component folder is 'D:/MyApplication/Components',
	 * and the component MyComponent resides in MyComponentJar.jar, then all
	 * jars in 'D:/MyApplication/Components/MyComponentJarLib' will be placed on
	 * the class path for loading MyComponent. You have to create this folder
	 * manually in case you need to make some additional jar files available.
	 * @param componentFolder the folder containing the jar files to search for
	 *            components
	 * @param componentSuperClass the common super class of all the components
	 *            to load
	 * @param componentInitializer an initializer that can do component class
	 *            specific initialization after a component is instantiated (may
	 *            be null)
	 * @return an array holding the components found in the jar files in the
	 *         specified folder
	 */
	public static Object[] loadComponents(final File componentFolder, Class componentSuperClass, ComponentInitializer componentInitializer) {
		return loadComponents(componentFolder, componentSuperClass, componentInitializer, null);
	}
	
	/**
	 * Load and instantiate components from the jar files residing in some
	 * folder. The runtime type of the component objects returned will be the
	 * specified component class or a sub class of it. If a component requires
	 * extra jars that are not on the surrounding application's class path,
	 * deposit them in '&lt;componentFolder&gt;/&lt;componentJarName&gt;Lib',
	 * where &lt;componentFolder&gt; is the specified folder and
	 * &lt;componentJarName&gt; is the name of the jar file the component was
	 * loaded from, without the '.jar' file extension.<br>
	 * <br>
	 * Example: Suppose the component folder is 'D:/MyApplication/Components',
	 * and the component MyComponent resides in MyComponentJar.jar, then all
	 * jars in 'D:/MyApplication/Components/MyComponentJarLib' will be placed on
	 * the class path for loading MyComponent. You have to create this folder
	 * manually in case you need to make some additional jar files available.
	 * @param componentFolder the folder containing the jar files to search for
	 *            components
	 * @param componentSuperClass the common super class of all the components
	 *            to load
	 * @param componentInitializer an initializer that can do component class
	 *            specific initialization after a component is instantiated (may
	 *            be null)
	 * @param errorLogger an error logger to collect any errors that occur
	 * @return an array holding the components found in the jar files in the
	 *         specified folder
	 */
	public static Object[] loadComponents(final File componentFolder, Class componentSuperClass, ComponentInitializer componentInitializer, ComponentLoadErrorLogger errorLogger) {
		
		// get base directory
		if (!componentFolder.exists())
			componentFolder.mkdir();
		
		// get data names
		String[] dataNames = readFileList(componentFolder, (componentFolder.getAbsolutePath().length() + 1), 1);
		
		// load components
		return loadComponents(
				dataNames,
				null,
				new InputStreamProvider() {
					public InputStream getInputStream(String dataName) throws IOException {
						return new FileInputStream(new File(componentFolder, dataName));
					}
				},
				componentSuperClass,
				componentInitializer,
				errorLogger
			);
	}
	
	private static String[] readFileList(File directory, int basePathLength, int depth) {
		if (depth < 0)
			return new String[0];
		ArrayList resultFiles = new ArrayList();
		fillFileList(directory, basePathLength, depth, resultFiles);
		return ((String[]) resultFiles.toArray(new String[resultFiles.size()]));
	}
	private static void fillFileList(File directory, int basePathLength, int depth, ArrayList resultFiles) {
		if (depth < 0)
			return;
		File[] files = directory.listFiles();
		for (int f = 0; f < files.length; f++) {
			if (files[f].isDirectory() && !files[f].equals(directory))
				fillFileList(files[f], basePathLength, (depth - 1), resultFiles);
			else resultFiles.add(files[f].getAbsolutePath().substring(basePathLength).replaceAll("\\\\", "\\/"));
		}
	}
	
	/**
	 * Load and instantiate components from the jar files contained in a list of
	 * data items. The runtime type of the component objects returned will be
	 * the specified component class or a sub class of it. If a component
	 * requires extra jars that are not on the surrounding application's class
	 * path, deposit them in
	 * '&lt;componentFolder&gt;/&lt;componentJarName&gt;Lib', where
	 * &lt;componentFolder&gt; is the specified folder and
	 * &lt;componentJarName&gt; is the name of the jar file the component was
	 * loaded from, without the '.jar' file extension.<br>
	 * <br>
	 * Example: Suppose the component MyComponent resides in MyComponentJar.jar,
	 * then all jars in the path 'MyComponentJarLib/' will be placed on the
	 * class path for loading MyComponent.
	 * @param dataNames an array holding the names of the data items to search
	 *            for components
	 * @param dataNamePrefix a prefix for filtering the data items
	 * @param isProvider a resolver that resolves the names of the specified
	 *            data items to InputStreams
	 * @param componentSuperClass the common super class of all the components
	 *            to load
	 * @param componentInitializer an initializer that can do component class
	 *            specific initialization after a component is instantiated (may
	 *            be null)
	 * @return an array holding the components found in the jar files found in
	 *         the specified list
	 */
	public static Object[] loadComponents(String[] dataNames, String dataNamePrefix, InputStreamProvider isProvider, Class componentSuperClass, ComponentInitializer componentInitializer) {
		return loadComponents(dataNames, dataNamePrefix, isProvider, componentSuperClass, componentInitializer, null);
	}
	
	/**
	 * Load and instantiate components from the jar files contained in a list of
	 * data items. The runtime type of the component objects returned will be
	 * the specified component class or a sub class of it. If a component
	 * requires extra jars that are not on the surrounding application's class
	 * path, deposit them in
	 * '&lt;componentFolder&gt;/&lt;componentJarName&gt;Lib', where
	 * &lt;componentFolder&gt; is the specified folder and
	 * &lt;componentJarName&gt; is the name of the jar file the component was
	 * loaded from, without the '.jar' file extension.<br>
	 * <br>
	 * Example: Suppose the component MyComponent resides in MyComponentJar.jar,
	 * then all jars in the path 'MyComponentJarLib/' will be placed on the
	 * class path for loading MyComponent.
	 * @param dataNames an array holding the names of the data items to search
	 *            for components
	 * @param dataNamePrefix a prefix for filtering the data items
	 * @param isProvider a resolver that resolves the names of the specified
	 *            data items to InputStreams
	 * @param componentSuperClass the common super class of all the components
	 *            to load
	 * @param componentInitializer an initializer that can do component class
	 *            specific initialization after a component is instantiated (may
	 *            be null)
	 * @param errorLogger an error logger to collect any errors that occur
	 * @return an array holding the components found in the jar files found in
	 *         the specified list
	 */
	public static Object[] loadComponents(String[] dataNames, String dataNamePrefix, InputStreamProvider isProvider, Class componentSuperClass, ComponentInitializer componentInitializer, ComponentLoadErrorLogger errorLogger) {
		
		// normalize prefix
		if (dataNamePrefix == null)
			dataNamePrefix = "";
		else if ((dataNamePrefix.length() != 0) && !dataNamePrefix.endsWith("/"))
			dataNamePrefix += "/";
		
		System.out.println("ComponentClassLoader: loading components from data list:");
		System.out.println("  component class: " + componentSuperClass.getName());
		System.out.println("  path prefix: " + dataNamePrefix);
//		System.out.println("  data list names are:");
//		for (int d = 0; d < dataNames.length; d++)
//			System.out.println("  - " + dataNames[d]);
		
		// get jar names
		String[] jarNames = getJarNames(dataNames, dataNamePrefix);
		Arrays.sort(jarNames);
		
		// examine jars
		ArrayList jarNameList = new ArrayList();
		LinkedHashMap classNamesToJarNames = new LinkedHashMap();
		System.out.println("  investigating jars:");
		for (int j = 0; j < jarNames.length; j++) {
			String jarName = jarNames[j];
			System.out.println("  - " + jarName);
			try {
				JarInputStream jis = new JarInputStream(isProvider.getInputStream(jarName));
				JarEntry je;
				while ((je = jis.getNextJarEntry()) != null) {
					String jarEntryName = je.getName();
					
					// new class file
					if (jarEntryName.endsWith(".class")) {
						jarEntryName = jarEntryName.substring(0, (jarEntryName.length() - ".class".length()));
						String className = jarEntryName.replace('/', '.');
						
						// collect names of all non-nested classes
						if (className.indexOf('$') == -1) {
							classNamesToJarNames.put(className, jarName);
							System.out.println("    - " + className);
						}
					}
				}
				jis.close();
				
				// add name of jar to list of jars to load
				jarNameList.add(jarName);
				
				// check for binary folder (DEPRECATED, FOR BACKWARD COMPATIBILITY ONLY)
				String[] jarBinJarNames = getJarNames(dataNames, (jarName.substring(0, (jarName.length() - ".jar".length())) + "Bin"));
				for (int jbj = 0; jbj < jarBinJarNames.length; jbj++) {
					System.out.println("    - " + jarBinJarNames[jbj]);
					jarNameList.add(jarBinJarNames[jbj]);
				}
				
				// check for library folder
				String[] jarLibJarNames = getJarNames(dataNames, (jarName.substring(0, (jarName.length() - ".jar".length())) + "Lib"));
				for (int jlj = 0; jlj < jarLibJarNames.length; jlj++) {
					System.out.println("    - " + jarLibJarNames[jlj]);
					jarNameList.add(jarLibJarNames[jlj]);
				}
			}
			catch (IOException ioe) {
				System.out.println("  could not access jar file '" + jarName + "': " + ioe.getMessage());
				ioe.printStackTrace(System.out);
			}
		}
		
		// check for shared binary and library folder (Lib PREFERRED, Bin DEPRECATED, FOR BACKWARD COMPATIBILITY ONLY)
		System.out.println("  adding shared jars:");
		String[] binJarNames = getJarNames(dataNames, (dataNamePrefix + "Bin"));
		for (int bj = 0; bj < binJarNames.length; bj++) {
			System.out.println("    - " + binJarNames[bj]);
			jarNameList.add(binJarNames[bj]);
		}
		String[] libJarNames = getJarNames(dataNames, (dataNamePrefix + "Lib"));
		for (int lj = 0; lj < libJarNames.length; lj++) {
			System.out.println("    - " + libJarNames[lj]);
			jarNameList.add(libJarNames[lj]);
		}
		
		// create class loader
		JarClassLoader componentLoader = JarClassLoader.createClassLoader(componentSuperClass);
		for (int j = 0; j < jarNameList.size(); j++) try {
			String jarName = ((String) jarNameList.get(j));
			InputStream jis = isProvider.getInputStream(jarName);
			componentLoader.addJar(jis, jarName);
			jis.close();
		}
		catch (IOException ioe) {
			System.out.println("  could not access jar '" + jarNameList.get(j) + "': " + ioe.getMessage());
			ioe.printStackTrace(System.out);
		}
		
		// iterate over jar entries
		ArrayList componentList = new ArrayList();
//		for (int jcn = 0; jcn < jarClassNames.size(); jcn++) {
//			String className = ((String) jarClassNames.get(jcn));
		for (Iterator cnit = classNamesToJarNames.keySet().iterator(); cnit.hasNext();) {
			String className = ((String) cnit.next());
			System.out.println("ComponentClassLoader: investigating class - " + className);
			Class componentClass = null;
			
			// try to load class
			try {
				componentClass = componentLoader.loadClass(className);
				System.out.println("  class loaded.");
			}
			catch (ClassNotFoundException cnfe) {
				System.out.println("  class not found.");
				if (errorLogger != null)
					errorLogger.logComponentLoadError(className, cnfe, "loading");
			}
			catch (NoClassDefFoundError ncdfe) {
				System.out.println("  required class not found: " + ncdfe.getMessage() + ".");
				if (errorLogger != null)
					errorLogger.logComponentLoadError(className, ncdfe, "loading");
			}
			catch (LinkageError le) {
				System.out.println("  class linkage failed: " + le.getMessage() + " (" + le.getClass().getName() + ").");
				if (errorLogger != null)
					errorLogger.logComponentLoadError(className, le, "loading");
			}
			catch (SecurityException se) { // may happen due to jar signatures
				System.out.println("  not allowed to load class.");
				if (errorLogger != null)
					errorLogger.logComponentLoadError(className, se, "loading");
			}
			
			//	anything to work with?
			if (componentClass == null)
				continue;
			if (Modifier.isAbstract(componentClass.getModifiers()))
				continue;
			if (Modifier.isInterface(componentClass.getModifiers()))
				continue;
			if (!Modifier.isPublic(componentClass.getModifiers()))
				continue;
			if (!componentSuperClass.isAssignableFrom(componentClass))
				continue;
			
			// class loaded successfully, create instance
			System.out.println("  got component class");
			Object component = null;
			try {
				component = componentClass.newInstance();
				System.out.println("  component class successfully instantiated.");
				System.out.println("  component class loaded from " + componentClass.getClassLoader());
			}
			catch (InstantiationException ie) {
				System.out.println("  could not instantiate component class.");
				if (errorLogger != null)
					errorLogger.logComponentLoadError(className, ie, "instantiation");
			}
			catch (IllegalAccessException iae) {
				System.out.println("  illegal access to component class.");
				if (errorLogger != null)
					errorLogger.logComponentLoadError(className, iae, "instantiation");
			}
			catch (NoClassDefFoundError ncdfe) {
				System.out.println("  could not find some part of component class: " + ncdfe.getMessage());
				System.out.println("  component class was loaded from " + componentClass.getClassLoader());
				ncdfe.printStackTrace(System.out);
				if (errorLogger != null)
					errorLogger.logComponentLoadError(className, ncdfe, "instantiation");
			}
			catch (AccessControlException ace) {
				Permission p = ace.getPermission();
				if (p == null)
					System.out.println("  plugin violated security constraint.");
				else System.out.println("  plugin violated security constraint, permission '" + p.getActions() + "' was denied for '" + p.getName() + "' by runtime environment.");
				if (errorLogger != null)
					errorLogger.logComponentLoadError(className, ace, "instantiation");
			}
			catch (Exception e) {
				System.out.println("  could not instantiate component class: " + e.getMessage());
				if (errorLogger != null)
					errorLogger.logComponentLoadError(className, e, "instantiation");
			}
			catch (Throwable t) {
				System.out.println("  could not instantiate component class: " + t.getMessage());
				t.printStackTrace(System.out);
				if (errorLogger != null)
					errorLogger.logComponentLoadError(className, t, "instantiation");
			}
			
			//	anything to work with?
			if (component == null)
				continue;
			
			//	initialize component if we have the facilities
			if (componentInitializer != null) try {
				String jarName = ((String) classNamesToJarNames.get(className));
				componentInitializer.initialize(component, jarName);
			}
			catch (Throwable t) {
				System.out.println("  could not initialize component class: " + t.getMessage());
				t.printStackTrace(System.out);
				if (errorLogger != null)
					errorLogger.logComponentLoadError(className, t, "initialization");
				continue; // if we have an initializer, it has to work out
			}
			
			//	store component
			componentList.add(component);
		}
		
		return componentList.toArray();
	}
	
	private static String[] getJarNames(String[] dataNames, String prefix) {
		if ((prefix.length() != 0) && !prefix.endsWith("/"))
			prefix += "/";
		HashSet jarDataNames = new HashSet();
		for (int f = 0; f < dataNames.length; f++) {
			if (dataNames[f].startsWith(prefix) && dataNames[f].endsWith(".jar") && (dataNames[f].substring(prefix.length()).indexOf('/') == -1))
				jarDataNames.add(dataNames[f]);
		}
		String[] jarNames = ((String[]) jarDataNames.toArray(new String[jarDataNames.size()]));
		Arrays.sort(jarNames);
		return jarNames;
	}
}
