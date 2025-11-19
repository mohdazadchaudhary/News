package com.example.news.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.news.R
import com.example.news.models.Article

// Move differCallback and differ INSIDE the class (they can't be top-level here)
class NewsAdapter : RecyclerView.Adapter<NewsAdapter.ArticleViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }
    }

    // this = the adapter instance
    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = differ.currentList[position]

        // Initialize the lateinit vars from the ViewHolder
        holder.articleImage = holder.itemView.findViewById(R.id.articleImage)
        holder.articleTitle = holder.itemView.findViewById(R.id.articleTitle)
        holder.articleDescription = holder.itemView.findViewById(R.id.articleDescription)
        holder.articleSource = holder.itemView.findViewById(R.id.articleSource)
        holder.articleDateTime = holder.itemView.findViewById(R.id.articleDateTime)

        holder.itemView.apply {
            Glide.with(this).load(article.urlToImage).into(holder.articleImage)
            holder.articleTitle.text = article.title
            holder.articleDescription.text = article.description
            holder.articleSource.text = article.source.name
            holder.articleDateTime.text = article.publishedAt
            setOnClickListener {
                onItemClickListener?.let { it(article) }
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size     // Fixed: .size (not .Size)
    }

    private var onItemClickListener: ((Article) -> Unit)? = null

    // Optional setter so you can set the listener from outside
    fun setOnItemClickListener(listener: (Article) -> Unit) {
        onItemClickListener = listener
    }

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var articleImage: ImageView
        lateinit var articleTitle: TextView
        lateinit var articleDescription: TextView
        lateinit var articleSource: TextView
        lateinit var articleDateTime: TextView
    }
}