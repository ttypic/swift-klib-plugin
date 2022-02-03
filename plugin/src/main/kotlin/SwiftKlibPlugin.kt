package com.ttypic.swiftklib.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.model.ObjectFactory
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMultiplatformPlugin
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithSimulatorTests
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile


class SwiftKlibPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // project.plugins.apply(KotlinMultiplatformPlugin::class.java)

        val objects: ObjectFactory = project.objects

        val swiftKlibEntries: NamedDomainObjectContainer<SwiftKlibEntry> =
            objects.domainObjectContainer(SwiftKlibEntry::class.java) {
                    name -> objects.newInstance(SwiftKlibEntry::class.java, name)
            }

        project.extensions.add("swiftklib", swiftKlibEntries)

        val kotlinMultiplatformExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

        swiftKlibEntries.all { entry ->
            val name: String = entry.name
            println("\n\n\n\n $name - ${entry.targets} \n\n\n\n")
            val nativeTarget = kotlinMultiplatformExtension.targets.getByName(entry.targets!!) as KotlinNativeTargetWithSimulatorTests
            nativeTarget.compilations.getByName("main").cinterops.getByName("SwiftChachaPoly") { settings ->
                println("\n\n\n\n${settings.interopProcessingTaskName}\n\n\n\n\n\n\n")
            }
            tasks.withType(KotlinNativeCompile::class.java).de
//            entry.targets.get().forEach {
//                val nativeTarget = kotlinMultiplatformExtension.targets.getByName(it) as KotlinNativeTargetWithSimulatorTests
//                nativeTarget.compilations.getByName("main").cinterops.getByName("SwiftChachaPoly") { settings ->
//                    println("\n\n\n\n${settings.interopProcessingTaskName}\n\n\n\n\n\n\n")
//                    project.tasks.register(
//                        "tempTask",
//                        Task::class.java
//                    ) { task ->
//                        tasks.findByPath(settings.interopProcessingTaskName)?.dependsOn(task)
//                        println("\n\n\n\nI am alive\n\n\n\n\n\n\n")
//                    }
//                    // tasks.withType(CInteropProcess::class.java)
//                    // tasks.withType(KotlinNativeCompile::class.java)
//                }
//            }

        }
    }
}
