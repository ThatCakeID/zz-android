package com.thatcakeid.zrytezene.ui.startup

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.thatcakeid.zrytezene.ExtraMetadata.setWatermarkColors
import com.thatcakeid.zrytezene.R
import com.thatcakeid.zrytezene.databinding.FragmentLoginBinding
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class LoginFragment : Fragment(R.layout.fragment_login) {
    private val binding: FragmentLoginBinding by viewBinding(FragmentLoginBinding::bind)

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setWatermarkColors(binding.textWatermark, binding.watermarkRoot)

        binding.loginEmailTie.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (binding.loginEmailTie.text.toString().trim()
                        .matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex())) {
                    binding.loginEmailTil.error = null
                } else {
                    binding.loginEmailTil.error = "Invalid email!"
                }
            }
        })

        binding.buttonContinue.setOnClickListener {
            val email = binding.loginEmailTie.text.toString().trim()
            val password = binding.loginPasswTie.text.toString()

            if (!email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex())) {
                Toast.makeText(this@LoginFragment, "Invalid email!", Toast.LENGTH_LONG).show()

                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Snackbar.make(binding.root, "Password can't be empty!", Snackbar.LENGTH_LONG).show()

                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                if (auth.currentUser!!.isEmailVerified) {
                    Toast.makeText(this@LoginFragment, "Logged in", Toast.LENGTH_LONG)
                        .show()
                    startActivity(Intent(applicationContext, SplashFragment::class.java))
                    finish()

                } else {
                    val alertDialog = AlertDialog.Builder(this@LoginFragment)

                    alertDialog.setTitle("Unverified Email")
                    alertDialog.setMessage("Your email isn't verified. Do you want to re-send a new verification link to your email?")
                    alertDialog.setPositiveButton("Yes") { _, _ ->
                        auth.currentUser!!.sendEmailVerification().addOnSuccessListener {
                                Snackbar.make(
                                    binding.root,
                                    "A verification link has been sent to your email. Please check your inbox or spam box.",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Snackbar.make(
                                    binding.root,
                                    e.message!!,
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                    }

                    alertDialog.setNegativeButton("No") { _, _ -> auth.signOut() }
                    alertDialog.setCancelable(false)
                    alertDialog.create().show()
                }
            }.addOnFailureListener { e ->
                Snackbar.make(
                    binding.root,
                    e.message!!,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        binding.forgotPasswordText.setOnClickListener {
            val email = binding.loginEmailTie.text.toString().trim()

            if (email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex())) {
                Toast.makeText(this@LoginFragment, "Please enter a valid email!", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
            Snackbar.make(
                binding.root,
                "A verification link has been sent to your email. Please check your inbox or spam box.",
                Snackbar.LENGTH_LONG
            ).show()

        }

        binding.registerText.setOnClickListener {
            startActivity(
                Intent(
                    applicationContext, RegisterFragment::class.java
                ).putExtra("email", binding.loginEmailTie.text.toString())
            )
        }
    }
}