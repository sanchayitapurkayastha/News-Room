package com.example.newsapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.newsapp.model.Article

@Database (entities = [Article::class], version = 1)
@TypeConverters (Converters::class)
abstract class ArticlesDatabase : RoomDatabase() {
    abstract fun getArticleDao (): ArticlesDao

    companion object {
        @Volatile
        private var INSTANCE: ArticlesDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context?) = INSTANCE ?: synchronized(LOCK) {
            INSTANCE ?: context?.let { createDatabase (it).also { INSTANCE = it } }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                ArticlesDatabase::class.java,
                "article_database.db"
            ).build()

        /*private fun getDatabase(context: Context): ArticlesDatabase {
            val tempInstance = INSTANCE

            if (tempInstance != null)
                return tempInstance

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArticlesDatabase::class.java,
                    "article_database"
                ). build ()

                INSTANCE = instance
                return instance
            }
        }*/
    }
}