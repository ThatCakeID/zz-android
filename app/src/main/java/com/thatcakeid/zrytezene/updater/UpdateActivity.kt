package com.thatcakeid.zrytezene.updater

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.thatcakeid.zrytezene.databinding.ActivityUpdateBinding

class UpdateActivity : AppCompatActivity() {
    private val binding: ActivityUpdateBinding by lazy {
        ActivityUpdateBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
        setContentView(binding.root)
    }
}