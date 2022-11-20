package com.example.snsproject

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.example.snsproject.databinding.ActivityPostingsHelpBinding


class PostingsHelpActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityPostingsHelpBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}