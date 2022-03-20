package com.example.mobappsprojectsebastiangode

import android.os.Environment
import android.provider.ContactsContract
import android.util.Log
import com.google.gson.Gson
import junit.framework.TestCase
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.lang.Math.random
import java.util.*

class LocalServiceTest {
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

    @Test
    fun checkGenerateJson(){
        // Create a dummy contact as data class
        val contact = LocalService.Contact("1","Test","112")

        // Convert it to JSON like the LocalService does
        val gson = Gson()
        val jsonString = gson.toJson(contact)

        // Assert that the JSON string is as expected
        val expectedString = "{\"id\":\"1\",\"name\":\"Test\",\"number\":\"112\"}"
        assertEquals(jsonString, expectedString)

    }

    @Test
    fun checkParseJson() {
        // Create a dummy JSON
        val jsonString = "{\"id\":\"1\",\"name\":\"Test\",\"number\":\"112\"}"

        // Generate contact data class from JSON
        val gson = Gson()
        val outputContact = gson.fromJson(jsonString, LocalService.Contact::class.java)

        // Check that the parsing was correct
        assertEquals(outputContact.id, "1")
        assertEquals(outputContact.name, "Test")
        assertEquals(outputContact.number, "112")
    }

    @Test
    fun checkFileOperations() {
        val jsonString = "{\"id\":\"1\",\"name\":\"Test\",\"number\":\"112\"}"

        // Write to a new File
        val file = File("test.txt")
        file.createNewFile()
        file.writeText(jsonString)

        // Read from the written file
        val fileRead = File("test.txt")
        val fileReadResult = fileRead.readText()

        // Check that File could be read and has the same output as input
        assertEquals(fileReadResult, jsonString)
    }
}