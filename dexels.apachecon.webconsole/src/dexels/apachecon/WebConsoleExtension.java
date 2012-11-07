package dexels.apachecon;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import javax.script.ScriptException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import  org.apache.felix.webconsole.AbstractWebConsolePlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import dexels.apachecon.api.Evaluator;


//@Component(service=Servlet.class,properties={"felix.webconsole.label=scripting"}, immediate=true)

@Component(configurationPolicy=ConfigurationPolicy.OPTIONAL,property={"felix.webconsole.label=scripting"})
public class WebConsoleExtension extends AbstractWebConsolePlugin implements Servlet {

//	<tr class="odd ui-state-default" id="entry0"><!-- template -->
//	<td>0</td><!-- ID -->
//	<td>
//		<div class="bIcon ui-icon ui-icon-triangle-1-e" title="Show Details" id="img0">&nbsp;</div>
//		<div class="bName"><a href="/system/console/bundles/0">System Bundle<span class="symName">org.apache.felix.framework</span></a></div> <!-- here goest bundle name/link -->
//	</td>
//	<td>4.0.3</td><!-- version -->
//	<td></td><!-- symbolic name -->
//	<td>Active</td><!-- status -->
//	<td><!-- actions -->
//		<ul class="icons ui-widget ui-helper-hidden">
//			<li class="dynhover ui-state-default ui-corner-all" title="Start"><span class="ui-icon ui-icon-play">&nbsp;</span></li>
//			<li class="dynhover ui-state-default ui-corner-all" title="Stop"><span class="ui-icon ui-icon-stop">&nbsp;</span></li>
//			<li class="dynhover ui-state-default ui-corner-all" title="Refresh Package Imports"><span class="ui-icon ui-icon-refresh">&nbsp;</span></li>
//			<li class="dynhover ui-state-default ui-corner-all" title="Update"><span class="ui-icon ui-icon-transferthick-e-w">&nbsp;</span></li>
//			<li class="dynhover ui-state-default ui-corner-all" title="Uninstall"><span class="ui-icon ui-icon-trash">&nbsp;</span></li>
//		</ul>
//	</td>
//	</tr>
//	 
	private static final long serialVersionUID = -6801334928121243975L;
	private BundleContext bundleContext;
	@Override
	public String getLabel() {
		return "scripting";
	}

	@Override
	public String getTitle() {
		return "Scripting services";
	}

	@Activate
	public void activate(BundleContext bc) {
		this.bundleContext = bc;
		
	}
	@Override
	protected void renderContent(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if(bundleContext==null) {
			throw new ServletException("Service failed");
		}
		String requestName = request.getParameter("script");
		PrintWriter writer = response.getWriter();
		try {
		if(requestName==null) {
				Collection<ServiceReference<Evaluator>> e = bundleContext.getServiceReferences(Evaluator.class, null);
				writer.write("<ul>");
				for (ServiceReference<Evaluator> serviceReference : e) {
					Evaluator ev = bundleContext.getService(serviceReference);
					String name = ev.getClass().getSimpleName();
					writer.write("<li><a href="+getLabel()+"?script="+name+">"+name+"</a></li>");
					bundleContext.ungetService(serviceReference);
				}
				writer.write("<ul>");
			
			} else {
				Collection<ServiceReference<Evaluator>> e = bundleContext
						.getServiceReferences(Evaluator.class, "(name="
								+ requestName + ")");
				ServiceReference<Evaluator> serviceReference = e.iterator().next();
				Evaluator ev = bundleContext.getService(serviceReference);
				try {
					Object result = ev.evaluate("Test input");
					writer.write("<strong>Result of calling script: "+requestName+" : " +result+"</strong>");
				} catch (ScriptException e1) {
					throw new ServletException("Error in script: ",e1);
				}
				
				bundleContext.ungetService(serviceReference);
			}
		} catch (InvalidSyntaxException e) {
			throw new ServletException("Service failed", e);
		}
	}

}
