package com.example.newsapp.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_ETHERNET
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkCapabilities.*
import android.os.Build
import android.net.ConnectivityManager.TYPE_MOBILE
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.newsapp.model.Article
import com.example.newsapp.model.NewsResponse
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.util.NewsApplication
import com.example.newsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    app: Application,
    private val newsrepo: NewsRepository
): AndroidViewModel(app) {

    val news: MutableLiveData <Resource<NewsResponse>> = MutableLiveData()
    var newsPage = 1
    private var newsResponse: NewsResponse? = null

    val search: MutableLiveData <Resource<NewsResponse>> = MutableLiveData()
    var searchPage = 1
    private var searchResponse: NewsResponse? = null

    private var newQuery:String? = null
    private var oldQuery:String? = null

    fun getNews (country: String) = viewModelScope.launch {
        safeNewsCall(country)
    }

    private suspend fun safeNewsCall (country: String) {
        news.postValue(Resource.Loading())//omit loading state so that fragment can handle that

        try {
            if (hasInternet()) {
                val response = newsrepo.getNews(country, newsPage)  //here we make network response
                news.postValue(handleNewsResponse(response))
            }
            else
                news.postValue(Resource.Error("No Internet Connection"))
        }
        catch (t: Throwable) {
            when (t) {
                is IOException -> news.postValue(Resource.Error("Network Failure"))
                else -> news.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    //whether to omit success state or error state in our news liveData
    private fun handleNewsResponse (response: Response<NewsResponse>) : Resource <NewsResponse> {
        if(response.isSuccessful)
            response.body()?.let { resultResponse->
                newsPage ++

                if (newsResponse == null)
                    newsResponse = resultResponse

                else {
                    val oldArticles = newsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(newsResponse?: resultResponse)
            }

        return Resource.Error(response.message())
    }

    fun searchNews (searchQuery: String) = viewModelScope.launch {
        safeSearchCall(searchQuery)
    }

    private suspend fun safeSearchCall (searchQuery: String) {
        newQuery = searchQuery
        search.postValue(Resource.Loading())

        try {
            if (hasInternet()) {
                val response = newsrepo.search(searchQuery, searchPage)
                search.postValue( handleSearchResponse(response) )
            }
            else
                search.postValue(Resource.Error("No Internet Connection"))
        }
        catch (t: Throwable) {
            when (t) {
                is IOException -> search.postValue(Resource.Error("Network Failure"))
                else -> search.postValue(Resource.Error("Conversion Error"))
            }
        }
    }


    private fun handleSearchResponse (response: Response<NewsResponse>) : Resource <NewsResponse> {
        if(response.isSuccessful)
            response.body()?.let { resultResponse->
                if (searchResponse == null || newQuery != oldQuery) {
                    searchPage = 1
                    oldQuery = newQuery
                    searchResponse = resultResponse
                }
                else {
                    searchPage ++
                    val oldArticles = searchResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchResponse?: resultResponse)
            }

        return Resource.Error(response.message())
    }

    fun saveArticle (article: Article) = viewModelScope.launch { newsrepo.updateInsert(article) }

    fun getSavedArticle () = newsrepo.getSavedArticle()

    fun deleteArticle (article: Article) = viewModelScope.launch { newsrepo.deleteArticle(article) }

    //check for internet
    private fun hasInternet (): Boolean {
        val connectivityManager = getApplication<NewsApplication>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNet = connectivityManager.activeNetwork?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNet)?: return false

            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
        else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type){
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }
}