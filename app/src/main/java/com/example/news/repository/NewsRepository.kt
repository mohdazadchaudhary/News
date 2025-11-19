package com.example.news.repository

import com.example.news.api.RetrofitInstance
import com.example.news.db.ArticleDatabase
import com.example.news.models.Article

class NewsRepository(val db: ArticleDatabase) {
    suspend fun getHeadlines(countryCode: String, pageNumber: Int) =
        RetrofitInstance.api.getHeadlines(countryCode, pageNumber)

    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        RetrofitInstance.api.searchForNews(searchQuery, pageNumber)

    suspend fun  upsert(article: Article) = db.getArticleDao().upsert(article)
    fun getFavoriteNews() = db.getArticleDao().getAllArticles()

    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)






}

