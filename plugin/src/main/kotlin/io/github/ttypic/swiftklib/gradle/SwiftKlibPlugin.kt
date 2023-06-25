package io.github.ttypic.swiftklib.gradle

import io.github.ttypic.swiftklib.gradle.task.CompileSwiftTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess

const val EXTENSION_NAME = "swiftklib"

class SwiftKlibPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val objects: ObjectFactory = project.objects

        val swiftKlibEntries: NamedDomainObjectContainer<SwiftKlibEntry> =
            objects.domainObjectContainer(SwiftKlibEntry::class.java) { name ->
                objects.newInstance(SwiftKlibEntry::class.java, name)
            }

        project.extensions.add(EXTENSION_NAME, swiftKlibEntries)

        swiftKlibEntries.all { entry ->
            val name: String = entry.name

            val targetToTaskName = CompileTarget.values().associateWith {
                getTaskName(name, it)
            }

            targetToTaskName.entries.forEach { (target, taskName) ->
                tasks.register(
                    taskName,
                    CompileSwiftTask::class.java,
                    name,
                    target,
                    entry.pathProperty,
                    entry.packageNameProperty,
                    entry.minIosProperty,
                    entry.minMacosProperty,
                    entry.minTvosProperty,
                    entry.minWatchosProperty,
                )
            }
        }

        tasks.withType(CInteropProcess::class.java).configureEach { cinterop ->
            val cinteropTarget = CompileTarget.byKonanName(cinterop.konanTarget.name)
                ?: return@configureEach

            val taskName = getTaskName(cinterop.interopName, cinteropTarget)

            val task = tasks.withType(CompileSwiftTask::class.java).findByName(taskName)
                ?: return@configureEach

            cinterop.settings.defFile = task.defFile
            cinterop.dependsOn(task)
        }
    }
}

private fun getTaskName(cinteropName: String, cinteropTarget: CompileTarget) =
    "${EXTENSION_NAME}${cinteropName.capitalized()}${cinteropTarget.name.capitalized()}"
