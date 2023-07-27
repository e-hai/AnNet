package com.an.net.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.an.net.R

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewModel = MainViewModel(application)
    }
}