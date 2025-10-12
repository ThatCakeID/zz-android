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
import com.thatcakeid.zrytezene.ZryteApp.Companion.getPrettyDateFormat
import com.thatcakeid.zrytezene.ZryteApp.Companion.getPrettyPlaysCount
import com.thatcakeid.zrytezene.R
import com.thatcakeid.zrytezene.data.MusicEntry

class HomeItemsRecyclerViewAdapter(
    private var context: Context,
    private var musics: List<MusicEntry>,
    private var users: Map<String, String>
) : RecyclerView.Adapter<HomeItemsRecyclerViewAdapter.ViewHolder>() {
    /**
     * Updates the items and users of this adapter.
     *
     * @param newMusics The new list of musics to use. If null, musics on this instance will not be updated.
     * @param newUsers The new map of users to use. If null, users on this instance will not be updated.
     */
    fun updateItems(
        newMusics: List<MusicEntry>?,
        newUsers: Map<String, String>?
    ) {
        if (newMusics != null) {
            musics = newMusics
        }
        if (newUsers != null) {
            users = newUsers
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bottomText = (
                if (users.containsKey(musics[position].authorUserId)) users[musics[position].authorUserId]
                else musics[position].authorUserId).toString() + " • " +

                getPrettyPlaysCount(musics[position].playCount) + " • " +
                getPrettyDateFormat(musics[position].time)

        holder.musicNameItem.text = musics[position].title
        holder.uploaderItem.text = bottomText

        if (musics[position].thumb == null) {
            // TODO: Use default image here
        } else {
            Glide.with(context)
                .load(musics[position].thumb)
                .into(holder.imageOverlay)
        }
    }

    override fun getItemCount(): Int = musics.size

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

    /**
     * Sets the click listener for the adapter.
     *
     * @param clickListener The click listener for the adapter.
     */
    @Suppress("Unused")
    fun setOnItemClickListener(clickListener: ClickListener) {
        Companion.clickListener = clickListener
    }

    interface ClickListener {
        /**
         * Called when an item is clicked.
         *
         * @param position The position of the item that was clicked.
         * @param v The view that was clicked.
         */
        fun onItemClick(position: Int, v: View)
        /**
         * Called when an item is long clicked.
         *
         * @param position The position of the item that was long clicked.
         * @param v The view that was long clicked.
         */
        fun onItemLongClick(position: Int, v: View)
    }

    companion object {
        private var clickListener: ClickListener? = null
    }
}