package com.thatcakeid.zrytezene

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.thatcakeid.zrytezene.databinding.ActivityDebugBinding

class DebugActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityDebugBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        AlertDialog.Builder(this)
            .setTitle("App crashed")
            .setMessage(intent.getStringExtra("error"))
            .setPositiveButton("Exit") { _, _ -> finish() }
            .create().show()
    }
}