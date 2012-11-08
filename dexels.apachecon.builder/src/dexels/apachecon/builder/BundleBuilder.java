package dexels.apachecon.builder;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface BundleBuilder {
	public File build(String scriptName, String engine, String extension,Map<String,String> annotations) throws IOException;
}
