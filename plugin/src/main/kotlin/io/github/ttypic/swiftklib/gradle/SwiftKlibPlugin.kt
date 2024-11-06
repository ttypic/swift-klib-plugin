package io.github.ttypic.swiftklib.gradle

import io.github.ttypic.swiftklib.gradle.api.SwiftKlibEntry
import io.github.ttypic.swiftklib.gradle.task.CompileSwiftTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.reflect.TypeOf
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.konan.target.HostManager
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

const val EXTENSION_NAME = "swiftklib"

@OptIn(ExperimentalStdlibApi::class)
@Suppress("unused")
class SwiftKlibPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val objects: ObjectFactory = project.objects

        val swiftKlibEntries: NamedDomainObjectContainer<out SwiftKlibEntryImpl> =
            objects.domainObjectContainer(SwiftKlibEntryImpl::class.java) { name ->
                objects.newInstance(SwiftKlibEntryImpl::class.java, name)
            }

        val type = TypeOf.typeOf<NamedDomainObjectContainer<out SwiftKlibEntry>>(
            typeOf<NamedDomainObjectContainer<SwiftKlibEntry>>().javaType
        )

        project.extensions.add(type, EXTENSION_NAME, swiftKlibEntries)

        if (!HostManager.hostIsMac) {
            logger.warn("Current host OS is not macOS. Disabling SwiftKlib plugin")
            return
        }

        swiftKlibEntries.all { entry ->
            val name: String = entry.name

            val targetToTaskName = CompileTarget.entries.associateWith {
                getTaskName(name, it)
            }

            val buildDir = project.layout.buildDirectory.asFile.get().absolutePath
            targetToTaskName.entries.forEach { (target, taskName) ->
                tasks.register(
                    taskName,
                    CompileSwiftTask::class.java,
                    project.hasProperty("swiftklibDebug"),
                    name,
                    target,
                    buildDir,
                    entry._path,
                    entry._packageName,
                    entry._minIos,
                    entry._minMacos,
                    entry._minTvos,
                    entry._minWatchos,
                    entry._toolsVersion
                ).configure {
                    it.dependenciesProperty = entry.dependencyHandler.dependencies
                }
            }
        }

        tasks.withType(CInteropProcess::class.java).configureEach { cinterop ->
            val cinteropTarget = CompileTarget.byKonanName(cinterop.konanTarget.name)
                ?: return@configureEach

            val taskName = getTaskName(cinterop.interopName, cinteropTarget)

            val task = tasks.withType(CompileSwiftTask::class.java).findByName(taskName)
                ?: return@configureEach

            cinterop.settings.definitionFile.set(task.defFile)
            cinterop.dependsOn(task)
        }
    }
}

private fun getTaskName(cinteropName: String, cinteropTarget: CompileTarget) =
    "${EXTENSION_NAME}${cinteropName.capitalized()}${cinteropTarget.name.capitalized()}"
