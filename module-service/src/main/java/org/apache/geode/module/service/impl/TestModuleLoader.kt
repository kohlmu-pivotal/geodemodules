package org.apache.geode.module.service.impl

import org.jboss.modules.DelegatingModuleLoader
import org.jboss.modules.JarModuleFinder
import org.jboss.modules.Module
import org.jboss.modules.ModuleSpec
import java.util.jar.JarFile

class TestModuleLoader(private val delegateModuleFinder: DelegateModuleFinder = DelegateModuleFinder())
    : DelegatingModuleLoader(Module.getSystemModuleLoader(), arrayOf(delegateModuleFinder)) {
    private val moduleSpecs: MutableMap<String, ModuleSpec> = mutableMapOf()

    fun findModule2(name: String): ModuleSpec? = findModule(name)

    fun loadModule2(name: String): Module = loadModule(name)

    override fun findModule(name: String): ModuleSpec? =
            moduleSpecs.toMap().asSequence().filter { it.key == name }.firstOrNull()
                    ?.value
                    ?: run { delegateModuleFinder.findModule(name, this) }
                            ?.also { moduleSpec ->
                                this.moduleSpecs[name] = moduleSpec
                            }


    fun addModuleSpec(moduleSpec: ModuleSpec) {
        moduleSpecs[moduleSpec.name] = moduleSpec
    }

    override fun toString(): String {
        return "test@" + System.identityHashCode(this)
    }

    fun addModuleJarFile(name: String, jarFile: JarFile) {
        delegateModuleFinder.addModuleFinder(JarModuleFinder(name, jarFile))
    }

    fun getRegisteredModuleSpecNames():List<String> = moduleSpecs.keys.toList()
}