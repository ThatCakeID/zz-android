package com.thatcakeid.zrytezene

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.thatcakeid.zrytezene.ExtraMetadata.setWatermarkColors
import com.thatcakeid.zrytezene.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

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
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.auth_error_email_invalid)
                    , Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.auth_error_pwd_empty),
                    Snackbar.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                if (auth.currentUser!!.isEmailVerified) {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.auth_login_success),
                        Toast.LENGTH_LONG
                    ).show()

                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()

                } else {
                    val alertDialog = AlertDialog.Builder(this@LoginActivity)

                    alertDialog.setTitle(getString(R.string.auth_email_unverified_title))
                    alertDialog.setMessage(getString(R.string.auth_email_unverified_msg))
                    alertDialog.setPositiveButton(getString(R.string.dialog_btn_positive)) { _, _ ->
                        auth.currentUser!!.sendEmailVerification().addOnSuccessListener {
                                Snackbar.make(
                                    binding.root,
                                    getString(R.string.auth_verify_link_success),
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Snackbar.make(
                                    binding.root,
                                    getString(R.string.auth_verify_link_fail, e.message!!),
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                    }

                    alertDialog.setNegativeButton(getString(R.string.dialog_btn_negative)) { _, _ -> auth.signOut() }
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
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.auth_error_email_invalid),
                    Toast.LENGTH_LONG
                ).show()
                
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
            Snackbar.make(
                binding.root,
                getString(R.string.auth_verify_link_success),
                Snackbar.LENGTH_LONG
            ).show()

        }

        binding.registerText.setOnClickListener {
            startActivity(
                Intent(
                    applicationContext, RegisterActivity::class.java
                ).putExtra("email", binding.loginEmailTie.text.toString())
            )
        }
    }
}