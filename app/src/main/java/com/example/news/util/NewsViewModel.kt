package com.example.news.util

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.news.models.Article
import com.example.news.models.NewsResponse
import com.example.news.repository.NewsRepository
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException



class NewsViewModel(
    application: Application,
    val newsRepository: NewsRepository
) : AndroidViewModel(application) {

    private val _headlines = MutableLiveData<Resource<NewsResponse>>()
    val headlines: LiveData<Resource<NewsResponse>> = _headlines

    var headlinesPage = 1
    var headlinesResponse: NewsResponse? = null

    private val _searchNews = MutableLiveData<Resource<NewsResponse>>()
    val searchNews: LiveData<Resource<NewsResponse>> = _searchNews

    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null
    var newSearchQuery: String? = null
    var oldSearchQuery: String? = null

    init {
        getHeadlines("in")
    }

    fun getHeadlines(countryCode: String) = viewModelScope.launch {
        HeadlinesInternet(countryCode)
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }


    private fun handleHeadlinesResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                headlinesPage++
                if (headlinesResponse == null) {
                    headlinesResponse = resultResponse
                } else {
                    val oldArticles = headlinesResponse?.articles
                    val newArticles = resultResponse.articles
                    (oldArticles as MutableList<Article>).addAll(newArticles.orEmpty())
                }
                _headlines.postValue(Resource.Success(headlinesResponse ?: resultResponse))
                return Resource.Success(headlinesResponse ?: resultResponse)
            }
            // if body is null
            _headlines.postValue(Resource.Error("Empty response"))
            return Resource.Error("Empty response")
        }
        _headlines.postValue(Resource.Error(response.message()))
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchNewsPage++

                if (searchNewsResponse == null || newSearchQuery != oldSearchQuery) {
                    searchNewsPage = 1
                    oldSearchQuery = newSearchQuery
                    searchNewsResponse = resultResponse
                } else {
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    (oldArticles as MutableList<Article>).addAll(newArticles.orEmpty())
                }

                _searchNews.postValue(Resource.Success(searchNewsResponse ?: resultResponse))
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }

            _searchNews.postValue(Resource.Error("Empty response"))
            return Resource.Error("Empty response")
        }

        _searchNews.postValue(Resource.Error(response.message()))
        return Resource.Error(response.message())
    }

    fun addToFavourites(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getFavouritesNews() = newsRepository.getFavoriteNews()

    fun addToDelete(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    fun internetConnection(context: Context): Boolean {
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when {
                    hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } ?: false
        }
    }

    private suspend fun HeadlinesInternet(countryCode: String) {
        _headlines.postValue(Resource.Loading())  // ← use _headlines (private MutableLiveData)

        try {
            if(internetConnection(getApplication())) {
                val response = newsRepository.getHeadlines(countryCode, headlinesPage)
                _headlines.postValue(handleHeadlinesResponse(response))
            } else {
                _headlines.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> _headlines.postValue(Resource.Error("Network Failure"))
                else -> _headlines.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private suspend fun searchNewsInternet(searchQuery: String) {
    newSearchQuery = searchQuery
    _searchNews.postValue(Resource.Loading())
        try {
            if (internetConnection(getApplication())) {
                val response = newsRepository.searchNews(searchQuery, searchNewsPage)
                _searchNews.postValue(handleSearchNewsResponse(response))
            } else {
                _searchNews.postValue(Resource.Error("No internet connection"))
            }
        }catch (t: Throwable) {
            when (t) {
                is IOException -> _headlines.postValue(Resource.Error("Network Failure"))
                else -> _headlines.postValue(Resource.Error("Conversion Error"))
            }
        }
    }
}