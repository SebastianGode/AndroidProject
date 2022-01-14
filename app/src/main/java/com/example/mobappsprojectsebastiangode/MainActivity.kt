package com.example.mobappsprojectsebastiangode

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.ContactsContract
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import android.app.Activity
import android.database.Cursor
import android.net.Uri
import android.content.ContentResolver
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    lateinit var toggle : ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout

    private lateinit var mService: LocalService
    private var mBound: Boolean = false

    var permissionReadContact: Boolean = false


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


            // Highlight the current selected Item
            it.isChecked = true

            // Switch Fragments when user clicks on Items in NavDrawer
            when(it.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment(), it.title.toString())
                R.id.nav_import -> replaceFragment(ImportFragment(), it.title.toString())
                R.id.nav_export -> replaceFragment(ExportFragment(), it.title.toString())
                R.id.nav_share -> replaceFragment(ShareFragment(), it.title.toString())
            }

            true
        }



        // Default Page is Home
        replaceFragment(HomeFragment(), "Home")
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
        onClickRequestPermission()

    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        mBound = false
    }

    /** Called when a button is clicked (the button in the layout file attaches to
     * this method with the android:onClick attribute)  */
    fun onContactListButtonClick(v: View) {

        // If boundService connected start the actual activity
        if (mBound) {
            // Check for Permissions and request if necessary
            onClickRequestPermission()
            // Only start Read Contacts when permission granted, else app will crash
            if (permissionReadContact) {
                // get all contacts which have an email or phone number
                val names = mService.getContacts()
                Toast.makeText(this, "number: $names", Toast.LENGTH_SHORT).show()
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