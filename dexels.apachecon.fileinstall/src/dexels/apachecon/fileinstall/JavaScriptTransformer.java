package dexels.apachecon.fileinstall;

import java.io.File;

import org.apache.felix.fileinstall.ArtifactTransformer;
import org.osgi.service.component.annotations.Component;


@Component
public class JavaScriptTransformer implements ArtifactTransformer {

	@Override
	public boolean canHandle(File f) {
		return f.getName().endsWith(".js");
	}

	@Override
	public File transform(File artifact, File tmpDir) throws Exception {
		return null;
	}

}
