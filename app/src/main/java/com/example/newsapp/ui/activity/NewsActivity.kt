package com.example.newsapp.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.newsapp.R
import com.example.newsapp.database.ArticlesDatabase
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.ui.viewmodel.NewsViewModel
import com.example.newsapp.ui.viewmodel.NewsViewModelProviderFactory
import com.google.android.material.bottomnavigation.BottomNavigationView

class NewsActivity : AppCompatActivity() {
    lateinit var  viewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        val bottomNavigationView = findViewById <BottomNavigationView> (R.id.bottomNav)
        val navGraph = findNavController(R.id.fragment)
        bottomNavigationView.setupWithNavController(navGraph)

        val newsRepository = NewsRepository (ArticlesDatabase.getDatabase(this))
        val viewModelProviderFactory = NewsViewModelProviderFactory (newsRepository)
        viewModel = ViewModelProvider (this, viewModelProviderFactory).get(NewsViewModel :: class.java)
    }
}