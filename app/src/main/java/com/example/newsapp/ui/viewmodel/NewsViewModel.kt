package com.example.newsapp.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.model.Article
import com.example.newsapp.model.NewsResponse
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(private val newsrepo: NewsRepository): ViewModel() {
    val news: MutableLiveData <Resource <NewsResponse>> = MutableLiveData()
    var newsPage = 1
    private var newsResponse: NewsResponse? = null

    val search: MutableLiveData <Resource <NewsResponse>> = MutableLiveData()
    var searchPage = 1
    private var searchResponse: NewsResponse? = null

    fun getNews (country: String) = viewModelScope.launch {
        //safeNewsCall(country)
        news.postValue(Resource.Loading())
        val response = newsrepo.getNews(country, newsPage)  //here we make network response
        news.postValue( handleNewsResponse(response) )
    }

    /*private suspend fun safeNewsCall (country: String) {
        news.postValue(Resource.Loading())//omit loading state so that fragment can handle that
        try {
            if (hasInternet()) {
                val response = newsrepo.getNews(country, newsPage)  //here we make network response
                news.postValue( handleNewsResponse(response) )
            }
            else {
                news.postValue(Resource.Error("No Internet Connection"))
            }
        }catch (t: Throwable) {
            when (t) {
                is IOException -> news.postValue(Resource.Error("Network Failure"))
                else -> news.postValue(Resource.Error("Conversion Error"))
            }
        }
    }*/

    fun searchNews (searchQuery: String) = viewModelScope.launch {
        //safeSearchCall(searchQuery)
        search.postValue(Resource.Loading())
        val response = newsrepo.search(searchQuery, newsPage)
        search.postValue( handleSearchResponse(response) )
    }

    /*private suspend fun safeSearchCall (searchQuery: String) {
        search.postValue(Resource.Loading())
        try {
            if (hasInternet()) {
                val response = newsrepo.search(searchQuery, newsPage)
                search.postValue( handleSearchResponse(response) )
            }
            else {
                search.postValue(Resource.Error("No Internet Connection"))
            }
        }catch (t: Throwable) {
            when (t) {
                is IOException -> search.postValue(Resource.Error("Network Failure"))
                else -> search.postValue(Resource.Error("Conversion Error"))
            }
        }
    }*/

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

    private fun handleSearchResponse (response: Response<NewsResponse>) : Resource <NewsResponse> {
        if(response.isSuccessful)
            response.body()?.let { resultResponse->
                searchPage ++
                if (searchResponse == null)
                    searchResponse = resultResponse
                else {
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
    /*private fun hasInternet (): Boolean {
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
    }*/
}