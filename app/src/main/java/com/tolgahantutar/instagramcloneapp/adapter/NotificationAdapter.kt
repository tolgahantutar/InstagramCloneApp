package com.tolgahantutar.instagramcloneapp.adapter

import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.tolgahantutar.instagramcloneapp.R
import com.tolgahantutar.instagramcloneapp.fragments.PostDetailFragment
import com.tolgahantutar.instagramcloneapp.fragments.ProfileFragment
import com.tolgahantutar.instagramcloneapp.model.Notification
import com.tolgahantutar.instagramcloneapp.model.Post
import com.tolgahantutar.instagramcloneapp.model.User
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class NotificationAdapter (private val mContext: Context,
                           private val mNotification: List<Notification>)
                            : RecyclerView.Adapter<NotificationAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.notifications_item_layout, parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val notification = mNotification[position]


        if (notification.text.equals("started following you")){
            holder.text.text = "started following you"
        }
         else if (notification.text.equals("liked your post")){
            holder.text.text = "liked your post"
        }
        else if (notification.text.contains("commented:")){
            holder.text.text = notification.text.replace("commented:","commented: ")
        }
        else{
            holder.text.text = notification.text
        }



        getUserInfo(holder.profileImage, holder.userName, notification.userid)

        if (notification.ispost){
            holder.postImage.visibility = View.VISIBLE
            getPostImage(holder.postImage, notification.postid)
        }else{
            holder.postImage.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (notification.ispost){
                val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                editor.putString("postId",notification.postid)
                editor.apply()
                (mContext as FragmentActivity).getSupportFragmentManager().beginTransaction().
                replace(R.id.fragment_container, PostDetailFragment()).commit()
            }else{
                val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                editor.putString("profileId",notification.userid)
                editor.apply()
                (mContext as FragmentActivity).getSupportFragmentManager().beginTransaction().
                replace(R.id.fragment_container, ProfileFragment()).commit()
            }
        }

    }

    override fun getItemCount(): Int {
    return mNotification.size
    }


    inner class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView){
        var postImage: ImageView = itemView.findViewById(R.id.notification_post_image)
        var profileImage: CircleImageView = itemView.findViewById(R.id.notification_profile_image)
        var userName: TextView = itemView.findViewById(R.id.username_notification)
        var text: TextView = itemView.findViewById(R.id.comment_notification)
    }

    private fun getUserInfo(imageView: ImageView, userName: TextView, publisherId: String){
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(publisherId)


        usersRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(imageView)
                    userName.text = user.username

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    private fun getPostImage(imageView: ImageView, postID: String) {
        val postRef =
            FirebaseDatabase.getInstance().reference.child("Posts").
            child(postID)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val post = snapshot.getValue<Post>(Post::class.java)

                    Picasso.get().load(post!!.postimage).placeholder(R.drawable.profile)
                        .into(imageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}