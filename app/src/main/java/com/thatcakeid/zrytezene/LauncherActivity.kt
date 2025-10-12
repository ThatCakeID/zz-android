package com.thatcakeid.zrytezene

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.view.WindowCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.thatcakeid.zrytezene.auth.LoginActivity
import com.thatcakeid.zrytezene.databinding.ActivityLauncherBinding
import com.thatcakeid.zrytezene.updater.UpdateActivity
import com.thatcakeid.zrytezene.updater.UpdateHelper

class LauncherActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityLauncherBinding.inflate(layoutInflater)
    }

    private val auth by lazy { FirebaseAuth.getInstance() }

    private val versionsDb by lazy {
        FirebaseFirestore.getInstance().collection("versions")
    }
    private val usersDb by lazy {
        FirebaseFirestore.getInstance().collection("users")
    }

    private val updateHelper by lazy {
        UpdateHelper(this, versionsDb)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
        setContentView(binding.root)

        // TODO: 10/12/2025 Implement a better update checker
        //       Please someone help me with this.
        updateHelper.check(
            onUpdateAvailable = {
                startActivity(Intent(this, UpdateActivity::class.java))
                finish()
            },
            onUpToDate = {
                validateAuth()
                Log.i(TAG, "No new updates, this app is latest!")
            },
            onError = { e ->
                validateAuth()
                Log.w(TAG, "Update error: ${e.message}")
            }
        )
    }

    private fun validateAuth() {
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        if (auth.currentUser?.isEmailVerified == false) {
            Toast.makeText(
                this@LauncherActivity,
                getString(R.string.auth_logout_unverified),
                Toast.LENGTH_LONG
            ).show()

            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        usersDb.document(auth.uid!!)
            .get()
            .addOnSuccessListener { snapshot: DocumentSnapshot ->
                if (snapshot.exists()) {
                    startActivity(Intent(applicationContext, StreamerActivity::class.java))
                    finish()
                } else {
                    // TODO: 10/12/2025 Implement user name dialog
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@LauncherActivity,
                    getString(R.string.error_unknown, e.message),
                    Toast.LENGTH_LONG
                ).show()

                // Resort to login activity
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
    }

    companion object {
        private const val TAG = "LauncherActivity"
    }
}