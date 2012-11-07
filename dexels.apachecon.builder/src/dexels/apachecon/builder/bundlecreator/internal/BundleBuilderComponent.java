package dexels.apachecon.builder.bundlecreator.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dexels.apachecon.builder.BundleBuilder;
import dexels.apachecon.builder.internal.CaseSensitiveXMLElement;
import dexels.apachecon.builder.internal.XMLElement;
import dexels.apachecon.builder.osgicompiler.OSGiJavaCompiler;

@Component(configurationPolicy=ConfigurationPolicy.OPTIONAL, immediate=true)
public class BundleBuilderComponent implements BundleBuilder {

	private String outputPath;
	private String scriptPath;
	private String jarPath;
	
	private OSGiJavaCompiler compiler ;
	private static final Logger logger = LoggerFactory.getLogger(BundleBuilderComponent.class);
	@Reference
	public void setCompiler(OSGiJavaCompiler c) {
		this.compiler = c;
	}
	
	@Override
	public File build(String scriptName, String engine,String extension) throws IOException {
		generateFactoryClass(scriptName,new HashSet<String>(),engine,extension);
		generateManifest(scriptName, "1.0.0", getDefaultPackages());
		generateDs(scriptName, new HashSet<String>(), "dexels.apachecon.api.Evaluator");
		compileJava(scriptName);
		File jarFile = createBundleJar(scriptName, false,extension);
		return jarFile;
	}

	@Activate
	protected void activate(Map<String,Object> params, BundleContext bc) {
		scriptPath = (String) params.get("scriptPath");
		outputPath = (String) params.get("outputPath");
		jarPath = (String) params.get("jarPath");
		scriptPath = "/Users/frank/git/apachecon/dexels.apachecon.script/script";
		outputPath = "/Users/frank/git/apachecon/dexels.apachecon.script/tmp";
		jarPath = "/Users/frank/git/apachecon/dexels.apachecon.script/jars";
//		System.err.println("Activating BundleBuilder");
//		try {
//			File jarFile = build("RubyExample","ruby",".rb");
//			doInstall(bc, jarFile, true);
//			bc.installBundle(jarFile.toURI().toURL().toString());
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
	}

	private void compileJava(String scriptName) throws FileNotFoundException, IOException {
		byte[] result = compiler.compile(scriptName, getJavaSource(scriptName));
//		File output = new File(outputPath);
		File outputFile = new File(outputPath, scriptName+".class");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(outputFile);
			IOUtils.copy(new ByteArrayInputStream(result),fos);
		} finally {
			if(fos!=null) {
				fos.close();
			}
		}
	}
	
	private Set<String> getDefaultPackages() {
		Set<String> result = new HashSet<String>();
		result.add("dexels.apachecon.api");
		result.add("dexels.apachecon.api.base");
		result.add("org.osgi.framework");
		return result;
	}
	private Writer getOutputWriter( String scriptName,String extension) throws IOException {
		File output = new File(outputPath);
		File outputFile = new File(output,scriptName+extension);
		FileWriter fw = new FileWriter(outputFile);
		return fw;
}
	
	private InputStream getJavaSource(String scriptName) throws FileNotFoundException {
		File output = new File(outputPath);
		File javaSource = new File(output,scriptName+".java");
		return new FileInputStream(javaSource);
	}
	
//	package dexels.apachecon;
//
//	import org.osgi.framework.BundleContext;
//	import org.osgi.service.component.annotations.Activate;
//	import org.osgi.service.component.annotations.Component;
//	import org.osgi.service.component.annotations.ConfigurationPolicy;
//
//	import dexels.apachecon.api.Evaluator;
//	import dexels.apachecon.api.base.BaseEvaluator;
//
//	@Component(immediate=true,configurationPolicy=ConfigurationPolicy.IGNORE,property={"name=ManualScript"})
//	public class ManualScript extends BaseEvaluator implements Evaluator {
//
//		public ManualScript() {
//			super(ManualScript.class.getSimpleName(),"ruby",".rb");
//		}
//		
//		@Activate
//		public void activate(BundleContext bc) {
//			super.activate(bc);
//		}
//
//	}
	
	
	private void generateFactoryClass(String script, Set<String> resources, String engine, String extension) throws IOException {
		
		PrintWriter w = new PrintWriter(getOutputWriter( script, ".java"));
//		PrintWriter w = new PrintWriter(writer);
		w.println("package script;");
//		w.println("import org.osgi.framework.BundleContext;");
		w.println("import dexels.apachecon.api.Evaluator;");
		w.println("import dexels.apachecon.api.base.BaseEvaluator;");
		w.println();
		w.println("public class "+script+" extends BaseEvaluator implements Evaluator {");
		w.println();
		for (String res : resources) {
			addResourceField(res, w);
		}
		w.println();
		w.println("  public "+script+"() {");
		w.println("    super("+script+".class.getSimpleName(),\""+engine+"\",\""+extension+"\");");
		w.println("  }");
		w.println();
		w.println("	protected String getScriptName() {");
		w.println("		return \"script."+ script+"\";");
		w.println("	}");
		w.println();
//		w.println("	public void activate(BundleContext bc) {");
//		w.println("	  super.activate(bc);");
//		w.println("	}");

		for (String res : resources) {
			addResourceDependency(res, w,"set");
			addResourceDependency(res, w,"clear");
		}
		w.println("");
		
		w.println("}");
		w.flush();
		w.close();
		
	}
	private void addResourceField(String res, PrintWriter w) {
		w.println("private Object _"+res+";");
	}

	private void addResourceDependency(String res, PrintWriter w,String prefix) {
		w.println("void "+prefix+res+"(Object resource) {");
		w.println("  this._"+res+" = resource;");
		w.println("  "+prefix+"Resource(\""+res+"\",resource); ");
		w.println("}\n");
	}
	

	private void generateManifest(String name, String version,Set<String> packages) throws IOException {
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String compileDate = df.format(new Date());
		String symbolicName = "navajo.script."+name;
		PrintWriter w = new PrintWriter(getOutputWriter( name, ".MF"));
//		PrintWriter w = new PrintWriter(writer);

		//		properties.getCompiledScriptPath(), pathPrefix, serviceName, ".java"
		w.print("Manifest-Version: 1.0\r\n");
		w.print("Bundle-SymbolicName: "+symbolicName+"\r\n");
		w.print("Bundle-Version: "+version+"."+compileDate+"\r\n");
		w.print("Bundle-Name: "+name+"\r\n");
		w.print("Bundle-RequiredExecutionEnvironment: JavaSE-1.6\r\n");
		w.print("Bundle-ManifestVersion: 2\r\n");
		w.print("Bundle-ClassPath: .\r\n");
		
		StringBuffer sb = new StringBuffer();
		Iterator<String> it = packages.iterator();
		boolean first = true;
		while (it.hasNext()) {
			if(!first) {
				sb.append(" ");
			}
			first = false;
			String pck =  it.next();
			sb.append(pck);
			if(it.hasNext()) {
				sb.append(",\r\n");
			}
			
		}
		// TODO Take care of scary MANIFEST restrictions (only 80 chars per line max, I think?)
		w.print("Import-Package: "+sb.toString()+"\r\n");
		w.print("Service-Component: OSGI-INF/script.xml\r\n");
		w.print("\r\n");
		w.flush();
		w.close();
	}
	
	private void generateDs(String script, Set<String> dependentResources, String scriptInterface) throws IOException {
		Writer writer = getOutputWriter( script, ".xml");

		String fullName = "script."+script;
		String symbolicName = fullName.replaceAll("/", ".");
		XMLElement xe = new CaseSensitiveXMLElement("scr:component");
		xe.setAttribute("xmlns:scr", "http://www.osgi.org/xmlns/scr/v1.1.0");
		xe.setAttribute("immediate", "true");
		xe.setAttribute("name",symbolicName);
		xe.setAttribute("activate", "activate");
		xe.setAttribute("deactivate", "deactivate");
		XMLElement implementation = new CaseSensitiveXMLElement("implementation");
		xe.addChild(implementation);
		implementation.setAttribute("class","script."+script);
		XMLElement service = new CaseSensitiveXMLElement("service");
		xe.addChild(service);
		XMLElement provide = new CaseSensitiveXMLElement("provide");
		service.addChild(provide);
		provide.setAttribute("interface", "dexels.apachecon.api.Evaluator");
		XMLElement property = new CaseSensitiveXMLElement("property");
		xe.addChild(property);
		property.setAttribute("name", "name");
		property.setAttribute("type", "String");
		property.setAttribute("value", script);
		
		for (String resource : dependentResources) {
			XMLElement dep = new CaseSensitiveXMLElement("reference");
			dep.setAttribute("bind", "set"+resource);
			dep.setAttribute("unbind", "clear"+resource);
			dep.setAttribute("policy", "static");
			dep.setAttribute("cardinality", "1..1");
			dep.setAttribute("interface", scriptInterface);
			dep.setAttribute("target", "(navajo.resource.name="+resource+")");
			xe.addChild(dep);
		}
//		PrintWriter w = new PrintWriter(getOutputWriter(getOutputPath(), script, ".xml"));
		PrintWriter w = new PrintWriter(writer);

		w.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		xe.write(w);
		w.flush();
		w.close();
	}
	

	private File createBundleJar(String scriptPath,boolean keepIntermediateFiles, String extension) throws IOException {
		String packagePath = "";
		String script = null;
		String fixOffset = "script"; //packagePath.equals("")?"defaultPackage":"";
		File out = new File(outputPath);
//		File out = new File(outB,fixOffset);
		
		File java = new File(out,scriptPath+".java");
		File factoryJavaFile = new File(out,scriptPath+"Factory.java");
		File classFile = new File(out,scriptPath+".class");
		File factoryClassFile = new File(out,scriptPath+"Factory.class");
		File manifestFile = new File(out,scriptPath+".MF");
		File dsFile = new File(out,scriptPath+".xml");
		File bundleDir = new File(out,scriptPath);
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
		System.err.println("bundlepackageDir: "+bundlePackageDir.getAbsolutePath());
		if(!bundlePackageDir.exists()) {
			bundlePackageDir.mkdirs();
		}
		File classFileInPlace = new File(bundlePackageDir,scriptPath+".class");
		FileUtils.copyFileToDirectory(new File(this.scriptPath,scriptPath+extension), bundlePackageDir);
//		File factoryClassFileInPlace = new File(bundlePackageDir,script+"Factory.class");
		FileUtils.copyFile(classFile, classFileInPlace);
//		FileUtils.copyFile(factoryClassFile, factoryClassFileInPlace);
		FileUtils.copyFile(manifestFile, metainfManifest);
		FileUtils.copyFile(dsFile, osgiinfScript);
		final File jarFile = new File(jarPath,scriptPath+".jar");
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

	private Bundle doInstall(BundleContext bundleContext, File bundleFile, boolean force) throws BundleException,FileNotFoundException, MalformedURLException {
		final String uri = bundleFile.toURI().toURL().toString();
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
		b = bundleContext.installBundle(uri, fis);
		b.start();
		return b;
	}

}
