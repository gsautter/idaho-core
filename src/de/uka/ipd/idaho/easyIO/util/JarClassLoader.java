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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class loader loads classes from JAR files, which can be added in a
 * variety of ways, in particular as <code>File</code>s, <code>URL</code>s,
 * and arbitrary <code>InputStream</code>s. <code>JarClassLoader</code> is
 * intended to take the place of a <code>URLClassLoader</code> where the
 * surrounding code needs to access JAR files that cannot easily be provided
 * as <code>URL</code>s. Instances of this class cache all added JARs in memory
 * in the form of byte arrays, indexed by the class names the individual JARs
 * provide. Once a class has been loaded, the latter is cached as well. This
 * prevents classes from being loaded more than once, preventing problems with
 * static members.
 * 
 * @author sautter
 */
public class JarClassLoader extends ClassLoader {
	private static final boolean DEBUG = false;
	
	/**
	 * Create a new component class loader. If the argument class object is not
	 * null, the parent class loader for the threaded class loader will be
	 * retrieved from this class.
	 * @param parentClassLoaderOwner the owner object of the parent class loader to
	 *            use
	 * @return a new component class loader
	 */
	public static JarClassLoader createClassLoader(Class parentClassLoaderOwner) {
		if (isThreadLocal()) {
			ClassLoaderRequest clr = new ClassLoaderRequest(parentClassLoaderOwner);
			return clr.getClassLoader();
		}
		else {
			if (parentClassLoaderOwner == null)
				return new JarClassLoader();
			else return new JarClassLoader(parentClassLoaderOwner.getClassLoader());
		}
	}
	
	private static final boolean DEBUG_FACTORY = false;
	private static LinkedList classLoaderRequestQueue = new LinkedList();
	private static ClassLoaderFactory classLoaderFactory = null;
	
	private static class ClassLoaderFactory extends Thread {
		private boolean keepRunning = true;
		public void run() {
			while (this.keepRunning) {
				ClassLoaderRequest clr = null;
				synchronized(classLoaderRequestQueue) {
					if (classLoaderRequestQueue.isEmpty()) try {
						classLoaderRequestQueue.wait();
						if (DEBUG_FACTORY) System.out.println("  factory woken up");
					} catch (InterruptedException ie) {}
					else clr = ((ClassLoaderRequest) classLoaderRequestQueue.removeFirst());
				}
				if (clr != null) {
					if (DEBUG_FACTORY) System.out.println("  got class loader request");
					
					try {
						JarClassLoader cl;
						if (clr.parentClassLoaderOwner == null)
							cl = new JarClassLoader();
						else cl = new JarClassLoader(clr.parentClassLoaderOwner.getClassLoader());
						if (DEBUG_FACTORY) System.out.println("  got class loader");
						clr.setConnection(cl, null);
					}
					catch (SecurityException se) {
						se.printStackTrace(System.out);
						clr.setConnection(null, se);
					}
					
					if (DEBUG_FACTORY) System.out.println("  class loader passed to request");
				}
			}
		}
		
		void shutdown() {
			this.keepRunning = false;
			synchronized(classLoaderRequestQueue) {
				classLoaderRequestQueue.notify();
			}
		}
	}
	
	private static class ClassLoaderRequest {
		Class parentClassLoaderOwner;
		private Object lock = new Object();
		private JarClassLoader cl;
		private SecurityException se;
		ClassLoaderRequest(Class parentClassLoaderOwner) {
			this.parentClassLoaderOwner = parentClassLoaderOwner;
		}
		JarClassLoader getClassLoader() throws SecurityException {
			synchronized(classLoaderRequestQueue) {
				classLoaderRequestQueue.addLast(this);
				if (DEBUG_FACTORY) System.out.println("  request enqueued");
				classLoaderRequestQueue.notify();
				if (DEBUG_FACTORY) System.out.println("  factory notified");
			}
			synchronized(this.lock) {
				if ((this.cl == null) && (this.se == null)) {
					try {
						if (DEBUG_FACTORY) System.out.println("  waiting");
						this.lock.wait();
					} catch (InterruptedException ie) {}
				}
				else if (DEBUG_FACTORY) System.out.println("  good service is fast :-)");
			}
			if (DEBUG_FACTORY) System.out.println("  requester woken up");
			if (this.se == null) {
				if (DEBUG_FACTORY) System.out.println("  returning class loader");
				return this.cl;
			}
			else {
				if (DEBUG_FACTORY) System.out.println("  throwing exception " + this.se.getMessage());
				throw this.se;
			}
		}
		void setConnection(JarClassLoader cl, SecurityException se) {
			synchronized(this.lock) {
				this.cl = cl;
				this.se = se;
				if (DEBUG_FACTORY) System.out.println("  class loader stored in request");
				this.lock.notify();
			}
		}
	}
	
	/**
	 * Check if ComponentClassLoader uses a dedicated thread for creating instances,
	 * and if these instances use dedicated threads for loading classes.
	 * @return the thread local.
	 */
	public static boolean isThreadLocal() {
		return (classLoaderFactory != null);
	}
	
	/**
	 * Specify whether or not ComponentClassLoader instances should be created and
	 * should load classes in a dedicated thread. In the presence of an eager
	 * SecurityManager, for instance, in an applet, this is necessary for
	 * offering class loading functionality to instances of classes that where
	 * loaded through subordinate class loaders instead of the system class
	 * loader itself. Even if such plugin instances originate from certified
	 * code, the SecurityManager won't allow them to create class loaders or
	 * load classes. Setting the threadLocal property will cause
	 * ComponentClassLoader instances to be produced and to work in a dedicated
	 * service thread, thus in code loaded entirely through the system class
	 * loader, and then hand out the loaded classes to the requesting threads.
	 * This circumvents the security restrictions. This property should be set
	 * as early as possible.
	 * @param threadLocal use a dedicated thread for loading classes?
	 */
	public static void setThreadLocal(boolean threadLocal) {
		if (threadLocal) {
			
			//	don't start twice
			if (classLoaderFactory == null) {
				classLoaderFactory = new ClassLoaderFactory();
				classLoaderFactory.start();
			}
		}
		else {
			
			//	check if something to shut down
			if (classLoaderFactory != null) {
				classLoaderFactory.shutdown();
				classLoaderFactory = null;
			}
		}
	}
	
	private HashMap jarEntryNamesToBytes = new HashMap();
	private Properties jarEntryNamesToJarNames = new Properties(); // helps tracking duplicate entries across JARs
	private HashMap classNameesToLoadedClasses = new HashMap();
	
	/**
	 * Constructor (visible to facilitate extension via subclasses)
	 */
	protected JarClassLoader() {
		super();
	}
	
	/**
	 * Constructor (visible to facilitate extension via subclasses)
	 * @param parent the parent ClassLoader
	 */
	protected JarClassLoader(ClassLoader parent) {
		super(parent);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
	 */
	public InputStream getResourceAsStream(String name) {
		byte[] bytes = ((byte[]) this.jarEntryNamesToBytes.get(name));
		return ((bytes == null) ? super.getResourceAsStream(name) : new ByteArrayInputStream(bytes));
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.ClassLoader#findClass(java.lang.String)
	 */
	protected Class findClass(String name) throws ClassNotFoundException {
		Class foundClass = ((Class) this.classNameesToLoadedClasses.get(name));
		if (foundClass == null)
			foundClass = this.findClassFromBytes(name);
		if (foundClass == null) {
			if (DEBUG) System.out.println("JCL: class '" + name + "' not found");
			throw new ClassNotFoundException(name);
		}
		return foundClass;
	}
	
	private Class findClassFromBytes(String name) {
		if (DEBUG) System.out.println("JCL: loading class '" + name + "' ...");
		
		String classDataName;
		if (name.endsWith(".class")) // adjusted to byte code data name before ... however that happened
			classDataName = name;
		else classDataName = (name.replace('.', '/') + ".class"); // convert into to byte code data name
		if (DEBUG) System.out.println(" - resource name is '" + classDataName + "'");
		
		byte[] classBytes = this.findClassBytes(classDataName);
		if (classBytes == null)
			return null;
		
		if (DEBUG) System.out.println(" - got '" + classBytes.length + "' bytes of byte code");
		Class foundClass = this.defineClass(name, classBytes, 0, classBytes.length);
		if (DEBUG) System.out.println(" - class defined");
		this.resolveClass(foundClass);
		if (DEBUG) System.out.println(" - class resolved");
		this.classNameesToLoadedClasses.put(name, foundClass);
		if (DEBUG) System.out.println(" - class loaded & cached");
		return foundClass;
	}
	
	private byte[] findClassBytes(String classDataName) {
		byte[] classBytes = ((byte[]) this.jarEntryNamesToBytes.get(classDataName));
		if (classBytes != null)
			return classBytes;
		
		if (DEBUG) System.out.println(" - byte code not found, attempting loading from parent");
		InputStream classByteSource = this.getResourceAsStream(classDataName);
		if (classByteSource == null) {
			if (DEBUG) System.out.println("   ==> byte code resource not found");
			return null;
		}
		
		try {
			ByteArrayOutputStream classByteCollector = new ByteArrayOutputStream();
			byte[] classByteBuffer = new byte[1024];
			for (int r; (r = classByteSource.read(classByteBuffer, 0, classByteBuffer.length)) != -1;)
				classByteCollector.write(classByteBuffer, 0, r);
			classBytes = classByteCollector.toByteArray();
			this.jarEntryNamesToBytes.put(classDataName, classBytes);
		}
		catch (IOException ioe) {
			if (DEBUG) {
				System.out.println("   ==> byte code resource loading failed: " + ioe.getMessage());
				ioe.printStackTrace(System.out);
			}
		}
		return classBytes;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
	 */
	protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			return super.loadClass(name, resolve);
		}
		catch (Throwable t) {
			if (DEBUG) {
				System.out.println("JCL: catching parent " + t.getClass().getName());
				t.printStackTrace(System.out);
			}
			Class cls = this.findClass(name);
			/* If we get here, we didn't get an error on our own attempt at
			 * loading the requested class, which means we know that dependency
			 * our parent could not resolve. This indicates the parent knows a
			 * class that depends on something only we know, which means JAR
			 * dependencies go against the application class loader hierarchy,
			 * and thus most likely against the _intended_ direction of JAR
			 * dependencies. */
			if ((t instanceof NoClassDefFoundError) || (t instanceof ClassNotFoundException)) {
				System.out.println("Caught " + t.getClass().getName() + " from parent on class '" + name + "'");
				System.out.println("CHECK CLASS PATH FOR REVERSE JAR DEPENDENCIES");
				System.err.println("Caught " + t.getClass().getName() + " from parent on class '" + name + "'");
				System.err.println("CHECK CLASS PATH FOR REVERSE JAR DEPENDENCIES");
			}
			return cls;
		}
	}
	
	/**
	 * Add a jar file in the form of a file object or URL. If the argument
	 * string specifies a protocol (i.e., contains <code>://</code>), this
	 * method interprets it as a URL. Otherwise, it is interpreted as a file
	 * path and name. The latter is interpreted as relative to the JVM root
	 * directory unless it starts with a <code>/</code> or with a Windows style
	 * drive letter followed by <code>:\</code> or  <code>:/</code>.
	 * @param jarName the URL or path and name of the jar file to add
	 * @throws IOException
	 */
	public void addJar(String jarName) throws IOException {
		if (jarName.indexOf("://") != -1)
			this.addJar(new URL(jarName));
		else if (jarName.startsWith("/") || (jarName.indexOf(":\\") != -1) || (jarName.indexOf(":/") != -1))
			this.addJar(new File(jarName));
		else this.addJar(new File(".", jarName));
	}
	
	/**
	 * Add a jar file in the form of a file object.
	 * @param jarFile the jar file to add
	 * @throws IOException
	 */
	public void addJar(File jarFile) throws IOException {
		InputStream jis = null;
		try {
			jis = new BufferedInputStream(new FileInputStream(jarFile));
			this.addJar(jis, jarFile.getAbsolutePath());
		}
		finally {
			if (jis != null)
				jis.close();
		}
	}
	
	/**
	 * Add a jar file in the form of a URL.
	 * @param jarUrl the URL to load the jar from
	 * @throws IOException
	 */
	public void addJar(URL jarUrl) throws IOException {
		InputStream jis = null;
		try {
			jis = new BufferedInputStream(jarUrl.openStream());
			this.addJar(jis, jarUrl.toString());
		}
		finally {
			if (jis != null)
				jis.close();
		}
	}

	/**
	 * Add a jar file, loading it from an arbitrary InputStream.
	 * @param jarSource the InputStream providing access to the jar to add
	 * @throws IOException
	 */
	public void addJar(InputStream jarSource, String jarName) throws IOException {
		if (DEBUG) System.out.println("JCL: adding JAR '" + jarName + "' ...");
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			for (int r; (r = jarSource.read(buf)) != -1;)
				baos.write(buf, 0, r);
			byte[] jarBytes = baos.toByteArray();
			if (DEBUG) System.out.println(" - loaded " + jarBytes.length + " bytes of JAR.");

			ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(jarBytes));
			for (ZipEntry ze; (ze = zis.getNextEntry()) != null;) {
				String entryName = ze.getName();
//				this.jarsByEntryNames.put(entryName, jarBytes);
//				if (DEBUG) System.out.println("  indexed jar for class " + ze.getName());
				if (entryName.endsWith("/")) {
					if (DEBUG) System.out.println(" - skipping folder entry '" + entryName + "'");
					continue;
				}
				
				baos = new ByteArrayOutputStream();
				for (int r; (r = zis.read(buf)) != -1;)
					baos.write(buf, 0, r);
				byte[] entryBytes = baos.toByteArray();
				if (DEBUG) System.out.println(" - got entry '" + entryName + "' with " + entryBytes.length + " bytes");
				
				byte[] oldEntryBytes = ((byte[]) this.jarEntryNamesToBytes.get(entryName));
				if (oldEntryBytes == null) /* no collision */ {
					this.jarEntryNamesToBytes.put(entryName, entryBytes);
					this.jarEntryNamesToJarNames.setProperty(entryName, ((jarName == null) ? "Unknown JAR" : jarName));
					if (DEBUG) System.out.println("   ==> cached");
				}
				else if ("META-INF/MANIFEST.MF".equals(entryName)) {} // those are in many JARs ...
				else if ("LICENSE.txt".equalsIgnoreCase(entryName)) {} // those are in many JARs as well ...
				else if ("README.txt".equalsIgnoreCase(entryName)) {} // those are in many JARs as well ...
				else if (Arrays.equals(entryBytes, oldEntryBytes)) /* equal duplicate, log it */ {
					System.err.println("WARNING: got duplicate bytes for entry '" + entryName + "' in JAR '" + jarName + "' (first loaded from '" + this.jarEntryNamesToJarNames.getProperty(entryName) + "')");
					System.out.println("   ==> got duplicate bytes for entry '" + entryName + "' in JAR '" + jarName + "' (first loaded from '" + this.jarEntryNamesToJarNames.getProperty(entryName) + "')");
				}
				else /* conflicting duplicate, log it */  {
					System.err.println("WARNING: got conflicting bytes for entry '" + entryName + "' in JAR '" + jarName + "' (first loaded from '" + this.jarEntryNamesToJarNames.getProperty(entryName) + "')");
					if (DEBUG) System.out.println("   ==> got conflicting bytes for entry '" + entryName + "' in JAR '" + jarName + "' (first loaded from '" + this.jarEntryNamesToJarNames.getProperty(entryName) + "')");
				}
//				if (DEBUG) System.out.println("  indexed bytes of jar entry " + ze.getName());
			}
		}
		catch (IOException ioe) {
			System.out.println("Exception loading JAR: " + ioe.getMessage());
			ioe.printStackTrace(System.out);
			throw ioe;
		}
	}
	
	/**
	 * Retrieve a list of the raw JAR entry names indexed in this class loader,
	 * i.e., in all the JARs added to it so far. The associated bytes can be
	 * obtained from The <code>getEntryBytes()</code> method. The list returned
	 * by this method does <b>not</b> include entry names from parent class
	 * loader.
	 * @return a array holding the entry names
	 */
	public String[] getEntryNames() {
		return this.getEntryNames(this);
	}
	
	/**
	 * Retrieve a list of the raw JAR entry names indexed in this class loader,
	 * i.e., in all the JARs added to it so far. The associated bytes can be
	 * obtained from The <code>getEntryBytes()</code> method. If the argument
	 * boolean is set to true, the list returned by this method also includes
	 * the entry names of any parent <code>JarClassLoader</code>.
	 * @return a array holding the entry names
	 */
	public String[] getEntryNames(boolean includeParents) {
		return this.getEntryNames(includeParents ? null : this);
	}
	
	/**
	 * Retrieve a list of the raw JAR entry names indexed in this class loader,
	 * i.e., in all the JARs added to it so far. The associated bytes can be
	 * obtained from The <code>getEntryBytes()</code> method. If the argument
	 * class loader is not null, recursive population of the list will not
	 * consider any ancestor <code>JarClassLoader</code> beyond the argument
	 * one.
	 * @return a array holding the entry names
	 */
	public String[] getEntryNames(ClassLoader stopAfter) {
		HashSet entryNames = new HashSet();
		this.addEntryNames(entryNames, stopAfter);
		String[] ens = ((String[]) entryNames.toArray(new String[entryNames.size()]));
		Arrays.sort(ens);
		return ens;
	}
	private void addEntryNames(HashSet entryNames, ClassLoader stopAfter) {
		entryNames.addAll(this.jarEntryNamesToBytes.keySet());
		if (stopAfter == this)
			return;
		ClassLoader pcl = this.getParent();
		if (pcl instanceof JarClassLoader)
			((JarClassLoader) pcl).addEntryNames(entryNames, stopAfter);
	}
	
	/**
	 * Retrieve the bytes of any entry of this class loader. This method uses
	 * the same delegation behavior as the <code>getResource()</code> and
	 * <code>getResourceAsStream()</code> methods, but saves client code the
	 * peculiarities of working with data streams and URLs.
	 * @param entryName the name of the entry to retrieve the bytes for
	 * @return an array holding the raw bytes of the entry with the specified
	 *            name
	 */
	public byte[] getEntryBytes(String entryName) {
		byte[] ebs = null;
		ClassLoader pcl = this.getParent();
		if (pcl instanceof JarClassLoader)
			ebs = ((JarClassLoader) pcl).getEntryBytes(entryName);
		else if (pcl != null) {
			InputStream eis = pcl.getResourceAsStream(entryName);
			if (eis != null) try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				for (int r; (r = eis.read(buf)) != -1;)
					baos.write(buf, 0, r);
				return baos.toByteArray();
			} catch (IOException ioe) { /* ignore exception, just like parent class loader */ }
		}
		if (ebs == null)
			ebs = ((byte[]) this.jarEntryNamesToBytes.get(entryName));
		return ebs;
	}
}
