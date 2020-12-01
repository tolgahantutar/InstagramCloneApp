package com.tolgahantutar.instagramcloneapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.tolgahantutar.instagramcloneapp.R
import com.tolgahantutar.instagramcloneapp.model.Post
import com.tolgahantutar.instagramcloneapp.model.User

class PostAdapter(private val mContext: Context,
                  private val mPost: List<Post>): RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(mContext).inflate(R.layout.post_layout,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    firebaseUser = FirebaseAuth.getInstance().currentUser

        val post= mPost[position]

        Picasso.get().load(post.postimage).into(holder.postImage)

        publisherInfo(holder.profileImage,holder.userName,holder.publisher,post.publisher)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView){
        var profileImage = itemView.findViewById<ImageView>(R.id.user_profile_image_post)
        var postImage = itemView.findViewById<ImageView>(R.id.post_image_home)
        var likeButton = itemView.findViewById<ImageView>(R.id.post_image_like_btn)
        var commentButton = itemView.findViewById<ImageView>(R.id.post_image_comment_btn)
        var saveButton = itemView.findViewById<ImageView>(R.id.post_save_comment_btn)
        var userName = itemView.findViewById<TextView>(R.id.user_name_search)
        var likes = itemView.findViewById<TextView>(R.id.likes)
        var publisher = itemView.findViewById<TextView>(R.id.publisher)
        var description = itemView.findViewById<TextView>(R.id.description)
        var comments = itemView.findViewById<TextView>(R.id.comments)
    }
    private fun publisherInfo(profileImage: ImageView?, userName: TextView?, publisher: TextView?, publisherID: String) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)

        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)
                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(profileImage)
                    userName!!.text = user.username
                    publisher!!.text = user.fullname
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}