package io.github.ttypic.swiftklib.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import java.io.File
import javax.inject.Inject

abstract class SwiftKlibEntry @Inject constructor(
    val name: String,
    objects: ObjectFactory,
) {

    val path: Property<File> = objects.property(File::class.java)
    val packageName: Property<String> = objects.property(String::class.java)
    val minIos: Property<Int> = objects.property(Int::class.java)
    val minMacos: Property<Int> = objects.property(Int::class.java)
    val minTvos: Property<Int> = objects.property(Int::class.java)
    val minWatchos: Property<Int> = objects.property(Int::class.java)

    fun packageName(name: String) = packageName.set(name)
}
