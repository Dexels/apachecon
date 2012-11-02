package apachecon;

import java.io.IOException;

import javax.script.ScriptException;

public interface Evaluator {
	public Object evaluate(Object o) throws ScriptException, IOException;
}
