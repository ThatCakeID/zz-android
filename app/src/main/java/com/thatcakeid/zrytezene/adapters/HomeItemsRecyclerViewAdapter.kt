package com.thatcakeid.zrytezene.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.thatcakeid.zrytezene.HelperClass.Companion.getPrettyDateFormat
import com.thatcakeid.zrytezene.HelperClass.Companion.getPrettyPlaysCount
import com.thatcakeid.zrytezene.R
import com.thatcakeid.zrytezene.data.MusicEntry

class HomeItemsRecyclerViewAdapter(
    private var mContext: Context,
    private var items: List<MusicEntry>,
    private var users: Map<String, String>
) : RecyclerView.Adapter<HomeItemsRecyclerViewAdapter.ViewHolder>() {
    fun updateItems(
        mContext: Context,
        items: List<MusicEntry>?,
        users: Map<String, String>?
    ) {
        this.mContext = mContext.applicationContext

        if (items != null) {
            this.items = items
        }
        if (users != null) {
            this.users = users
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bottomText = (
                if (users.containsKey(items[position].authorUserId)) users[items[position].authorUserId]
                else items[position].authorUserId).toString() + " • " +

                getPrettyPlaysCount(items[position].plays) + " • " +
                getPrettyDateFormat(items[position].time)

        holder.musicNameItem.text = items[position].title
        holder.uploaderItem.text = bottomText

        if (items[position].thumb == null) {
            holder.imageOverlay.setImageResource(R.drawable.ic_zrytezene)
        } else {
            Glide.with(mContext)
                .load(items[position].thumb)
                .into(holder.imageOverlay)
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView), View.OnClickListener, OnLongClickListener {

        var imageOverlay: ImageView = itemView.findViewById(R.id.image_overlay)
        var musicNameItem: TextView = itemView.findViewById(R.id.musicname_item)
        var uploaderItem: TextView = itemView.findViewById(R.id.uploader_item)

        override fun onClick(v: View) {
            clickListener?.onItemClick(bindingAdapterPosition, v)
        }

        override fun onLongClick(v: View): Boolean {
            clickListener?.onItemLongClick(bindingAdapterPosition, v)
            return false
        }

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }
    }

    fun setOnItemClickListener(clickListener: ClickListener) {
        Companion.clickListener = clickListener
    }

    interface ClickListener {
        fun onItemClick(position: Int, v: View)
        fun onItemLongClick(position: Int, v: View)
    }

    companion object {
        private var clickListener: ClickListener? = null
    }
}