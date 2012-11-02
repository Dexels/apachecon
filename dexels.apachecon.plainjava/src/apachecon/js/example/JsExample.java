package apachecon.js.example;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import apachecon.Evaluator;

public class JsExample implements Evaluator {

	private final String scriptName;
	
	public JsExample(String name) {
		this.scriptName = name;
	}
	
	@Override
	public Object evaluate(Object o) throws ScriptException, IOException {
		final ScriptEngineManager factory = new ScriptEngineManager();
	    final ScriptEngine engine = factory.getEngineByName("js");
	    Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
	    bindings.put("input",o);
	    Reader r = new FileReader("./scripts/"+scriptName);
	    Object result = engine.eval(r);
	    r.close();
	    return result;
	}

}
