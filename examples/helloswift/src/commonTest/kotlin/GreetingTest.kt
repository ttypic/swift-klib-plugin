package com.ttypic.objclibs

import com.ttypic.objclibs.greeting.*
import kotlin.test.*

class GreetingTest {

    @Test
    fun test() {
        assertEquals("HeLLo WorLd!", HelloWorld.helloWorld())
    }

}
