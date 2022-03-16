package com.example.mobappsprojectsebastiangode

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.TextView
import androidx.core.view.forEach


class MainActivity : AppCompatActivity() {

    // vars for drawer
    lateinit var toggle : ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout

    // vars to access BoundService
    private lateinit var mService: LocalService
    private var mBound: Boolean = false

    // vars to check for permissions
    private var permissionReadContact: Boolean = false
    private var permissionWriteFiles: Boolean = false
    private var permissionWriteContact: Boolean = false

    // var to store the selected Contact
    var selectedContactJson: String = ""

    // This is code from the example https://developer.android.com/guide/components/bound-services
    // Defines callbacks for service binding, passed to bindService()
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            // Bounding to LocalService complete, now cast Binder and get the service
            val binder = service as LocalService.LocalBinder
            mService = binder.getService()
            // Set mBound to true for checks that service is correctly bound
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            // Set mBound to false for checks that service is not bound
            mBound = false
        }
    }

    // Returns true if the device has a big screen (e.g. Tablet)
    private fun isBigScreen(context: Context): Boolean {
        return ((context.resources.configuration.screenLayout
                and Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // We only have an drawer layout on small devices, so don't do this on big Screens
        if(!isBigScreen(this)) {
            drawerLayout = findViewById(R.id.drawerLayout)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeButtonEnabled(true)

            toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()
        }

        val navView : NavigationView = findViewById(R.id.nav_view)

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

    // method for replacing fragments
    private fun replaceFragment(fragment: Fragment, title : String) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()

        // Check whether the device is big and even has the Drawer
        if(!isBigScreen(this)) {
            // Close Drawer when user clicks on it
            drawerLayout.closeDrawers()
        }

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
        // Unbind from Service and set the check to false
        unbindService(connection)
        mBound = false
    }

    // Called when a button is clicked (the button in the layout file attaches to
    // this method with the android:onClick attribute).
    // This is the same for all onClick functions here
    // It needs an View parameter to work, but I don't need it in code. So suppressing the warning.
    // Suppressing this warning on every onClick method from now on
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

    // Function to create the list of contacts
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
            findViewById<TextView>(R.id.exportStringTextView).text = getString(
                R.string.errorContactSelection)
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
            Toast.makeText(applicationContext,getString(R.string.noContactSelected),
                Toast.LENGTH_SHORT).show()
        }
    }

    // Show the contents of the file using the read method of the Service
    fun onReadFileButtonClick(@Suppress("UNUSED_PARAMETER") v: View) {
        if (mBound) {
            // Try to catch an error for wrong files
            try {
                val contents = mService.readFile()
                findViewById<TextView>(R.id.readFileImportTextView).text = contents
            }
            catch (e: Exception) {
                Toast.makeText(applicationContext,getString(R.string.malformedInputFile),
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Import the file
    fun onImportFileButtonClick(@Suppress("UNUSED_PARAMETER") v: View) {
        if (mBound) {
            // Try to catch an error for wrong files
            try {
                val contents = mService.readFile()

                if (mBound) {
                    // Parse JSON input
                    val contactEntity = mService.parseJson(contents)
                    // Call the service method to import the contact
                    mService.importContact(contactEntity)
                }
            }
            catch (e: Exception) {
                Toast.makeText(applicationContext,getString(R.string.malformedInputFile),
                    Toast.LENGTH_SHORT).show()
            }


        }
    }

    // Show all Edit fields and get Contact information
    fun onEditContactReadButtonClick(@Suppress("UNUSED_PARAMETER") v: View) {
        // Open the Dialog to allow Write permissions to contacts if not exist
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission granted, so set check to true
                permissionWriteContact = true
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_CONTACTS
            ) -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.WRITE_CONTACTS
                )
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.WRITE_CONTACTS
                )
            }
        }
        if (mBound) {
            // If user has selected a contact go ahead, else throw error message
            if (selectedContactJson != "") {
                // Parse JSON input
                val contactEntity = mService.parseJson(selectedContactJson)
                // Change TextView to show the contact's name
                findViewById<TextView>(R.id.editContactNameTextView).text = getString(
                    R.string.editNameText, contactEntity.name)
                // Make the TextView in front of the phone number as well as the edit field visible
                findViewById<TextView>(R.id.editPhoneNumberTextView).visibility = View.VISIBLE
                val editPhone = findViewById<EditText>(R.id.editTextPhoneNumberPhone)
                editPhone.visibility = View.VISIBLE
                editPhone.setText(contactEntity.number)
                // Make the save button visible too
                findViewById<Button>(R.id.editContactSaveButton).visibility = View.VISIBLE
            }
            else {
                Toast.makeText(applicationContext,getString(R.string.noContactSelected),
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onEditContactSaveButtonClick(@Suppress("UNUSED_PARAMETER") v: View) {
        if(mBound) {
            if (selectedContactJson != "") {
                // Get the selected contactEntity as contact data class
                val contactEntity = mService.parseJson(selectedContactJson)

                // Get the new Phone Number from the EditText field
                val newNumber = findViewById<EditText>(R.id.editTextPhoneNumberPhone).text.toString()

                // Create the new contact data class
                val newContact = LocalService.Contact(contactEntity.id, contactEntity.name,
                    newNumber)

                // Call the updateContact Function in BoundService
                mService.updateContact(newContact, contactEntity)

                // Give response that successful
                Toast.makeText(applicationContext,getString(R.string.contactUpdateSuccessful),
                    Toast.LENGTH_SHORT).show()

                // Update internal JSON for future use
                selectedContactJson = mService.generateJson(newContact)
            }
            else {
                Toast.makeText(applicationContext,getString(R.string.noContactSelected),
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}