package com.thatcakeid.zrytezene.auth

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.os.Bundle
import android.text.TextWatcher
import android.text.Editable
import androidx.core.view.WindowCompat
import com.google.android.material.snackbar.Snackbar
import com.thatcakeid.zrytezene.R
import com.thatcakeid.zrytezene.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
        setContentView(binding.root)

        binding.registerEmailTie.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (binding.registerEmailTie.text.toString().trim()
                        .matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex())) {
                    binding.registerEmailTil.error = null
                } else {
                    binding.registerEmailTil.error = "Invalid email!"
                }
            }

        })

        if (intent.getStringExtra("email") != "")
            binding.registerEmailTie.setText(intent.getStringExtra("email"))

        binding.buttonContinue.setOnClickListener {

            val email = binding.registerEmailTie.text.toString().trim()
            val password = binding.registerPasswTie.text.toString().trim()
            val passwordRepeat = binding.registerPassw2Tie.text.toString().trim()

            if (!email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex())) {
                Snackbar.make(binding.root,
                    getString(R.string.auth_error_email_invalid), Snackbar.LENGTH_LONG).show()

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

            if (password != passwordRepeat) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.auth_error_pwd_mismatch),
                    Snackbar.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(
                binding.registerEmailTie.text.toString().trim(),
                binding.registerPasswTie.text.toString()
            ).addOnSuccessListener {
                assert(auth.currentUser != null)
                auth.currentUser!!
                    .sendEmailVerification().addOnSuccessListener {
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
                auth.signOut()
            }.addOnFailureListener { e ->
                Snackbar.make(
                    binding.root,
                    e.message!!,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
        binding.textView5.setOnClickListener { finish() }
    }
}