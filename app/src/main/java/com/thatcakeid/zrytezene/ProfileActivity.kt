package com.thatcakeid.zrytezene

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.thatcakeid.zrytezene.ExtraMetadata.setWatermarkColors
import com.thatcakeid.zrytezene.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }

    private var database = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setWatermarkColors(binding.textWatermark, binding.watermarkRoot)

        setSupportActionBar(binding.toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val uid = intent.getStringExtra("uid")

        val userRef = database.collection("users").document(uid!!)

        userRef.addSnapshotListener { value, _ ->
            if (value == null) {
                Toast.makeText(
                    this@ProfileActivity,
                    getString(R.string.profile_update_error_null),
                    Toast.LENGTH_LONG
                ).show()

                return@addSnapshotListener
            }

            binding.userName.text = value.getString("username")
            binding.userBio.text = value.getString("description")

            if (value.getString("img_url") == "") {
                binding.userProfilePicture.imageTintList = ContextCompat.getColorStateList(
                        applicationContext,
                        R.color.imageTint
                )

                binding.userProfilePicture.setImageResource(R.drawable.ic_account_circle)
            } else {
                binding.userProfilePicture.imageTintList = null

                Glide.with(applicationContext)
                        .load(value.getString("img_url"))
                        .into(binding.userProfilePicture)
            }
        }

    }
}