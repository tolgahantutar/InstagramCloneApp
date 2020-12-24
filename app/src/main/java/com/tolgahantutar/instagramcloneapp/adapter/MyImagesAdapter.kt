package com.tolgahantutar.instagramcloneapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tolgahantutar.instagramcloneapp.R
import com.tolgahantutar.instagramcloneapp.fragments.PostDetailFragment
import com.tolgahantutar.instagramcloneapp.model.Post

class MyImagesAdapter (private val mContext: Context,private val mPost: List<Post>)
    :RecyclerView.Adapter<MyImagesAdapter.ViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyImagesAdapter.ViewHolder {
    val view = LayoutInflater.from(mContext).inflate(R.layout.images_item_layout,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyImagesAdapter.ViewHolder, position: Int) {
        val post: Post = mPost[position]
        Picasso.get().load(post.postimage).into(holder.postImage)

        holder.postImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("postId",post.postid)
            editor.apply()
            (mContext as FragmentActivity).getSupportFragmentManager().beginTransaction().
            replace(R.id.fragment_container, PostDetailFragment()).commit()
        }
    }

    override fun getItemCount(): Int = mPost.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var postImage: ImageView = itemView.findViewById(R.id.post_image)
    }

}