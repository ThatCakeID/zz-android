package com.thatcakeid.zrytezene.ui.startup

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView.Guidelines
import com.canhub.cropper.options
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.thatcakeid.zrytezene.R
import com.thatcakeid.zrytezene.databinding.FragmentSplashBinding
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class SplashFragment : Fragment(R.layout.fragment_splash) {
    private val binding: FragmentSplashBinding by viewBinding(FragmentSplashBinding::bind)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                    val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)

                    // Frick Android's deprecation
                    val appVersionCode =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode
                        else packageInfo.versionCode.toLong()

                    // Get the client's app version and compare it with the one in the server
                    if ((document["version"] as Long) > appVersionCode) {
                        // There's a newer version!
                        findNavController()
                            .navigate(R.id.action_splashFragment_to_updateFragment)

                    } else {
                        if (auth.currentUser == null) {
                            // We're not logged in, login then
                            findNavController()
                                .navigate(R.id.action_splashFragment_to_loginFragment)
                        } else {
                            if (auth.currentUser!!.isEmailVerified) {
                                usersDb.document(auth.uid!!)
                                    .get()
                                    .addOnSuccessListener { snapshot: DocumentSnapshot ->
                                        if (snapshot.exists()) {
                                            // Logged in, open the homepage
                                            findNavController()
                                                .navigate(R.id.action_splashFragment_to_home_nav)

                                        } else {
                                            showBottomSheet()
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            requireContext(),
                                            "An error occurred: " + e.message,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                return@addOnSuccessListener
                            } else {
                                auth.signOut()
                                Toast.makeText(
                                    requireContext(),
                                    "You've been signed out because your current account's email is not verified.",
                                    Toast.LENGTH_LONG
                                ).show()

                                // Go to login page
                                findNavController()
                                    .navigate(R.id.action_splashFragment_to_loginFragment)
                            }
                        }
                    }
                } catch (ignored: PackageManager.NameNotFoundException) {
                } // Ignored, this error shouldn't happen
            } // Set a listener that will listen if there are any errors
            .addOnFailureListener { e ->
                // Show the error to user
                Toast.makeText(
                    requireContext(),
                    "An error occured: " + e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showBottomSheet() {
        val view = layoutInflater.inflate(R.layout.sheet_userdata, null, false)
        (view.parent as View).setBackgroundColor(0x00000000)
        val bottomSheetDialog = BottomSheetDialog(requireContext())

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
                            findNavController()
                                .navigate(R.id.action_splashFragment_to_home_nav)

                        }.addOnFailureListener { e ->
                            Toast.makeText(
                                requireContext(),
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
                    findNavController()
                        .navigate(R.id.action_splashFragment_to_home_nav)
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
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