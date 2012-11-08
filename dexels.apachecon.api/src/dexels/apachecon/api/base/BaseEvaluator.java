package dexels.apachecon.api.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.felix.mishell.OSGiScriptEngineManager;
import org.osgi.framework.BundleContext;

import dexels.apachecon.api.Evaluator;


public abstract class BaseEvaluator implements Evaluator {

	protected final String scriptName;
	private BundleContext bundleContext;
	private final String engineName;
	private final String extension;
	private final Map<String,Object> depedencies = new HashMap<String,Object>();

	public BaseEvaluator(String scriptName, String engineName, String extension) {
		this.scriptName = scriptName;
		this.engineName = engineName;
		this.extension = extension;
	}

	@Override
	public Object evaluate(Object o) throws ScriptException, IOException {
		ScriptEngineManager scriptEngineManager = new OSGiScriptEngineManager(bundleContext);
//		List<ScriptEngineFactory> l =  scriptEngineManager.getEngineFactories();
//		final ScriptEngineManager factory = new ScriptEngineManager();
	    final ScriptEngine engine = scriptEngineManager.getEngineByName(engineName);
	    Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
	    bindings.put("input",o);
	    bindings.putAll(depedencies);
//	    InputStream is = getClass().getClassLoader().getResourceAsStream("dexels/apachecon/"+scriptName+extension);
	    InputStream is = getClass().getResourceAsStream(scriptName+extension);
	    Reader r = new InputStreamReader(is);
	    Object result = engine.eval(r);
	    r.close();
	    return result;
	}

	public void activate(BundleContext bc) {
		this.bundleContext = bc;
	}

	protected void  clearResource(String name,Object resource) {
		depedencies.remove(name);
	}
	
	protected void  setResource(String name,Object resource) {
		depedencies.put(name,resource);
	}

}