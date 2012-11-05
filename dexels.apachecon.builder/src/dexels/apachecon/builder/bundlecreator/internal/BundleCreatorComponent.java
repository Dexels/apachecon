package dexels.apachecon.builder.bundlecreator.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dexels.apachecon.builder.BundleCreator;


public class BundleCreatorComponent implements BundleCreator {

	private final static Logger logger = LoggerFactory.getLogger(BundleCreatorComponent.class);

	private ScriptCompiler scriptCompiler;
	private JavaCompiler javaCompiler;
	/* (non-Javadoc)
	 * @see com.dexels.navajo.compiler.tsl.ScriptCompiler#compileTsl(java.lang.String)
	 */
	private BundleContext bundleContext;

	
	public void setScriptCompiler(ScriptCompiler scriptCompiler) {
		this.scriptCompiler = scriptCompiler;
	}

	public void setJavaCompiler(JavaCompiler javaCompiler) {
		this.javaCompiler = javaCompiler;
	}

	
	/**
	 * The script compiler to clear
	 * @param scriptCompiler  
	 */
	public void clearScriptCompiler(ScriptCompiler scriptCompiler) {
		this.scriptCompiler = null;
	}

	/**
	 * The java compiler to clear
	 * @param javaCompiler
	 */
	public void clearJavaCompiler(JavaCompiler javaCompiler) {
		this.javaCompiler = null;
	}

	
	@Override
	public String formatCompilationDate(Date d) {
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String formatted = df.format(d);
		return formatted;
	}
	
	@Override
	public void createBundle(String scriptName, Date compilationDate, String scriptExtension, List<String> failures, List<String> success, List<String> skipped, boolean force, boolean keepIntermediate) throws Exception {
		
		String script = scriptName.replaceAll("\\.", "/");
		
		File scriptFolder = new File(navajoIOConfig.getScriptPath());
		File f = new File(scriptFolder,script);
//		boolean isInDefaultPackage = script.indexOf('/')==-1;
		final String formatCompilationDate = formatCompilationDate(compilationDate);
		File scriptFile = new File(scriptFolder,script+"."+scriptExtension);
		if(!scriptFile.exists()) {
			logger.error("Script or folder not found: "+script+" full path: "+scriptFile.getAbsolutePath());
			return;
		}
		Date compiled = getCompiledModificationDate(script);
		Date modified = getScriptModificationDate(script);
		if(!force && compiled!=null && compiled.after(modified)) {
//				logger.debug("Skipping up-to-date script: "+scriptFile.getAbsolutePath());
			skipped.add(script);
		} else {
			scriptCompiler.compileTsl(script,formatCompilationDate,dependencies);
			javaCompiler.compileJava(script);
			javaCompiler.compileJava(script+"Factory");
			createBundleJar(script,keepIntermediate);
			success.add(script);
		}

	}
	
	@Override
	public void installBundle( String scriptPath,
			List<String> failures, List<String> success, List<String> skipped, boolean force) {
		try {
			Bundle b = doInstall(scriptPath, force);
			if(b==null) {
				skipped.add(scriptPath);
			} else {
				success.add(b.getSymbolicName());
			}
		} catch (BundleException e) {
			failures.add(e.getLocalizedMessage());
			reportInstallationError(scriptPath, e);
		} catch (FileNotFoundException e1) {
			failures.add(e1.getLocalizedMessage());
			reportInstallationError(scriptPath, e1);
		}
	}

	private Bundle doInstall(String scriptPath, boolean force) throws BundleException,FileNotFoundException {
		File bundleFile = getScriptBundleJar(scriptPath);
		final String uri = SCRIPTPROTOCOL+scriptPath;
		Bundle previous = bundleContext.getBundle(uri);
		if(previous!=null) {
			if (force) {
				previous.uninstall();
				logger.debug("uninstalling bundle with URI: "+uri);
			} else {
				logger.info("Skipping bundle at: "+uri+" as it is already installed. Lastmod: "+new Date(previous.getLastModified())+" status: "+previous.getState());
				return null;
			}
		}
		logger.info("Installing script: "+bundleFile.getName());
		FileInputStream fis = new FileInputStream(bundleFile);
		Bundle b;
		b = this.bundleContext.installBundle(uri, fis);
		b.start();
		return b;
	}
	
	private String getRelative(File path, File base) {
		String relative = base.toURI().relativize(path.toURI()).getPath();
		return relative;
	}
	

	private File createBundleJar(String scriptPath,boolean keepIntermediateFiles) throws IOException {
		String packagePath = null;
		String script = null;
		if(scriptPath.indexOf('/')>=0) {
			packagePath = scriptPath.substring(0,scriptPath.lastIndexOf('/'));
			script = scriptPath.substring(scriptPath.lastIndexOf('/')+1);
		} else {
			packagePath ="";
			script=scriptPath;
		}
		String fixOffset = packagePath.equals("")?"defaultPackage":"";
		File outB = new File(navajoIOConfig.getCompiledScriptPath());
		File out = new File(outB,fixOffset);
		
		File java = new File(outB,scriptPath+".java");
		File factoryJavaFile = new File(outB,scriptPath+"Factory.java");
		File classFile = new File(out,scriptPath+".class");
		File factoryClassFile = new File(out,scriptPath+"Factory.class");
		File manifestFile = new File(outB,scriptPath+".MF");
		File dsFile = new File(outB,scriptPath+".xml");
		File bundleDir = new File(outB,scriptPath);
		if(!bundleDir.exists()) {
			bundleDir.mkdirs();
		}
		File bundlePackageFix = new File(bundleDir,fixOffset);
		File bundlePackageDir = new File(bundlePackageFix,packagePath);
		if(!bundlePackageDir.exists()) {
			bundlePackageDir.mkdirs();
		}
		File packagePathFile = new File(bundleDir,packagePath);
		if(!packagePathFile.exists()) {
			packagePathFile.mkdirs();
		}
		File metainf = new File(bundleDir,"META-INF");
		if(!metainf.exists()) {
			metainf.mkdirs();
		}
		File osgiinf = new File(bundleDir,"OSGI-INF");
		if(!osgiinf.exists()) {
			osgiinf.mkdirs();
		}
		File osgiinfScript = new File(osgiinf,"script.xml");
		File metainfManifest = new File(metainf,"MANIFEST.MF");
		
		File classFileInPlace = new File(bundlePackageDir,script+".class");
		File factoryClassFileInPlace = new File(bundlePackageDir,script+"Factory.class");
		FileUtils.copyFile(classFile, classFileInPlace);
		FileUtils.copyFile(factoryClassFile, factoryClassFileInPlace);
		FileUtils.copyFile(manifestFile, metainfManifest);
		FileUtils.copyFile(dsFile, osgiinfScript);
		final File jarFile = new File(outB,scriptPath+".jar");
		addFolderToZip(bundleDir, null,jarFile, bundleDir.getAbsolutePath()+"/");
		if(!keepIntermediateFiles) {
			classFile.delete();
			manifestFile.delete();
			dsFile.delete();
			java.delete();
			factoryJavaFile.delete();
			factoryClassFile.delete();
			FileUtils.deleteDirectory(bundleDir);
		}
		return jarFile;
	}


	
	private void addFolderToZip(File folder, ZipOutputStream zip, File zipFile, String baseName) throws IOException {
		boolean toplevel = false;
		if(zip==null) {
			zip = new ZipOutputStream(new FileOutputStream(zipFile));
			toplevel = true;
		}			
		File[] files = folder.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				addFolderToZip(file, zip, zipFile, baseName);
			} else {
				String name = file.getAbsolutePath().substring(baseName.length());
				ZipEntry zipEntry = new ZipEntry(name);
				zip.putNextEntry(zipEntry);
				IOUtils.copy(new FileInputStream(file), zip);
				zip.closeEntry();
			}
		}
		if(toplevel) {
			zip.flush();
			zip.close();
		}
	}

	@Activate
	public void activate(ComponentContext cc) {
		logger.debug("Activating Bundle creator");
		this.bundleContext = cc.getBundleContext();
		
	}
	
	@Deactivate
	public void deactivate() {
		logger.debug("Deactivating Bundle creator");
		this.bundleContext = null;
	}

	@Override
	public Date getBundleInstallationDate(String scriptPath) {
		String scrpt = scriptPath.replaceAll("/", ".");
		final String bundleURI = SCRIPTPROTOCOL+scrpt;
		Bundle b = this.bundleContext.getBundle(bundleURI);
		if(b==null) {
			logger.warn("Can not determine age of bundle: "+bundleURI+" as it can not be found.");
			return null;
		}
		return new Date(b.getLastModified());
	}
	
	private File getScriptBundleJar(String script) {
		String scriptPath = script.replaceAll("\\.", "/");
		final String extension = "jar";
		File compiledScriptPath = new File(navajoIOConfig.getCompiledScriptPath());
		File jarFile = new File(compiledScriptPath,scriptPath+"."+extension);
		return jarFile;
	}
	
	private boolean needsCompilation(String scriptPath) throws FileNotFoundException {
		Date compiled = getCompiledModificationDate(scriptPath);
		Date script = getScriptModificationDate(scriptPath);
		if(script==null) {
			throw new FileNotFoundException("Script "+scriptPath+" is missing!");
		}
		if(compiled==null) {
			return true;
		}
		return compiled.compareTo(script) < 0;
		
		
	}



	private boolean checkForRecompile(String rpcName) {
		Date mod = getScriptModificationDate(rpcName);
		if(mod==null) {
			logger.error("No modification date for script: "+rpcName+" this is weird.");
			return false;
		}
		Date install = getBundleInstallationDate(rpcName);
		if(install!=null) {
			if(install.before(mod)) {
				logger.debug("Install: "+install);
				logger.debug("mod: "+mod);
				logger.debug("comp: ", getCompiledModificationDate(rpcName));
				logger.debug("Obsolete script found. Needs recompile.");
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	
	public CompiledScript getCompiledScript(String rpcName)
			throws ClassNotFoundException {
		String filter = "(navajo.scriptName="+rpcName+")";
		
		ServiceReference[] sr;
			try {
				sr = bundleContext.getServiceReferences(CompiledScriptFactory.class.getName(), filter);
			if(sr!=null && sr.length!=0) {
				CompiledScriptFactory csf = bundleContext.getService(sr[0]);
				if(csf==null) {
					logger.warn("Script with filter: "+filter+" found, but could not be resolved.");
					return null;
				}
				CompiledScript cs = csf.getCompiledScript();
				return cs;
			}
		} catch (InvalidSyntaxException e) {
			throw new ClassNotFoundException("Error resolving script service for: "+rpcName,e);
		} catch (InstantiationException e) {
			throw new ClassNotFoundException("Error resolving script service for: "+rpcName,e);
		} catch (IllegalAccessException e) {
			throw new ClassNotFoundException("Error resolving script service for: "+rpcName,e);
		}
	
		return null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private CompiledScript waitForService(String rpcPath) throws Exception {
		String rpcName = rpcPath.replaceAll("/", ".");
//		script://person/InitSearchPersons
		final String bundleURI = SCRIPTPROTOCOL+rpcPath;
		Bundle scriptBundle = bundleContext.getBundle(bundleURI);
		if(scriptBundle==null) {
			throw new UserException(-1,"Can not resolve bundle for service: "+rpcName+" failed to find bundle with URI: "+bundleURI);
		}
		if(scriptBundle.getState() != Bundle.ACTIVE) {
			throw new UserException(-1,"Can not resolve bundle for service: "+rpcName+" bundle with URI: "+bundleURI+" is not active. State: "+scriptBundle.getState());
		}
		
		String filterString = "(navajo.scriptName="+rpcName+")";
		logger.debug("waiting for service...: "+rpcName);
		Filter filter = bundleContext.createFilter(filterString);
		ServiceReference<CompiledScriptFactory>[] ss = (ServiceReference<CompiledScriptFactory>[]) bundleContext.getServiceReferences(CompiledScriptFactory.class.getName(), filterString);
		if(ss!=null && ss.length>0) {
			logger.info("Service present: "+ss.length);
		} else {
			throw new UserException(-1,"Bundle resolved but no service available for service: "+rpcName+" probably it is missing a dependency");
		}
//		ServiceTracker tr = new ServiceTracker(bundleContext,filter,null);
//		tr.open();
//		CompiledScriptFactory result = (CompiledScriptFactory) tr.waitForService(12000);
		CompiledScriptFactory result = bundleContext.getService(ss[0]);
		
		if(result==null) {
			logger.error("Service resolution failed!");
			throw new UserException(-1,"Can not resolve bundle for service: "+rpcName);
		} 
		CompiledScript cc = result.getCompiledScript();
//		tr.close();
		return cc;
	}


}
