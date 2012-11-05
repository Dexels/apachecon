package dexels.apachecon.api.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

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

	public BaseEvaluator(String scriptName, String engineName, String extension) {
		this.scriptName = scriptName;
		this.engineName = engineName;
		this.extension = extension;
	}

	@Override
	public Object evaluate(Object o) throws ScriptException, IOException {
		ScriptEngineManager scriptEngineManager = new OSGiScriptEngineManager(bundleContext);
		List<ScriptEngineFactory> l =  scriptEngineManager.getEngineFactories();
		for (ScriptEngineFactory scriptEngineFactory : l) {
			System.err.println("engine: "+scriptEngineFactory.getEngineName());
		}
//		final ScriptEngineManager factory = new ScriptEngineManager();
	    final ScriptEngine engine = scriptEngineManager.getEngineByName(engineName);
	    Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
	    bindings.put("input",o);
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

}