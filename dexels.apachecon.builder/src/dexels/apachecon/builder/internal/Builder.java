package dexels.apachecon.builder.internal;

import java.io.InputStream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import dexels.apachecon.builder.osgicompiler.OSGiJavaCompiler;

@Component
public class Builder {
	   private OSGiJavaCompiler javaCompiler;

	   
	@Reference(name="javaCompiler")
		public void setCompiler(OSGiJavaCompiler j) {
		   System.err.println("j: "+j);
		   this.javaCompiler = j;
	   }
	
	public void createBundle(String name, InputStream script) {
		
	}
	
}
