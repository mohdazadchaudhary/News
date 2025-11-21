package com.example.news.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.navigation.fragment.navArgs
import com.example.news.NewsActivity
import com.example.news.R
import com.example.news.databinding.FragmentArticleBinding
import com.example.news.util.NewsViewModel
import com.google.android.material.snackbar.Snackbar


class ArticleFragment : Fragment(R.layout.fragment_article) {

    lateinit var viewModel: NewsViewModel
    private lateinit var binding: FragmentArticleBinding
    val args: ArticleFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentArticleBinding.bind(view!!)

        viewModel = (activity as NewsActivity).viewModel
        val article = args.article

        binding.webView.apply {
            webViewClient = WebViewClient()
            article.url?.let {
                loadUrl(it)
            }
        }

        binding.fab.setOnClickListener {
            viewModel.addToFavourites(article)
            Snackbar.make(it, "Added to Favourites", Snackbar.LENGTH_SHORT).show()
        }

        return view
    }
}