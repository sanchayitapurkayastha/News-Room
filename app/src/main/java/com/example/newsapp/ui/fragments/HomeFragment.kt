package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.database.ArticlesDatabase
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.ui.adapter.NewsAdapter
import com.example.newsapp.ui.viewmodel.NewsViewModel
import com.example.newsapp.ui.viewmodel.NewsViewModelProviderFactory
import com.example.newsapp.util.Constants
import com.example.newsapp.util.NewsApplication
import com.example.newsapp.util.Resource
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var viewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter

    var isLoading = false   //are we loading
    var isLastPage = false  // are we in the last page
    var isScrolling = false // are we scrolling

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newsRepository = ArticlesDatabase(context)?.let { NewsRepository(it) }
        val appCtx : NewsApplication = activity?.application as NewsApplication
        val viewModelProviderFactory = newsRepository?.let {
            NewsViewModelProviderFactory(appCtx, it)
        }
        viewModel = viewModelProviderFactory?.let {
            ViewModelProvider(requireActivity(), it).get(NewsViewModel::class.java)
        }!!

        setRecyclerView()

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply { putSerializable("article", it) }
            findNavController().navigate(R.id.action_homeFragment_to_articleFragment, bundle)
        }

        viewModel.getNews("us")
        viewModel.news.observe(viewLifecycleOwner, { response ->
            when (response) {

                is Resource.Success -> {
                    isLoading = false
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.newsPage == totalPages
                        if (isLastPage)
                            recycle_home.setPadding(0, 0, 0, 0)
                    }
                }

                is Resource.Error -> {
                    isLoading = false
                    response.message?.let { message ->
                        Toast.makeText(activity, "An error occured: $message", Toast.LENGTH_SHORT).show()
                    }
                }

                else -> isLoading = true
            }
        })
    }

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
                viewModel.getNews ("us")
                isScrolling = false
            }
        }
    }

    private fun setRecyclerView () {
        newsAdapter = NewsAdapter()
        recycle_home.setHasFixedSize(true)
        recycle_home.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager (activity)
            addOnScrollListener(this@HomeFragment.scrollListener)
        }
    }

}