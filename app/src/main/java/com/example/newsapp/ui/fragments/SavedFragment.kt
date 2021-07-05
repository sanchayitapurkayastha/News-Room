package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.ui.activity.NewsActivity
import com.example.newsapp.ui.adapter.NewsAdapter
import com.example.newsapp.ui.viewmodel.NewsViewModel
import kotlinx.android.synthetic.main.fragment_saved.*

class SavedFragment : Fragment(R.layout.fragment_saved) {
    private lateinit var viewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saved, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        setRecyclerView()

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply { putSerializable("article", it) }
            findNavController().navigate( R.id.action_savedFragment_to_articleFragment, bundle )
        }

        //swipe delete
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback (
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val articleToDelete = newsAdapter.differ.currentList[position]
                viewModel.deleteArticle(articleToDelete)
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(recycle_saved)
        }

        //update our recycler view list
        viewModel.getSavedArticle().observe(viewLifecycleOwner, { articles ->
            newsAdapter.differ.submitList(articles)
        })
    }

    private fun setRecyclerView () {
        newsAdapter = NewsAdapter()
        recycle_saved.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager (activity)
        }
    }
}