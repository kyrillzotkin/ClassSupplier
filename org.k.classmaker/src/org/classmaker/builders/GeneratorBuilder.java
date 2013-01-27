package org.classmaker.builders;

import java.util.Map;

import org.classmaker.Bundle;
import org.classmaker.ClassMaker;
import org.classmaker.codegen.EcoreGenerator;
import org.classmaker.codegen.Generator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class GeneratorBuilder extends AbstractBuilder {

	public static final String BUILDER_ID = ClassMaker.PLUGIN_ID + '.'
			+ "generatorBuilder";

	protected Generator generator = new EcoreGenerator();

	@Override
	protected IProject[] build(int kind, Map<String, String> args,
			IProgressMonitor monitor) throws CoreException {
		if (kind != FULL_BUILD)
			return null;
		IProgressMonitor oldMonitor = ClassMaker.getInstance().monitor();
		ClassMaker.getInstance().setMonitor(monitor);
		Bundle bundle = getBundle(getProject().getName());
		generator.setResourceSet(resourceSet);
		generator.generate(bundle, getProject(), getRule(kind, args));
		ClassMaker.getInstance().setMonitor(oldMonitor);
		return null;
	}

}