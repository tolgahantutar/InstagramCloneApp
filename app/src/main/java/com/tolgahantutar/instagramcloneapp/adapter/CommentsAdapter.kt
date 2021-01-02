package com.tolgahantutar.instagramcloneapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.tolgahantutar.instagramcloneapp.model.Comment
import com.tolgahantutar.instagramcloneapp.model.User
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.comments_item_layout.view.*

class CommentsAdapter (private val mContext: Context,
                       private val mComment: MutableList<Comment>?
):RecyclerView.Adapter<CommentsAdapter.ViewHolder>(){

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.comments_item_layout, parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentsAdapter.ViewHolder, position: Int) {
    firebaseUser = FirebaseAuth.getInstance().currentUser

    val comment = mComment!![position]
    holder.commentTV.text = comment.comment
    getUserInfo(holder.imageProfile, holder.userNameTV, comment.publisher)
    }

    override fun getItemCount(): Int {
    return mComment!!.size
    }

    inner class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView){
    var imageProfile: CircleImageView = itemView.findViewById(R.id.user_profile_image_comment)
    var userNameTV: TextView = itemView.findViewById(R.id.user_name_comment)
    var commentTV: TextView = itemView.findViewById(R.id.comment_comment)
    }

    private fun getUserInfo(imageProfile: CircleImageView, userNameTV: TextView, publisher: String) {
    val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisher)
        userRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)
                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(imageProfile)
                    userNameTV.text = user.username
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}