package com.example.news.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news.R
import com.example.news.adapters.NewsAdapter
import com.example.news.databinding.FragmentSearchBinding
import com.example.news.databinding.ItemErrorBinding
import com.example.news.util.Constants
import com.example.news.util.NewsViewModel
import com.example.news.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {
    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    lateinit var itemSearchErrorBinding: ItemErrorBinding
    lateinit var retryButton: Button
    lateinit var errorText: TextView
    lateinit var itemSearchError: CardView
    lateinit var binding: FragmentSearchBinding
    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Fixed: Initialize ViewModel AFTER view is created
        newsViewModel = ViewModelProvider(requireActivity())[NewsViewModel::class.java]

        itemSearchErrorBinding = binding.itemSearchError
        retryButton = itemSearchErrorBinding.retryButton
        errorText = itemSearchErrorBinding.errorText

        setupSearchRecycler()

        // Item click → go to ArticleFragment
        newsAdapter.setOnItemClickListener { article ->
            val bundle = Bundle().apply {
                putSerializable("article", article)
            }
            findNavController().navigate(
                R.id.action_searchFragment_to_articleFragment,
                bundle
            )
        }

        var job: Job? = null
        binding.searchEdit.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(Constants.SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if (editable.toString().trim().isNotEmpty()) {
                        newsViewModel.searchNews(editable.toString().trim())
                    }
                }
            }
        }


        newsViewModel.searchNews.observe(viewLifecycleOwner) { response ->

            if (binding.searchEdit.text.toString().trim().isEmpty() && response is Resource.Error) {
                hideErrorMessage()
                hideProgressBar()
                newsAdapter.differ.submitList(emptyList())
                return@observe
            }

            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())

                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = newsViewModel.searchNewsPage == totalPages

                        if (isLastPage) {
                            binding.recyclerSearch.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(requireActivity(), "Error: $message", Toast.LENGTH_LONG).show()
                        showErrorMessage(message)
                    }
                }
                is Resource.Loading -> {
                    if (binding.searchEdit.text.toString().trim().isNotEmpty()) {
                        showProgressBar()
                    }
                }
            }
        }

        retryButton.setOnClickListener {
            if (binding.searchEdit.text.toString().trim().isNotEmpty()) {
                newsViewModel.searchNews(binding.searchEdit.text.toString().trim())
            } else {
                hideErrorMessage()
            }
        }

        // Pagination
        binding.recyclerSearch.addOnScrollListener(scrollListener)
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showErrorMessage(message: String) {
        itemSearchErrorBinding.root.visibility = View.VISIBLE
        itemSearchErrorBinding.errorText.text = message
        isError = true
    }

    private fun hideErrorMessage() {
        itemSearchErrorBinding.root.visibility = View.INVISIBLE
        isError = false
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate = !isError && isNotLoadingAndNotLastPage && isAtLastItem &&
                    isNotAtBeginning && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                val query = binding.searchEdit.text.toString().trim()
                if (query.isNotEmpty()) {
                    newsViewModel.searchNews(query)
                }
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                isScrolling = true
            }
        }
    }

    private fun setupSearchRecycler() {
        newsAdapter = NewsAdapter()
        binding.recyclerSearch.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchFragment.scrollListener)
        }
    }
}