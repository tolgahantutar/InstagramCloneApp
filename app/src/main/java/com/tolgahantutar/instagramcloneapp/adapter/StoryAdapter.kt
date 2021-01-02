package com.tolgahantutar.instagramcloneapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.tolgahantutar.instagramcloneapp.AddStoryActivity
import com.tolgahantutar.instagramcloneapp.MainActivity
import com.tolgahantutar.instagramcloneapp.R
import com.tolgahantutar.instagramcloneapp.StoryActivity
import com.tolgahantutar.instagramcloneapp.model.Story
import com.tolgahantutar.instagramcloneapp.model.User
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile.view.*

class StoryAdapter (private val mContext: Context,
                    private val mStory: List<Story>) :
RecyclerView.Adapter<StoryAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      return if (viewType == 0){
           val view = LayoutInflater.from(mContext).inflate(R.layout.add_story_item,parent,false)
            StoryAdapter.ViewHolder(view)
       }else{
           val view = LayoutInflater.from(mContext).inflate(R.layout.story_item,parent,false)
            StoryAdapter.ViewHolder(view)
       }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val story = mStory[position]

        getUserInfo(holder, story.userid, position)

        if (holder.adapterPosition !== 0){
            seenStory(holder, story.userid)
        }
        if (holder.adapterPosition === 0){
            myStories(holder.addStory_text, holder.story_plus_btn, false)
        }

        holder.itemView.setOnClickListener {
           if (holder.adapterPosition === 0){
               myStories(holder.addStory_text, holder.story_plus_btn, true)
           }else{
               val intent = Intent(mContext, StoryActivity::class.java)
               intent.putExtra("userId",story.userid)
               mContext.startActivity(intent)
           }

        }
    }

    override fun getItemCount(): Int {
    return mStory.size
    }


    class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView){
        //StoryItem
        var story_image_seen = itemView.findViewById<CircleImageView>(R.id.story_image_seen)
        var story_image = itemView.findViewById<CircleImageView>(R.id.story_image)
        var story_username = itemView.findViewById<TextView>(R.id.story_username)

        //AddStoryItem
        var story_plus_btn = itemView.findViewById<ImageView>(R.id.story_add)
        var addStory_text = itemView.findViewById<TextView>(R.id.add_story_text)
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0){
            return 0
        }else{
            return 1
        }
    }
    private fun getUserInfo(viewHolder: ViewHolder, userId: String, position: Int){
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId)

        usersRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(viewHolder.story_image)
                    if (position!=0){
                        Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(viewHolder.story_image_seen)
                        viewHolder.story_username.text = user.username

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun myStories(text: TextView, imageView: ImageView, click: Boolean){
        val storyRef = FirebaseDatabase.getInstance().reference
            .child("Story").child(FirebaseAuth.getInstance().currentUser!!.uid)

        storyRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {
            var counter = 0
                var timeCurrent = System.currentTimeMillis()
                for (snapshot in datasnapshot.children){
                    val story = snapshot.getValue(Story::class.java)

                    if (timeCurrent>story!!.timestart && timeCurrent<story.timeend){
                        counter++
                    }
                }
                if (click){
                    if (counter>0){
                    val alertDialog = AlertDialog.Builder(mContext).create()

                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "View Story"){
                            dialogInterface, which ->
                            val intent = Intent(mContext,Story::class.java)
                            intent.putExtra("userId",FirebaseAuth.getInstance().currentUser!!.uid)
                            mContext.startActivity(intent)
                            dialogInterface.dismiss()
                        }
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story"){
                                dialogInterface, which ->
                            val intent = Intent(mContext,AddStoryActivity::class.java)
                            intent.putExtra("userId",FirebaseAuth.getInstance().currentUser!!.uid)
                            mContext.startActivity(intent)
                            dialogInterface.dismiss()
                        }
                        alertDialog.show()
                    }
                    else{
                        val intent = Intent(mContext,AddStoryActivity::class.java)
                        intent.putExtra("userId",FirebaseAuth.getInstance().currentUser!!.uid)
                        mContext.startActivity(intent)

                    }
                }
                else{
                    if (counter > 0){
                        text.text = "My Story"
                        imageView.visibility = View.GONE
                    }else{
                        text.text = "Add Story"
                        imageView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    private fun seenStory(viewHolder: ViewHolder, userId: String){
        val storyRef = FirebaseDatabase.getInstance().reference
            .child("Story").child(userId)

        storyRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
            var i = 0
                for (snapshot in snapshot.children){
                    if (!snapshot.child("views").
                        child(FirebaseAuth.getInstance().currentUser!!.uid).exists()&&
                            System.currentTimeMillis()<snapshot.getValue(Story::class.java)!!.timeend){
                        i++
                    }
                }
                if (i>0){
                    viewHolder.story_image.visibility = View.VISIBLE
                    viewHolder.story_image_seen.visibility = View.GONE
                }else{
                    viewHolder.story_image.visibility = View.GONE
                    viewHolder.story_image_seen.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}