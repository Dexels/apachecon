package dexels.apachecon;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import  org.apache.felix.webconsole.AbstractWebConsolePlugin;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;

@Component(provide=Servlet.class,properties={"felix.webconsole.label=scripting"}, immediate=true)

public class WebConsoleExtension extends AbstractWebConsolePlugin implements Servlet {

	 
	private static final long serialVersionUID = -6801334928121243975L;
	@Override
	public String getLabel() {
		return "Oempaloeoempa";
	}

	@Override
	public String getTitle() {
		return "Scripting services";
	}

	@Activate
	public void activate() {
		System.err.println("Activate!");
		
	}
	@Override
	protected void renderContent(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().write("Oempalooooompa");
//		getBundleContext().g
		
	}

}
