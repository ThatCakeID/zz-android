package com.thatcakeid.zrytezene

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.thatcakeid.zrytezene.databinding.ActivityUpdateBinding

class UpdateActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityUpdateBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
    }
}