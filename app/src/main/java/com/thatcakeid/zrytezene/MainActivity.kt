package com.thatcakeid.zrytezene

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
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.pm.PackageInfoCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.storage.FirebaseStorage
import com.canhub.cropper.CropImageView.Guidelines
import com.canhub.cropper.options
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.thatcakeid.zrytezene.databinding.ActivityMainBinding
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

    private var pfpImgView: ShapeableImageView? = null

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            imageUri.set(result.uriContent)
            pfpImgView?.setImageURI(result.uriContent)
            pfpImgView?.imageTintList = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        // Initialize Firebase
        val versionsDb = FirebaseFirestore.getInstance().collection("versions")

        versionsDb.get() // Fetch the data to client
            // Set a listener that listen if the data is already received
            .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                // Convert the result to List<DocumentSnapshot> and get the first item
                // NOTE: This implementation will be changed soon
                val document = queryDocumentSnapshots.documents[0]
                try {
                    // Get the package information of this application
                    val appPackageInfo = packageManager.getPackageInfo(packageName, 0)
                    // Get the app's version code using PackageInfoCompat
                    //   ryenyuku should be happy now :piola:
                    val appVersionCode = PackageInfoCompat.getLongVersionCode(appPackageInfo)
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
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            getString(R.string.error_unknown, e.message),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                return@addOnSuccessListener
                            } else {
                                auth.signOut()
                                Toast.makeText(
                                    this@MainActivity,
                                    getString(R.string.auth_logout_unverified),
                                    Toast.LENGTH_LONG
                                ).show()
                                startActivity = LoginActivity::class.java
                            }
                        }
                    }
                    startActivity(Intent(applicationContext, startActivity))
                    finish()
                } catch (_: PackageManager.NameNotFoundException) {
                } // Ignored, this error shouldn't happen
            } // Set a listener that will listen if there are any errors
            .addOnFailureListener { e ->
                // Show the error to user
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.error_unknown, e.message),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showBottomSheet() {
        val view = layoutInflater.inflate(R.layout.sheet_userdata, binding.root, false)
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
                        }.addOnFailureListener { e ->
                            Toast.makeText(
                                this@MainActivity,
                                getString(R.string.error_unknown, e.message),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.error_unknown, task.exception!!.message),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                usersDb.document(auth.uid!!).set(data).addOnSuccessListener {
                    bottomSheetDialog.dismiss()
                    startActivity(Intent(applicationContext, HomeActivity::class.java))
                    finish()
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.error_unknown, e.message),
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

        pfpImgView = view.findViewById(R.id.user_image)

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.show()
    }
}