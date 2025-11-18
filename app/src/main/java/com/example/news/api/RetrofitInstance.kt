package com.example.news.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {

    companion object {

        // Make sure you have this constant somewhere (usually in Constants.kt)
        private const val BASE_URL = "https://newsapi.org/"

        // Logging interceptor
        private val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // OkHttpClient with logging
        private val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        // Retrofit instance (lazy so it's created only when first used)
        private val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        // Public API service — this is what you'll actually use
        val api: NewsAPI by lazy {
            retrofit.create(NewsAPI::class.java)
        }
    }
}