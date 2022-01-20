package com.example.mobappsprojectsebastiangode

import android.app.AlertDialog
import android.content.*
import android.content.ContentValues.TAG
import android.database.Cursor
import android.os.Bundle
import android.os.IBinder
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import java.util.jar.Manifest
import androidx.core.content.ContentResolverCompat

// Creates the Home Fragment
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

}