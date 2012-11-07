package dexels.apachecon.builder;

import java.io.File;
import java.io.IOException;

public interface BundleBuilder {
	public File build(String scriptName, String engine, String extension) throws IOException;
}
