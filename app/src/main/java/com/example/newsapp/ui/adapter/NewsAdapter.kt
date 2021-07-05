package com.example.newsapp.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.R
import com.example.newsapp.model.Article
import kotlinx.android.synthetic.main.layout_item.view.*

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback <Article> (){
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer (this, diffCallback)

    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.layout_item, null, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = differ.currentList[position]

        holder.itemView.apply {
            heading.text = article.title
            content.text = article.description
            Glide.with(this).load(article.urlToImage).into(image)
            src.text = article.source?.name
            time.text = article.publishedAt

            setOnItemClickListener {
                onItemClickListener.let {
                    if (it != null) {
                        it(article)
                    }
                }
            }
        }
    }

    private var onItemClickListener: ((Article) -> Unit)? = null
    fun setOnItemClickListener (listener: (Article)->Unit) {
        onItemClickListener = listener
    }
    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    class ViewHolder (itemView: View): RecyclerView.ViewHolder(itemView)
}