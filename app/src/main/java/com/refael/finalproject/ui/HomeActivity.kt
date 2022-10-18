package com.refael.finalproject.ui

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.refael.finalproject.R
import com.refael.finalproject.all_tasks.AllTasksFragment
import com.refael.finalproject.databinding.ActivityHomeBinding
import com.refael.finalproject.profile.ProfileFragment
import com.refael.finalproject.repository.AddTaskFragment
import kotlin.system.exitProcess

class HomeActivity : AppCompatActivity() {

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                moveToFragment(AllTasksFragment())
                return@OnNavigationItemSelectedListener true
            }

            R.id.nav_add_post -> {
                moveToFragment(AddTaskFragment())
                return@OnNavigationItemSelectedListener true
            }

            R.id.nav_profile -> {
                moveToFragment(ProfileFragment())
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        //If there is no connection, automatically open profile fragment with offline saved posts
        if(isOnline(this)){
            moveToFragment(AllTasksFragment())
        } else {
            moveToFragment(ProfileFragment())
            navView.menu.findItem(R.id.nav_profile).isChecked = true
        }
    }

    //Check internet connection
    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }

    //Fragment navigation
    private fun moveToFragment(fragment: Fragment) {
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragment_container, fragment)
        fragmentTrans.commit()
    }

    //Add dialog box to ask the user if he wants to exit the app
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