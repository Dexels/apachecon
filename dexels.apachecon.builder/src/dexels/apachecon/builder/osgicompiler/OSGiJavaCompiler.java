package dexels.apachecon.builder.osgicompiler;

import java.io.IOException;
import java.io.InputStream;

public interface OSGiJavaCompiler {
	public byte[] compile(String className, InputStream source) throws IOException;
}

