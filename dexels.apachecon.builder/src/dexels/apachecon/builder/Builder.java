package dexels.apachecon.builder;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import dexels.apachecon.builder.osgicompiler.OSGiJavaCompiler;

@Component
public class Builder {
//    @Reference(name = "javaCompiler", referenceInterface = PreferencesService.class,  
//            cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC) 
    
    private OSGiJavaCompiler javaCompiler;
	
}
