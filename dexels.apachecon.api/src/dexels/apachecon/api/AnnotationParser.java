package dexels.apachecon.api;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface AnnotationParser {
	public Map<String, String> parseAnnotations(File scriptFile) throws IOException;
}
