package dexels.apachecon.fileinstall;

import java.io.File;

import org.apache.felix.fileinstall.ArtifactListener;
import org.apache.felix.fileinstall.ArtifactTransformer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import dexels.apachecon.builder.BundleBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;

@Component(configurationPolicy=ConfigurationPolicy.IGNORE)
public class RubyTransformer implements ArtifactTransformer, ArtifactListener {

	private BundleBuilder builder = null;
	private final static Logger logger = LoggerFactory.getLogger(RubyTransformer.class);
	@Reference
	public void setBundleBuilder(BundleBuilder b) {
		this.builder = b;
	}
	
	@Override
	public boolean canHandle(File f) {
		return f.getName().endsWith(".rb");
	}

	@Override
	public File transform(File artifact, File tmpDir) throws Exception {
		try {
			logger.debug("transformer called. Artifact: "+artifact.getAbsolutePath()+" tmp: "+tmpDir.getAbsolutePath()); 
			String[] parts = artifact.getName().split("\\.");
			File bundle = builder.build(parts[0], "ruby", "."+parts[1]);
			logger.debug("Bundle created: "+bundle);
			return bundle;
		} catch (Throwable e) {
			logger.error("Error performing transform:",e);
		}
		return null;
	}

}
