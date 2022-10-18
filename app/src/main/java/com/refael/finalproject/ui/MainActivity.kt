package com.refael.finalproject.ui

import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.refael.finalproject.R
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onBackPressed() { // Back button exit dialog box.
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.confirm_exit))
            setMessage(getString(R.string.are_you_sure_exit))

            setPositiveButton(getString(R.string.yes)) { _, _ ->
                // if user press yes, then finish the current activity.
                moveTaskToBack(true)
                exitProcess(-1)
            }

            setNegativeButton(getString(R.string.no)) { _, _ ->
            }
            setCancelable(true)
        }.create().show()
    }

}