package com.example.newsapp.ui.activity

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.newsapp.R
import com.example.newsapp.ui.viewmodel.NewsViewModel
import kotlinx.android.synthetic.main.activity_news.*

class NewsActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        val navHostFragment= supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController= navHostFragment.navController
        bottomNav.setupWithNavController(navController)
    }
}