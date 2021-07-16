package com.example.newsapp.ui.fragments

import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.database.ArticlesDatabase
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.ui.adapter.NewsAdapter
import com.example.newsapp.ui.viewmodel.NewsViewModel
import com.example.newsapp.ui.viewmodel.NewsViewModelProviderFactory
import com.example.newsapp.util.NewsApplication
import com.google.android.material.snackbar.Snackbar
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
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

        val newsRepository = ArticlesDatabase(context)?.let { NewsRepository(it) }
        val appCtx : NewsApplication = activity?.application as NewsApplication
        val viewModelProviderFactory = newsRepository?.let {
            NewsViewModelProviderFactory(appCtx, it)
        }
        viewModel = viewModelProviderFactory?.let {
            ViewModelProvider(requireActivity(),
                it
            ).get(NewsViewModel::class.java)
        }!!

        setRecyclerView()

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply { putSerializable("article", it) }
            findNavController().navigate( R.id.action_savedFragment_to_articleFragment, bundle )
        }

        //swipe delete
        val callback = object : ItemTouchHelper.SimpleCallback (
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

                Snackbar.make(view, "Successfully deleted article", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        viewModel.saveArticle(articleToDelete)
                    }
                    show()
                }
            }

            override fun onChildDraw( c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

                context?.let { ContextCompat.getColor(it, R.color.red_600) }?.let {
                    RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder,
                        dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(it)
                        .addActionIcon(R.drawable.ic_delete)
                        .create()
                        .decorate()
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(callback).apply { attachToRecyclerView(recycle_saved) }

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