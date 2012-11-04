package dexels.apachecon.builder.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Set;

public class BuildComponent {
	
	private String getOutputPath() {
		return "";
	}
	
	private Writer getOutputWriter(String path, String packagePath, String scriptName,String extension) {
		return null;
	}
	
	private void generateFactoryClass(String script, String packagePath, Set<String> resources) throws IOException {
		
		String javaPackagePath = packagePath.replaceAll("/", ".");
		
		
		PrintWriter w = new PrintWriter(getOutputWriter(getOutputPath(), packagePath, script+"Factory", ".java"));
		if ("".equals(packagePath)) {
			w.println("package defaultPackage;");
		} else {
			w.println("package "+javaPackagePath+";");
		}
		
		w.println("import com.dexels.navajo.server.*;");
		w.println("import com.dexels.navajo.mapping.*;");
		w.println();
		w.println("public class "+script+"Factory extends CompiledScriptFactory {");
		w.println("	protected String getScriptName() {");
		if ("".equals(packagePath)) {
			w.println("		return \"defaultPackage."+ script+"\";");
		} else {
			w.println("		return \""+javaPackagePath+"."+ script+"\";");
		}
		w.println("	}");
		for (String res : resources) {
			addResourceField(res, w);
		}
		
		w.println("public CompiledScript getCompiledScript() throws InstantiationException, IllegalAccessException, ClassNotFoundException {");
		w.println("	Class<? extends CompiledScript> c;");
		w.println("	c = (Class<? extends CompiledScript>) Class.forName(getScriptName());");
		w.println("	CompiledScript instance = c.newInstance();");
		w.println("	super.initialize(instance);");
		w.println("	return instance;");
		w.println("}");
		w.println("");
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

	private void generateManifest(String description, String version, String packagePath, String script, Set<String> packages, String compileDate) throws IOException {
		String symbolicName = "navajo.script."+description;
		PrintWriter w = new PrintWriter(getOutputWriter(getOutputPath(), packagePath, script, ".MF"));
		
		//		properties.getCompiledScriptPath(), pathPrefix, serviceName, ".java"
		w.print("Manifest-Version: 1.0\r\n");
		w.print("Bundle-SymbolicName: "+symbolicName+"\r\n");
		w.print("Bundle-Version: "+version+"."+compileDate+"\r\n");
		w.print("Bundle-Name: "+description+"\r\n");
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
		w.print("Import-Package: "+sb.toString()+"\r\n");
		w.print("Service-Component: OSGI-INF/script.xml\r\n");
		w.print("\r\n");
		w.flush();
		w.close();
	}
	
	private void generateDs(String packagePath, String script, Set<String> dependentResources) throws IOException {
		
		String fullName;
		if (packagePath.equals("")) {
			fullName = script;
		} else {
			fullName = packagePath+"/"+script;

		}
		String javaPackagePath;
		if("".equals(packagePath)) {
			javaPackagePath = "defaultPackage";
		} else {
			javaPackagePath = packagePath.replaceAll("/", ".");
		}
		String symbolicName = fullName.replaceAll("/", ".");
		XMLElement xe = new CaseSensitiveXMLElement("scr:component");
		xe.setAttribute("xmlns:scr", "http://www.osgi.org/xmlns/scr/v1.1.0");
		xe.setAttribute("immediate", "false");
		xe.setAttribute("name",symbolicName);
		xe.setAttribute("activate", "activate");
		xe.setAttribute("deactivate", "deactivate");
		XMLElement implementation = new CaseSensitiveXMLElement("implementation");
		xe.addChild(implementation);
		implementation.setAttribute("class",javaPackagePath+"."+script+"Factory");
		XMLElement service = new CaseSensitiveXMLElement("service");
		xe.addChild(service);
		XMLElement provide = new CaseSensitiveXMLElement("provide");
		service.addChild(provide);
		provide.setAttribute("interface", "com.dexels.navajo.server.CompiledScriptFactory");
		XMLElement property = new CaseSensitiveXMLElement("property");
		xe.addChild(property);
		property.setAttribute("name", "navajo.scriptName");
		property.setAttribute("type", "String");
		property.setAttribute("value", symbolicName);
		
//		for (Dependency dependency : dependencies) {
//			XMLElement dep = new CaseSensitiveXMLElement("reference");
//			dep.setAttribute("bind", "setDependency");
//			dep.setAttribute("deptype", dependency.getType());
//			dep.setAttribute("depId", dependency.getId());
//			dep.setAttribute("depStamp", dependency.getCurrentTimeStamp());
//			xe.addChild(dep);
//			logger.debug("Dependency: "+dep.toString());
//		}
//		  <reference bind="setIOConfig" cardinality="1..1" interface="com.dexels.navajo.server.NavajoIOConfig" name="NavajoIOConfig" policy="dynamic" unbind="clearIOConfig"/>
		for (String resource : dependentResources) {
			XMLElement dep = new CaseSensitiveXMLElement("reference");
			dep.setAttribute("bind", "set"+resource);
			dep.setAttribute("unbind", "clear"+resource);
			dep.setAttribute("policy", "static");
			dep.setAttribute("cardinality", "1..1");
			dep.setAttribute("interface", "javax.sql.DataSource");
			dep.setAttribute("target", "(navajo.resource.name="+resource+")");
			xe.addChild(dep);
		}
		PrintWriter w = new PrintWriter(getOutputWriter(getOutputPath(), packagePath, script, ".xml"));
		w.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		xe.write(w);
		w.flush();
		w.close();
	}


	public void activate() {
		System.err.println("Activating TSL compiler");
//		compiler = new TslCompiler(classLoader, navajoIOConfig);
	}
	
	public void deactivate() {
		System.err.println("Deactivating TSL compiler");
	}
	


}
