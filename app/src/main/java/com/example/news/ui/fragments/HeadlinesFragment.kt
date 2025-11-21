package com.example.news.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news.NewsActivity
import com.example.news.R
import com.example.news.adapters.NewsAdapter
import com.example.news.databinding.FragmentHeadlineBinding
import com.example.news.databinding.ItemErrorBinding
import com.example.news.util.Constants
import com.example.news.util.NewsViewModel
import com.example.news.util.Resource

class HeadlinesFragment : Fragment() {

    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    lateinit var binding: FragmentHeadlineBinding

    // Fixed: Correct type for included layout
    private lateinit var itemHeadlinesErrorBinding: ItemErrorBinding
    private lateinit var retryButton: Button
    private lateinit var errorText: TextView

    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHeadlineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get ViewModel from Activity
        newsViewModel = (requireActivity() as NewsActivity).NewsViewModel

        // Get the included error layout binding
        itemHeadlinesErrorBinding = binding.itemHeadlinesError
        retryButton = itemHeadlinesErrorBinding.retryButton
        errorText = itemHeadlinesErrorBinding.errorText

        setupHeadlinesRecycler()

        // Item click → go to ArticleFragment
        newsAdapter.setOnItemClickListener { article ->
            val bundle = Bundle().apply {
                putSerializable("article", article)
            }
            findNavController().navigate(
                R.id.action_headlinesFragment_to_articleFragment,
                bundle
            )
        }

        // Observe headlines
        newsViewModel.headlines.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())

                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = newsViewModel.headlinesPage == totalPages

                        if (isLastPage) {
                            binding.recyclerHeadlines.setPadding(0, 0, 0, 0)
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
                    showProgressBar()
                }
            }
        }

        // Retry button
        retryButton.setOnClickListener {
            newsViewModel.getHeadlines("in")
        }

        // Pagination scroll listener
        binding.recyclerHeadlines.addOnScrollListener(scrollListener)
    }

    private fun setupHeadlinesRecycler() {
        newsAdapter = NewsAdapter()
        binding.recyclerHeadlines.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
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
        itemHeadlinesErrorBinding.root.visibility = View.VISIBLE
        itemHeadlinesErrorBinding.errorText.text = message
        isError = true
    }

    private fun hideErrorMessage() {
        itemHeadlinesErrorBinding.root.visibility = View.INVISIBLE
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
                newsViewModel.getHeadlines("in")
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
}