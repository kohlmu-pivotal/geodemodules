package org.apache.geode.module.service.impl

import org.jboss.modules.ModuleFinder
import org.jboss.modules.ModuleLoader
import org.jboss.modules.ModuleSpec

class DelegateModuleFinder : ModuleFinder {

    private val moduleFinders: MutableList<ModuleFinder> = mutableListOf()

    override fun findModule(name: String, delegateLoader: ModuleLoader): ModuleSpec? {
        val toList = moduleFinders.toList()
        val map = toList.mapNotNull { moduleFinder -> moduleFinder.findModule(name, delegateLoader as TestModuleLoader) }
        return map.firstOrNull()
    }
    fun addModuleFinder(moduleFinder: ModuleFinder) =
            moduleFinders.add(moduleFinder)
}