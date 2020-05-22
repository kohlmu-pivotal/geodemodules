package org.apache.geode.main

import org.apache.geode.module.service.ModuleService
import org.apache.geode.module.service.impl.JBossModuleServiceImpl
import org.apache.geode.service.SampleService
import org.jboss.modules.Module

class MainApp(private val moduleService: ModuleService = JBossModuleServiceImpl()) {
    private fun loadImplementationFromServiceLoader(clazz: Class<SampleService>) {
        try {
            val sampleServices = moduleService.loadService(clazz)
            sampleServices.forEach { println(it.value) }
        } catch (e: Exception) {
            println(e)
        }
    }

    companion object {
//        private const val SUB_MODULE_1_PATH = "/tamara2/users/ukohlmeyer/projects/geodemodules/sub-module1/target/submodule-1-1.0-SNAPSHOT.jar"
//        private const val SUB_MODULE_2_PATH = "/tamara2/users/ukohlmeyer/projects/geodemodules/sub-module2/target/sub-module2-1.0-SNAPSHOT.jar"
//        private const val SUB_MODULE_3_PATH = "/tamara2/users/ukohlmeyer/projects/geodemodules/sub-module3/target/sub-module3-1.0-SNAPSHOT.jar"
//        private const val SUB_MODULE_4_PATH = "/tamara2/users/ukohlmeyer/projects/geodemodules/sub-module4/target/sub-module4-1.0-SNAPSHOT.jar"
//        private const val SUB_MODULE_5_PATH = "/tamara2/users/ukohlmeyer/projects/geodemodules/sub-module5/target/sub-module5-1.0-SNAPSHOT.jar"
        private const val MANIFEST_JAR_PATH = "/tamara2/users/ukohlmeyer/projects/geodemodules/module-assembly/build/libs/geode-modules-dependencies.jar"


        @JvmStatic
        fun main(args: Array<String>) {
            val mainApp = MainApp()

            mainApp.registerModuleFromJar(MANIFEST_JAR_PATH,"manifestJarModule")

//            mainApp.registerModuleFromJar(SUB_MODULE_1_PATH, "submodule1")
//            mainApp.registerModuleFromJar(SUB_MODULE_2_PATH, "submodule2")
//            mainApp.registerModuleFromJar(SUB_MODULE_3_PATH, "submodule3")
//            mainApp.registerModuleFromJar(SUB_MODULE_4_PATH, "submodule4")
//            mainApp.registerModuleFromJar(SUB_MODULE_5_PATH, "submodule5", "submodule4")


            val manifestModule = mainApp.loadModule("manifestJarModule")

//            val subModule1 = mainApp.loadModule("submodule1")
//            val subModule2 = mainApp.loadModule("submodule2")
//            val subModule3 = mainApp.loadModule("submodule3")
//            val subModule4 = mainApp.loadModule("submodule4")
//            val subModule5 = mainApp.loadModule("submodule5")
//
//            testClassLeakage(subModule1)
//            testClassLeakage(subModule2)
//            testClassLeakage(subModule3)
//            testClassLeakage(subModule4)
//            testClassLeakage(subModule5)

            mainApp.loadImplementationFromServiceLoader(SampleService::class.java)

        }

        private fun testClassLeakage(module: Module) {
            checkClassAndLogError(module, "org.apache.geode.service.impl.SampleServiceImpl")
            checkClassAndLogError(module, "org.apache.geode.service.SampleService")
            checkClassAndLogError(module, "org.apache.geode.subService.SampleSubService")
            checkClassAndLogError(module, "org.apache.geode.subService.impl.SampleSubServiceImpl")
            checkClassAndLogError(module, "org.springframework.util.StringUtils")
            checkClassAndLogError(module, "org.apache.commons.lang3.StringUtils")
            checkClassAndLogError(module, "com.google.common.base.Strings")
            println("<< ------------------------ >>")
        }

        private fun checkClassAndLogError(module: Module, classString: String) {
            try {
                module.classLoader.loadClass(classString)
            } catch (e: Exception) {
                println("${module.name} cannot find $classString")
            }
        }
    }

    private fun loadModule(moduleName: String): Module = moduleService.loadModule(moduleName)


    private fun registerModuleFromJar(modulePath: String, name: String, vararg dependentModules: String) {
        moduleService.registerModuleFromJar(modulePath, name, *dependentModules)
    }
}


