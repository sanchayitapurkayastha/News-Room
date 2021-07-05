package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.ui.activity.NewsActivity
import com.example.newsapp.ui.adapter.NewsAdapter
import com.example.newsapp.ui.viewmodel.NewsViewModel
import com.example.newsapp.util.Constants
import com.example.newsapp.util.Resource
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class
SearchFragment : Fragment(R.layout.fragment_search) {
    private lateinit var viewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        setRecyclerView()

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply { putSerializable("article", it) }
            findNavController().navigate( R.id.action_searchFragment_to_articleFragment, bundle )
        }

        //creating delay in the search
        var job: Job? = null
        search_bar.addTextChangedListener {editable ->
            job?.cancel()   //cancel current job & start new job
            job = MainScope().launch {
                delay(500L)
                editable?.let {
                    if (editable.toString().isNotEmpty())
                        viewModel.searchNews(editable.toString())
                }
            }
        }

        viewModel.search.observe(viewLifecycleOwner, { response->
            when (response) {
                is Resource.Success -> {
                    isLoading = false
                    response.data?.let {newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.searchPage == totalPages
                        if(isLastPage)
                            recycle_search.setPadding(0, 0, 0, 0)

                    }
                }
                is Resource.Error -> {
                    isLoading = false
                    response.message?.let { message ->
                        Toast.makeText(activity,"An error occured: $message", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Loading -> {
                    isLoading = true
                }
            }
        })
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

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
            val isTotalMorethanVisible = totalItems >= Constants.QUERY_PAGE_SIZE
            val paginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotFirst
                    && isTotalMorethanVisible && isScrolling
            if (paginate) {
                viewModel.searchNews (search_bar.text.toString())
                isScrolling = false
            }
        }
    }

    private fun setRecyclerView () {
        newsAdapter = NewsAdapter()
        recycle_search.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager (activity)
            addOnScrollListener(this@SearchFragment.scrollListener)
        }
    }
}