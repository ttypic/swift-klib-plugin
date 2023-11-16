package com.ttypic.objclibs

import com.ttypic.objclibs.greeting.*
import kotlin.test.*

class GreetingTest {

    @Test
    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    fun test() {
        assertEquals("HeLLo WorLd!", HelloWorld.helloWorld())
    }

}
