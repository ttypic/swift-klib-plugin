package com.ttypic.swiftklib.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithSimulatorTests
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile


class SwiftKlibPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val objects: ObjectFactory = project.objects

        val swiftKlibEntries: NamedDomainObjectContainer<SwiftKlibEntry> =
            objects.domainObjectContainer(SwiftKlibEntry::class.java) {
                    name -> objects.newInstance(SwiftKlibEntry::class.java, name)
            }

        project.extensions.add("swiftklib", swiftKlibEntries)

        val kotlinMultiplatformExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

        swiftKlibEntries.all { entry ->
            val name: String = entry.name
            // register task for all targets
            // add dependencies
        }
    }
}
