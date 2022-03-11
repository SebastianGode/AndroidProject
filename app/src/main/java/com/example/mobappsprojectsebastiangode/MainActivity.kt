package com.example.mobappsprojectsebastiangode

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.os.Environment
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.TextView
import androidx.core.view.forEach
import com.google.gson.Gson
import java.io.File


class MainActivity : AppCompatActivity() {

    lateinit var toggle : ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout

    private lateinit var mService: LocalService
    private var mBound: Boolean = false

    private var permissionReadContact: Boolean = false
    private var permissionWriteFiles: Boolean = false

    var selectedContactJson: String = ""


    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as LocalService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        val navView : NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {


            // Highlight the current selected Item in NavBar
            it.isChecked = true

            // Switch Fragments when user clicks on Items in NavDrawer
            when(it.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment(), it.title.toString())
                R.id.nav_edit -> replaceFragment(EditFragment(), it.title.toString())
                R.id.nav_import -> replaceFragment(ImportFragment(), it.title.toString())
                R.id.nav_export -> replaceFragment(ExportFragment(), it.title.toString())
                R.id.nav_share -> replaceFragment(ShareFragment(), it.title.toString())
            }

            true
        }



        // Default Page is Home
        replaceFragment(HomeFragment(), getString(R.string.home))
    }

    // Method for replacing fragments
    private fun replaceFragment(fragment: Fragment, title : String) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()

        // Close Drawer when user clicks on it
        drawerLayout.closeDrawers()

        // Set the correct title of current fragment
        setTitle(title)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(toggle.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        // Bind to LocalService
        Intent(this, LocalService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        // Check for Permissions and request if necessary
        alertContactPermission()
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        mBound = false
    }

    /** Called when a button is clicked (the button in the layout file attaches to
     * this method with the android:onClick attribute) */
    // It needs an View parameter to work, but I don't need it in code. So suppressing the warning.
    fun onContactListButtonClick(@Suppress("UNUSED_PARAMETER") v: View) {
        // If boundService connected start the actual activity
        if (mBound) {
            // Check for Permissions and request if necessary
            alertContactPermission()
            // Only start Read Contacts when permission granted, else app will crash
            if (permissionReadContact) {
                // get all contacts which have an email or phone number
                val names = mService.getContacts()
                listContacts(names)
            }

        }


    }

    // Create PermissionLauncher to request permissions if necessary
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            permissionReadContact = isGranted
        }

    // Check for permissions and call requestPermissionLauncher to ask user to get permission
    private fun onClickRequestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission granted, so set check to true
                permissionReadContact = true
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_CONTACTS
            ) -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_CONTACTS
                )
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_CONTACTS
                )
            }
        }
    }

    private fun alertContactPermission() {
        // Create AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.notice)
        // Set message
        builder.setMessage(R.string.alertContact)
        // Add an okay button and call the Request Permissions on clicking it
        builder.setNeutralButton(R.string.ok){ _,_ ->
            onClickRequestPermission()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        // Show the Dialog when Permission isn't granted
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_DENIED) {
            alertDialog.show()
        }
        else {
            permissionReadContact = true
        }

    }

    private fun listContacts(contacts: ArrayList<LocalService.Contact>) {
        // Create arrayAdapter to display a list
        val arrayAdapter: ArrayAdapter<*>

        // data class for mapping contactID to listID
        data class IdMap(
            val list_id: String,
            val contact_id: String,
        )

        // Simplify the data class LocalService.Contact to a String and map contactID to listID
        val contactSimple = ArrayList<String>()
        val idMapping = ArrayList<IdMap>()
        for (i in contacts.indices) {
            contactSimple.add(contacts[i].name + "\n" + contacts[i].number)
            idMapping.add(IdMap(i.toString(), contacts[i].id))
        }

        // Get the listView on the Home Fragment
        val contactList = findViewById<ListView>(R.id.listViewContacts)
        // Create IDs for each element
        val id : Int = R.id.txtListElement

        // Fill the ListView with the custom contact_list_template
        arrayAdapter = ArrayAdapter(this,
            R.layout.contact_list_template, id, contactSimple)
        contactList.adapter = arrayAdapter



        // Make a function for clicking on items
        contactList.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view,
                                                                            position, _ ->
            val selectedItem = adapterView.getItemAtPosition(position) as String
            val itemIdAtPos = adapterView.getItemIdAtPosition(position)

            // Change color for selected item to purple and all others to white
            contactList.forEach { it.setBackgroundColor(ContextCompat.getColor(this,
                R.color.white)) }
            view.setBackgroundColor(ContextCompat.getColor(this, R.color.green_200))


            var contactId = ""
            for (ids in idMapping) {
                if (itemIdAtPos == ids.list_id.toLong()) {
                    contactId = ids.contact_id
                }
            }

            Toast.makeText(applicationContext,"Selected contact $selectedItem",
                Toast.LENGTH_LONG).show()
            if (mBound) {
                // save selected Contact as JSON as var
                val selectedItemClass = contacts.find { it.id == contactId }
                if (selectedItemClass != null) {
                    selectedContactJson = mService.generateJson(selectedItemClass)
                }

            }
        }
    }

    fun onRetrieveButtonClick(@Suppress("UNUSED_PARAMETER") v: View) {
        if (selectedContactJson != "") {
            findViewById<TextView>(R.id.exportStringTextView).text =
                getString(R.string.exportMessage,selectedContactJson)
        }
        else {
            findViewById<TextView>(R.id.exportStringTextView).text = getString(R.string.errorContactSelection)
        }
    }

    fun onSaveFileButtonClick(@Suppress("UNUSED_PARAMETER") v: View) {


        // Create AlertDialog if Permission has not been granted
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.notice)
        // Set message
        builder.setMessage(R.string.alertContact)
        // Add an okay button and call the Request Permissions on clicking it
        builder.setNeutralButton(R.string.ok){ _,_ ->
            // Open the Dialog to allow permissions
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission granted, so set check to true
                    permissionWriteFiles = true
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) -> {
                    requestPermissionLauncher.launch(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
                else -> {
                    requestPermissionLauncher.launch(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
            }
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        // Show the Dialog when Permission isn't granted
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED) {
            alertDialog.show()
        }
        else {
            permissionWriteFiles = true
        }



        // Write to file using the Bound Service method
        if (selectedContactJson != "") {
            if (mBound) {
                val directory = mService.writeFile(selectedContactJson)
                Toast.makeText(applicationContext,("Saved file under $directory"),
                    Toast.LENGTH_LONG).show()
            }
        }
        else {
            Toast.makeText(applicationContext,"No contact selected!",
                Toast.LENGTH_SHORT).show()
        }
    }

    // Show the contents of the file using the read method of the Service
    fun onReadFileButtonClick(@Suppress("UNUSED_PARAMETER") v: View) {
        if (mBound) {
            try {
                val contents = mService.readFile()
                findViewById<TextView>(R.id.readFileImportTextView).text = contents
            }
            catch (e: Exception) {
                Toast.makeText(applicationContext,"Malformed Input File!",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onImportFileButtonClick(@Suppress("UNUSED_PARAMETER") v: View) {
        val gson = Gson()
        if (mBound) {

            // Try to catch an error for wrong files
            try {
                val contents = mService.readFile()
                // Parse JSON input to the Contact data class defined in LocalService
                val contactEntity = gson.fromJson(contents, LocalService.Contact::class.java)
                // Call the service method to import the contact
                mService.importContact(contactEntity)
            }
            catch (e: Exception) {
                Toast.makeText(applicationContext,"Malformed Input File!",
                    Toast.LENGTH_SHORT).show()
            }


        }
    }







//    @SuppressLint("Range")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when (requestCode) {
//            pickContact -> if (resultCode == RESULT_OK) {
//                val contactData: Uri? = data?.data
//                val c: Cursor? = contactData?.let { contentResolver.query(it, null, null, null, null) }
//                if (c != null) {
//                    if (c.moveToFirst()) {
//                        val name: String =
//                            c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
//                        Toast.makeText(this, "Name: $name", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        }
//    }

}