package com.example.mobappsprojectsebastiangode


import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import android.provider.ContactsContract
import android.util.JsonWriter
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import com.google.gson.Gson
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class LocalService : Service() {

    // Binder given to clients
    private val binder = LocalBinder()

    // Random number generator
    private val mGenerator = Random()

    /** method for clients  */
    val randomNumber: Int
        get() = mGenerator.nextInt(100)

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): LocalService = this@LocalService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    // Create a data class for the contact list
    data class Contact(
        val id : String,
        val name : String,
        val number : String)

    // Suppress Warning for cursor that it needs to be bigger than 0
    @SuppressLint("Range")
    // private Function to get contact list as array
    private fun getNamePhoneDetails(): ArrayList<Contact> {
        val names = ArrayList<Contact>()
        val cr = contentResolver
        // Query for the contact list
        val cur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
            null, null, null)
        if (cur!!.count > 0) {
            // Add all contact Names+Numbers+ID to array
            while (cur.moveToNext()) {
                val id = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NAME_RAW_CONTACT_ID))
                val name = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                names.add(Contact(id , name , number))
            }
        }
        // Close cursor
        cur.close()
        return names
    }

    // Function to filter double entries in Contact list due to more than one phone number saved
    fun getContacts(): ArrayList<Contact> {
        val list = getNamePhoneDetails()

        // Create a new list to only store unique contacts
        val outputList = ArrayList<Contact>()

        // Function to remove double contacts
        list.distinctBy { it.id }.forEach {
            outputList.add(it)
        }

        // returns data class / object Contact with all contacts which exist on the phone
        return outputList
    }

    fun generateJson(Contact : Contact): String {
        val gson = Gson()
        val jsonstring = gson.toJson(Contact)
        return jsonstring
    }

    // method for Reading the JSON out of the file
    fun readFile(): String {
        val path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val directory = File(path, "contacts")
        val file = File(directory, "exportContact.json")
        return file.readText()
    }
    // method for writing the json file to phone storage
    fun writeFile(selectedContactJson: String): String {
        val path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val directory = File(path, "contacts")
        directory.mkdirs()
        val file = File(directory, "exportContact.json")
        file.createNewFile()
        file.writeText(selectedContactJson)
        // Return the directory
        return directory.toString()
    }
    // method to import a contact from a contact data class
    fun importContact(Contact: Contact) {
        val intent = Intent(ContactsContract.Intents.Insert.ACTION)
        intent.type = ContactsContract.RawContacts.CONTENT_TYPE
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(ContactsContract.Intents.Insert.NAME, Contact.name)
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, Contact.number)
        startActivity(intent)
    }
}


//fun getNameEmailDetails(): ArrayList<String>? {
//    val names = ArrayList<String>()
//    val cr: ContentResolver = getContentResolver()
//    val cur: Cursor? = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
//    if (cur.getCount() > 0) {
//        while (cur.moveToNext()) {
//            val id: String = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
//            val cur1: Cursor? = cr.query(
//                ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
//                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", arrayOf(id), null
//            )
//            while (cur1.moveToNext()) {
//                //to get the contact names
//                val name: String =
//                    cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
//                Log.e("Name :", name)
//                val email: String =
//                    cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
//                Log.e("Email", email)
//                if (email != null) {
//                    names.add(name)
//                }
//            }
//            cur1.close()
//        }
//    }
//    return names
//}




//private fun doIt() {
//    val contentResolver: ContentResolver = getContentResolver()
//    // IDs und Namen aller sichtbaren Kontakte ermitteln
//    val mainQueryProjection = arrayOf(
//        ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME
//    )
//    val mainQuerySelection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = ?"
//    val mainQuerySelectionArgs = arrayOf("1")
//    val mainQueryCursor: Cursor? = contentResolver.query(
//        ContactsContract.Contacts.CONTENT_URI, mainQueryProjection, mainQuerySelection,
//        mainQuerySelectionArgs, null
//    )
//    // Trefferliste abarbeiten...
//    if (mainQueryCursor != null) {
//        while (mainQueryCursor.moveToNext()) {
//            val contactId: String = mainQueryCursor.getString(0)
//            val displayName: String = mainQueryCursor.getString(1)
//            tv.append("===> $displayName ($contactId)\n")
//        }
//        mainQueryCursor.close()
//    }
//}
