package com.example.newsapp.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapp.R
import com.example.newsapp.ui.activity.NewsActivity

class SplashScreenActivity : AppCompatActivity() {

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

         Handler(Looper.getMainLooper()).postDelayed({
            val i = Intent(this, NewsActivity::class.java)
            startActivity(i)
            finish()
        }, 3000)
    }
}