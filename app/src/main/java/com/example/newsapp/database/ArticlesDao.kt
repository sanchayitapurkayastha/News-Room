package com.example.newsapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.newsapp.model.Article

@Dao
interface ArticlesDao {
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateInsert (article: Article): Long   //return the id

    @Query ("SELECT * FROM articles")
    fun getArticles() : LiveData <List<Article>>    //return a list of articles in LiveData

    @Delete
    suspend fun deleteArticle (article: Article)
}