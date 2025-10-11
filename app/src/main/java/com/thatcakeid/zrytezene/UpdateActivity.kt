package com.thatcakeid.zrytezene

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.WindowCompat
import com.thatcakeid.zrytezene.databinding.ActivityUpdateBinding

class UpdateActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityUpdateBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
        setContentView(binding.root)
    }
}