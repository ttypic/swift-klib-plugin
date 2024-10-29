package io.github.ttypic.swiftklib.gradle

import io.github.ttypic.swiftklib.gradle.api.SwiftKlibEntry
import io.github.ttypic.swiftklib.gradle.api.SwiftPackageConfiguration
import io.github.ttypic.swiftklib.gradle.internal.SwiftPackageConfigurationImpl
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import java.io.File
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal abstract class SwiftKlibEntryImpl @Inject constructor(
    val name: String,
    private val objects: ObjectFactory,
) : SwiftKlibEntry {
    val _path: Property<File> = objects.property(File::class.java)
    val _packageName: Property<String> = objects.property(String::class.java)
    val _minIos: Property<Int> = objects.property(Int::class.java).convention(13)
    val _minMacos: Property<Int> = objects.property(Int::class.java).convention(11)
    val _minTvos: Property<Int> = objects.property(Int::class.java).convention(13)
    val _minWatchos: Property<Int> = objects.property(Int::class.java).convention(8)

    override var path: File by _path.bind()
    override var minIos: Int by _minIos.bind()
    override var minMacos: Int by _minMacos.bind()
    override var minTvos: Int by _minTvos.bind()
    override var minWatchos: Int by _minWatchos.bind()

    internal val dependencyHandler = SwiftPackageConfigurationImpl(objects)

    override fun packageName(name: String) = _packageName.set(name)

    override fun dependencies(configuration: SwiftPackageConfiguration.() -> Unit) {
        dependencyHandler.apply(configuration)
    }
}

fun <T> Property<T>.bind(): ReadWriteProperty<Any, T> {
    return object : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return get()
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            set(value)
        }
    }
}
