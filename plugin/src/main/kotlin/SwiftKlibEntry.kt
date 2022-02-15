package com.ttypic.swiftklib.gradle

import org.gradle.api.provider.Property
import javax.inject.Inject


abstract class SwiftKlibEntry @Inject constructor(val name: String) {
    abstract val src: Property<String>
    abstract var targets: Property<List<String>>
}