package com.tolgahantutar.instagramcloneapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.tolgahantutar.instagramcloneapp.CommentsActivity
import com.tolgahantutar.instagramcloneapp.MainActivity
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

        if(post.description.equals("")){
            holder.description.visibility = View.GONE
        }else{
            holder.description.visibility = View.VISIBLE
            holder.description.text = post.description
        }

        publisherInfo(holder.profileImage,holder.userName,holder.publisher,post.publisher)
        isLikes(post.postid,  holder.likeButton)
        numberOfLikes(holder.likes, post.postid)
        numberOfComments(holder.comments, post.postid)

        holder.likeButton.setOnClickListener {
            if(holder.likeButton.tag == "Like"){
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.postid)
                    .child(firebaseUser!!.uid)
                    .setValue(true)
            }else{
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.postid)
                    .child(firebaseUser!!.uid)
                    .removeValue()
                /*val intent = Intent(mContext, MainActivity::class.java)
                mContext.startActivity(intent)*/
            }
        }
        holder.commentButton.setOnClickListener {
            val intent = Intent(mContext,CommentsActivity::class.java)
            intent.putExtra("postId",post.postid)
            intent.putExtra("publisherId",post.publisher)
            mContext.startActivity(intent)
        }
        holder.comments.setOnClickListener {
            val intent = Intent(mContext,CommentsActivity::class.java)
            intent.putExtra("postId",post.postid)
            intent.putExtra("publisherId",post.publisher)
            mContext.startActivity(intent)
        }
    }

        private fun numberOfLikes(likes: TextView?, postid: String) {
            val LikesRef =  FirebaseDatabase.getInstance().reference
                .child("Likes").child(postid)

            LikesRef.addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                       likes!!.text = snapshot.childrenCount.toString() + " likes"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    private fun numberOfComments(comments: TextView?, postid: String) {
        val commentsRef =  FirebaseDatabase.getInstance().reference
            .child("Comments").child(postid)

        commentsRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    comments!!.text = "view all "+snapshot.childrenCount.toString() + " comments"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun isLikes(postid: String, likeButton: ImageView?) {
    val firebaseUser = FirebaseAuth.getInstance().currentUser

        val LikesRef =  FirebaseDatabase.getInstance().reference
            .child("Likes").child(postid)

        LikesRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
            if(snapshot.child(firebaseUser!!.uid).exists()){
                likeButton!!.setImageResource(R.drawable.heart_clicked)
                likeButton.tag = "Liked"
            }else{
                likeButton!!.setImageResource(R.drawable.heart_not_clicked)
                likeButton.tag = "Like"
            }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
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