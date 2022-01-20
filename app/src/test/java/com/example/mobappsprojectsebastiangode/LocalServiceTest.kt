package com.example.mobappsprojectsebastiangode

import android.provider.ContactsContract
import android.util.Log
import junit.framework.TestCase
import org.junit.Assert.*
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

    @Test
    fun checkGetContacts() {
        val names = ArrayList<LocalService.Contact>()

        //Create one Contact
        val id = "1"
        val name = "Test"
        val number = "112"
        names.add(LocalService.Contact(id, name, number))

        //Simulate API response that Contact having a second number
        val id2 = "1"
        val name2 = "Test"
        val number2 = "113"
        names.add(LocalService.Contact(id2, name2, number2))

        val outputList = ArrayList<LocalService.Contact>()

        // Call distinct function
        names.distinctBy { it.id }.forEach {
            outputList.add(it)
        }

        // Assert that contact is saved and does not exist twice in outputList but in names
        assertEquals(outputList[0].id, "1")
        assertEquals(outputList[0].name, "Test")
        assertEquals(outputList[0].number, "112")
        assertEquals(outputList.size, 1)
        assertEquals(names.size, 2)

    }
}