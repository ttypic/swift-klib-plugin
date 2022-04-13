package com.ttypic.swiftklib.gradle

import com.ttypic.swiftklib.gradle.task.CompileSwift
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess

class SwiftKlibPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val objects: ObjectFactory = project.objects

        val swiftKlibEntries: NamedDomainObjectContainer<SwiftKlibEntry> =
            objects.domainObjectContainer(SwiftKlibEntry::class.java) { name ->
                objects.newInstance(SwiftKlibEntry::class.java, name)
            }

        project.extensions.add("swiftklib", swiftKlibEntries)

        swiftKlibEntries.all { entry ->
            val name: String = entry.name

            val targetToTaskName = CompileTarget.values().associateWith {
                "swiftklib${name.capitalized()}${it.name.capitalized()}"
            }

            targetToTaskName.entries.forEach { (target, taskName) ->
                tasks.register(
                    taskName,
                    CompileSwift::class.java,
                    name,
                    target,
                    entry.path,
                    entry.packageNameProperty.get(),
                )
            }
        }

        tasks.withType(CInteropProcess::class.java).configureEach { cinterop ->
            val cinteropTarget = CompileTarget.byKonanName(cinterop.konanTarget.name)
                ?: return@configureEach
            val taskName =
                "swiftklib${cinterop.interopName.capitalized()}${cinteropTarget.name.capitalized()}"
            val task = (tasks.findByName(taskName) as CompileSwift?) ?: return@configureEach

            cinterop.settings.defFile = task.defFile
            cinterop.dependsOn(task)
        }
    }
}
