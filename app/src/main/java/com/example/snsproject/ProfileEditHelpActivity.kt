package com.example.snsproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.snsproject.databinding.ActivityProfileEditHelpBinding


class ProfileEditHelpActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityProfileEditHelpBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}