package com.example.newsapp.repository

import com.example.newsapp.api.RetrofitInstance
import com.example.newsapp.database.ArticlesDatabase
import com.example.newsapp.model.Article

class NewsRepository (val db: ArticlesDatabase) {
    suspend fun getNews (country : String, pageSize: Int) = RetrofitInstance.api.getNews(country, pageSize)

    suspend fun search (searchQuery: String, pageSize: Int) = RetrofitInstance.api.search(searchQuery, pageSize)

    suspend fun updateInsert (article : Article) = db.getArticleDao().updateInsert(article)

    fun getSavedArticle () = db.getArticleDao().getArticles()

    suspend fun deleteArticle (article: Article) = db.getArticleDao().deleteArticle(article)
}