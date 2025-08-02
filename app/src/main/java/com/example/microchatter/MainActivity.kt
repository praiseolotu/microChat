package com.example.microchatter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.microchatter.databinding.ActivityMainBinding
import com.example.microchatter.ui.chat.ChatFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.RECORD_AUDIO
    )

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ChatFragment.newInstance())
            .commit()
    }

    private fun checkPermissions() {
        val ungranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (ungranted.isNotEmpty()) {
            if (shouldShowRequestPermissionRationale(ungranted.first())) {
                AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("μChat needs these permissions to function properly.")
                    .setPositiveButton("Allow") { _, _ ->
                        permissionLauncher.launch(ungranted.toTypedArray())
                    }
                    .setNegativeButton("Deny", null)
                    .show()
            } else {
                permissionLauncher.launch(ungranted.toTypedArray())
            }
        }
    }
}
