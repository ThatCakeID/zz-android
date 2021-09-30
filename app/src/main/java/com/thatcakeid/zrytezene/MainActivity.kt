package com.thatcakeid.zrytezene

import android.annotation.SuppressLint
import com.thatcakeid.zrytezene.ExtraMetadata.setWatermarkColors
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import com.canhub.cropper.CropImageContract
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.DocumentSnapshot
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.storage.FirebaseStorage
import com.canhub.cropper.CropImageView.Guidelines
import com.canhub.cropper.options
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.thatcakeid.zrytezene.databinding.ActivityMainBinding
import java.lang.Exception
import java.util.HashMap
import java.util.concurrent.atomic.AtomicReference

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val auth by lazy { FirebaseAuth.getInstance() }

    private val usersDb by lazy {
        FirebaseFirestore.getInstance().collection("users")
    }

    private val imageUri = AtomicReference<Uri?>()

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            imageUri.set(result.uriContent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setWatermarkColors(binding.textWatermark, binding.watermarkRoot)

        // Initialize Firebase
        val versionsDb = FirebaseFirestore.getInstance().collection("versions")

        versionsDb.get() // Fetch the data to client
            // Set a listener that listen if the data is already received
            .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                // Convert the result to List<DocumentSnapshot> and get the first item
                // NOTE: This implementation will be changed soon
                val document = queryDocumentSnapshots.documents[0]
                try {
                    // Get the app's package information
                    val packageInfo = packageManager.getPackageInfo(packageName, 0)

                    // Frick Android's deprecation
                    val appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        packageInfo.longVersionCode
                    } else {
                        packageInfo.versionCode.toLong()
                    }

                    val startActivity: Class<out Activity?>

                    // Get the client's app version and compare it with the one in the server
                    if ((document["version"] as Long) > appVersionCode) {
                        // There's a newer version!
                        startActivity = UpdateActivity::class.java
                    } else {
                        if (auth.currentUser == null) {
                            startActivity = LoginActivity::class.java
                        } else {
                            if (auth.currentUser!!.isEmailVerified) {
                                usersDb.document(auth.uid!!)
                                    .get()
                                    .addOnSuccessListener { snapshot: DocumentSnapshot ->
                                        if (snapshot.exists()) {
                                            startActivity(
                                                Intent(
                                                    applicationContext,
                                                    HomeActivity::class.java
                                                )
                                            )
                                            finish()
                                        } else {
                                            showBottomSheet()
                                        }
                                    }
                                    .addOnFailureListener { e: Exception ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            "An error occured: " + e.message,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                return@addOnSuccessListener
                            } else {
                                auth.signOut()
                                Toast.makeText(
                                    this@MainActivity,
                                    "You've been signed out because your current account's email is not verified.",
                                    Toast.LENGTH_LONG
                                ).show()
                                startActivity = LoginActivity::class.java
                            }
                        }
                    }
                    startActivity(Intent(applicationContext, startActivity))
                    finish()
                } catch (ignored: PackageManager.NameNotFoundException) {
                } // Ignored, this error shouldn't happen
            } // Set a listener that will listen if there are any errors
            .addOnFailureListener { e: Exception ->
                // Show the error to user
                Toast.makeText(
                    this@MainActivity,
                    "An error occured: " + e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    @SuppressLint("InflateParams")
    private fun showBottomSheet() {
        val view = layoutInflater.inflate(R.layout.sheet_userdata, null, false)
        view.rootView.setBackgroundColor(0x00000000)
        val bottomSheetDialog = BottomSheetDialog(this@MainActivity)

        view.findViewById<Button>(R.id.button_ok).setOnClickListener {
            val data = HashMap<String, Any>()

            data["description"] = ""
            data["img_url"] = ""
            data["mail"] = auth.currentUser!!.email!!
            data["time_creation"] = Timestamp.now()
            data["username"] =
                view.findViewById<TextView>(R.id.username_tie).text.toString()

            if (imageUri.get() != null) {
                val userPfp = FirebaseStorage.getInstance()
                    .reference.child("users/images").child(auth.uid!!).child("profile-img")

                val uploadTask = userPfp.putFile(imageUri.get()!!)
                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    userPfp.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        data["img_url"] = task.result.toString()

                        usersDb.document(auth.uid!!).set(data).addOnSuccessListener {
                            bottomSheetDialog.dismiss()
                            startActivity(Intent(applicationContext, HomeActivity::class.java))
                            finish()
                        }.addOnFailureListener { e: Exception ->
                            Toast.makeText(
                                this@MainActivity,
                                "An error occured: " + e.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Snackbar.make(
                            binding.root,
                            "An error occured: " + task.exception!!.message,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                usersDb.document(auth.uid!!).set(data).addOnSuccessListener {
                    bottomSheetDialog.dismiss()
                    startActivity(Intent(applicationContext, HomeActivity::class.java))
                    finish()
                }.addOnFailureListener { e: Exception ->
                    Toast.makeText(
                        this@MainActivity,
                        "An error occured: " + e.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }

        view.findViewById<View>(R.id.user_image).setOnClickListener {
            cropImage.launch(
                options {
                    setGuidelines(Guidelines.ON)
                }
            )
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.show()
    }
}