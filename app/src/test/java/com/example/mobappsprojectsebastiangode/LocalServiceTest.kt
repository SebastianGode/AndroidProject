package com.example.mobappsprojectsebastiangode

import junit.framework.TestCase
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.Math.random
import java.util.*

class LocalServiceTest {
    @Test
    fun checkRandomNumber() {
        val testGenerator = Random()
        val output = testGenerator.nextInt(100)
        assertTrue(output >= 0 && output < 100)
    }
}