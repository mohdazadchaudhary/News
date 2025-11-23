package com.example.news.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
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
    ): View {
        binding = FragmentArticleBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(requireActivity())[NewsViewModel::class.java]

        val article = args.article


        binding.webView.apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            article.url?.let { loadUrl(it) }
        }

        binding.fab.setOnClickListener {
            viewModel.addToFavourites(article)
            Snackbar.make(it, "Article saved successfully", Snackbar.LENGTH_LONG).show()
        }

        return binding.root
    }

    // This prevents back button from closing the app when in WebView
    override fun onDestroyView() {
        binding.webView.destroy()   // ← Prevents memory leaks
        super.onDestroyView()
    }
}