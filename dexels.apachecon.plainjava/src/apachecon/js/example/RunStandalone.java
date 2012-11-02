package apachecon.js.example;

import java.io.IOException;

import javax.script.ScriptException;

import apachecon.Evaluator;

public class RunStandalone {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ScriptException 
	 */
	public static void main(String[] args) throws ScriptException, IOException {
		Evaluator e = new JsExample("JsExample.js");
		System.err.println("result: "+e.evaluate(3));
	}

}
