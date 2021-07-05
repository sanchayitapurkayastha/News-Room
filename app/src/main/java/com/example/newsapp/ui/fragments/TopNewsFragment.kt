package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.ui.activity.NewsActivity
import com.example.newsapp.ui.adapter.NewsAdapter
import com.example.newsapp.ui.viewmodel.NewsViewModel
import com.example.newsapp.util.Constants.Companion.QUERY_PAGE_SIZE
import com.example.newsapp.util.Resource
import kotlinx.android.synthetic.main.fragment_top_news.*


class TopNewsFragment : Fragment() {
    private lateinit var viewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        setRecyclerView()

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply { putSerializable("article", it) }
            findNavController().navigate( R.id.action_homeFragment_to_articleFragment, bundle )
        }

        viewModel.news.observe(viewLifecycleOwner, { response->
            when (response) {
                is Resource.Success -> {
                    isLoading = false
                    response.data?.let {newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.newsPage == totalPages
                        if(isLastPage)
                            recycle_top_news.setPadding(0, 0, 0, 0)
                    }
                }
                is Resource.Error -> {
                    isLoading = false
                    response.message?.let { message ->
                        Toast.makeText(activity,"An error occured: $message",Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Loading -> {
                    isLoading = true
                }
            }
        })
    }

    var isLoading = false   //are we loading
    var isLastPage = false  // are we in the last page
    var isScrolling = false // are we scrolling

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                isScrolling = true
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItems = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItems
            val isNotFirst = firstVisibleItemPosition >= 0
            val isTotalMorethanVisible = totalItems >= QUERY_PAGE_SIZE
            val paginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotFirst
                    && isTotalMorethanVisible && isScrolling
            if (paginate) {
                viewModel.getNews ("us")
                isScrolling = false
            }
        }
    }

    private fun setRecyclerView () {
        newsAdapter = NewsAdapter()
        recycle_top_news.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager (activity)
            addOnScrollListener(this@TopNewsFragment.scrollListener)
        }
    }
}