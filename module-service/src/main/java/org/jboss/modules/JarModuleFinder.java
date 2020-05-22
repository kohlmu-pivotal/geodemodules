/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.modules;

import java.io.File;
import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.geode.module.service.impl.TestModuleLoader;
import org.jboss.modules.filter.PathFilters;

/**
 * A module finder which uses a JAR file as a module repository.
 */
public final class JarModuleFinder implements ModuleFinder {

	private final String myName;
	private final JarFile jarFile;
	private final AccessControlContext context;

	private static final String DEPENDENCIES = "Dependencies";
	private static final String MODULES_DIR = "modules";
	private static final String[] NO_STRINGS = new String[0];
	private static final String OPTIONAL = "optional";
	private static final String EXPORT = "export";

	/**
	 * Construct a new instance.
	 *
	 * @param myIdentifier the identifier to use for the JAR itself
	 * @param jarFile the JAR file to encapsulate
	 */
	public JarModuleFinder(final ModuleIdentifier myIdentifier, final JarFile jarFile) {
		this(myIdentifier.toString(), jarFile);
	}

	/**
	 * Construct a new instance.
	 *
	 * @param myName the name to use for the JAR itself
	 * @param jarFile the JAR file to encapsulate
	 */
	public JarModuleFinder(final String myName, final JarFile jarFile) {
		this.myName = myName;
		this.jarFile = jarFile;
		context = AccessController.getContext();
	}

	public ModuleSpec findModule(final String name, final ModuleLoader delegateLoader) throws ModuleLoadException {
		if (name.equals(myName)) {
			// special root JAR module
			Manifest manifest;
			int lastIndexOfPathSeparator = jarFile.getName().lastIndexOf(File.separator);

			String jarFilePath = jarFile.getName().substring(0, lastIndexOfPathSeparator);

			try {
				manifest = jarFile.getManifest();
			}
			catch (IOException e) {
				throw new ModuleLoadException("Failed to load MANIFEST from JAR", e);
			}
			ModuleSpec.Builder builder = ModuleSpec.build(name);

			builder.addDependency(new LocalDependencySpecBuilder()
				.setExportFilter(PathFilters.isOrIsChildOf("org/apache/geode"))
				//                .setImportFilter(PathFilters.isOrIsChildOf("org/apache/geode"))
				.setImportServices(true)
				.setExport(true)
				.build());

			Attributes mainAttributes = manifest.getMainAttributes();
			String mainClass = mainAttributes.getValue(Attributes.Name.MAIN_CLASS);
			if (mainClass != null) {
				builder.setMainClass(mainClass);
			}
			String classPath = mainAttributes.getValue(Attributes.Name.CLASS_PATH);
			String dependencies = mainAttributes.getValue(DEPENDENCIES);
			//MultiplePathFilterBuilder pathFilterBuilder = PathFilters.multiplePathFilterBuilder(true);
			//pathFilterBuilder.addFilter(PathFilters.is(MODULES_DIR), false);
			//pathFilterBuilder.addFilter(PathFilters.isChildOf(MODULES_DIR), false);
			builder.addResourceRoot(ResourceLoaderSpec
				.createResourceLoaderSpec(ResourceLoaders.createJarResourceLoader(jarFile)));
					//,pathFilterBuilder.create()));
			String[] classPathEntries = classPath == null ? NO_STRINGS : classPath.split("\\s+");
			for (String entry : classPathEntries) {
				if (!entry.isEmpty()) {
					if (entry.startsWith("../") || entry.startsWith("./") || entry.startsWith("/") || entry
						.contains("/../")) {
						// invalid
						continue;
					}
					File root = new File(jarFilePath + File.separator + entry);

					if (entry.endsWith("/")) {
						// directory reference
						builder.addResourceRoot(ResourceLoaderSpec
							.createResourceLoaderSpec(ResourceLoaders.createPathResourceLoader(root.toPath())));
					}
					else {
						// assume a JAR
						JarFile childJarFile;
						try {
							childJarFile = new JarFile(root, true);
						}
						catch (IOException e) {
							// ignore and continue
							continue;
						}
						builder.addResourceRoot(ResourceLoaderSpec
							.createResourceLoaderSpec(ResourceLoaders.createJarResourceLoader(childJarFile)));
					}
				}
			}
			processDependecies(builder, dependencies, (TestModuleLoader) delegateLoader,jarFilePath);
			builder.addDependency(DependencySpec.createSystemDependencySpec(JDKPaths.JDK));
			builder.addDependency(DependencySpec.createLocalDependencySpec());
			return builder.create();
		}
		return null;
	}

	private void processDependecies(ModuleSpec.Builder builder, String dependencies,TestModuleLoader moduleLoader,String rootPath) {
		String[] dependencyEntries = dependencies == null ? NO_STRINGS : dependencies.split(" ");
		for (String dependencyEntry : dependencyEntries) {
			boolean optional = false;
			boolean export = false;
			dependencyEntry = dependencyEntry.trim();
			if (!dependencyEntry.isEmpty()) {

				String moduleName = dependencyEntry.substring(0,dependencyEntry.lastIndexOf(".jar"));

				try {
					ModuleSpec moduleSpec = moduleLoader.findModule2(moduleName);
					if(moduleSpec == null)
					{
						moduleLoader.addModuleJarFile(moduleName,new JarFile(new File(rootPath+File.separator+dependencyEntry),true));
						moduleLoader.findModule2(moduleName);
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}

				//for (int i = 1; i < fields.length; i++) {
				//	String field = fields[i];
				//	if (field.equals(OPTIONAL)) {
				//		optional = true;
				//	}
				//	else if (field.equals(EXPORT)) {
						export = true;
					//}
					// else ignored
				//}
				builder.addDependency(new ModuleDependencySpecBuilder()
					.setName(moduleName)
					.setExport(export)
					.setOptional(optional)
					.build());
			}
		}
	}
}
