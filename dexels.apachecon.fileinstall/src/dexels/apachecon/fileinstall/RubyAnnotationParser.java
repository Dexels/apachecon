package dexels.apachecon.fileinstall;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

import dexels.apachecon.api.AnnotationParser;

@Component(property={"language=ruby"})
public class RubyAnnotationParser implements AnnotationParser {

	@Override
	public Map<String,String> parseAnnotations(File scriptFile) throws IOException {
		FileReader fr = new FileReader(scriptFile);
		
		BufferedReader buffer=new BufferedReader(fr);
		String line = null;
		Map<String,String> result = new HashMap<String,String>();
		do {
			line = buffer.readLine();
			if(line!=null && line.startsWith("##//")) {
				String actual = line.substring(4);
				int eq = actual.indexOf("=");
				String resource = actual.substring(0,eq);
				String target = actual.substring(eq+1,actual.length());
				result.put(resource,target);
			}
		} while (line!=null);
		buffer.close();
		return result;
	}

}
