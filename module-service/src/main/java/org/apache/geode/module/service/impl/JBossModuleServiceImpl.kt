package org.apache.geode.module.service.impl

import org.jboss.modules.JarModuleFinder
import org.apache.geode.module.service.ModuleService
import org.apache.geode.service.SampleService
import org.jboss.modules.*
import java.io.File
import java.util.*
import java.util.jar.JarFile

class JBossModuleServiceImpl(private val moduleLoader: TestModuleLoader = TestModuleLoader()) : ModuleService {
    private val moduleMap: MutableMap<String, Module> = mutableMapOf()
    private val modulesList: MutableList<String> = mutableListOf();

    override fun loadClass(className: String): Class<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loadService(clazz: Class<out SampleService>): List<SampleService> {
        val returnList = mutableListOf<SampleService>()
        if (moduleMap.isEmpty()) {
            moduleLoader.getRegisteredModuleSpecNames().forEach { moduleName -> moduleMap[moduleName] = loadModule(moduleName) }
        }
        moduleMap.values.forEach { module ->
            ServiceLoader.load(clazz, module.classLoader).forEach {
                        it.init(this)
                        returnList.add(it)
                    }
        }
        return returnList
    }

    override fun registerModuleFromJar(jarPath: String, moduleName: String, vararg dependentComponents: String) {
        moduleLoader.addModuleJarFile(moduleName, JarFile(File(jarPath), true))
//        ResourceLoaderSpec.createResourceLoaderSpec(ResourceLoaders.createJarResourceLoader(moduleName, JarFile(File(jarPath), true)))
//        val builder: ModuleSpec.Builder = ModuleSpec.build(moduleName)
//
//        // Add the module's own content
//        builder.addDependency(LocalDependencySpecBuilder()
//                .setExportFilter(PathFilters.isOrIsChildOf("org/apache/geode"))
////                .setImportFilter(PathFilters.isOrIsChildOf("org/apache/geode"))
//                .setImportServices(true)
//                .setExport(true)
//                .build())
//
//        dependentComponents.forEach {
//            builder.addDependency(
//                    ModuleDependencySpecBuilder()
////                            .setImportFilter(PathFilters.isOrIsChildOf("org/apache/geode"))
////                            .setImportServices(true)
//                            .setName(it)
//                            .build())
//        }
//
//        builder.addDependency(DependencySpec
//                .createSystemDependencySpec(PathUtils.getPathSet(null)))
//
//        builder.addResourceRoot(ResourceLoaderSpec.createResourceLoaderSpec(ResourceLoaders.createJarResourceLoader(moduleName, JarFile(File(jarPath), true))))
//        val moduleSpec = builder.create()
//        moduleLoader.addModuleSpec(moduleSpec)
//
        modulesList.add(moduleName);
    }

    override fun loadModule(moduleName: String): Module = moduleLoader.loadModule(moduleName)

    private fun loadImplementationFromServiceLoader(module: Module) {

    }
}
