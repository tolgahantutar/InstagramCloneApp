package com.tolgahantutar.instagramcloneapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.tolgahantutar.instagramcloneapp.adapter.CommentsAdapter
import com.tolgahantutar.instagramcloneapp.model.Comment
import com.tolgahantutar.instagramcloneapp.model.User
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_comments.*

class CommentsActivity : AppCompatActivity() {

    private var postId = ""
    private var publisherId = ""
    private var firebaseUser: FirebaseUser? = null
    private var commentsAdapter: CommentsAdapter? = null
    private var commentList: MutableList<Comment>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)


        val intent = intent

        postId = intent.getStringExtra("postId").toString()
        publisherId = intent.getStringExtra("publisherId").toString()

        firebaseUser = FirebaseAuth.getInstance().currentUser

        var recyclerView: RecyclerView
        recyclerView = findViewById(R.id.recyclerview_comments)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerView.layoutManager = linearLayoutManager

        commentList = ArrayList()
        commentsAdapter = CommentsAdapter(this,commentList)
        recyclerView.adapter = commentsAdapter

        getUserInfo()
        readComments()
        getPostImage()

        post_comment.setOnClickListener {
        if (add_comment.text.toString() == ""){
            Toast.makeText(this, "Please write comment first", Toast.LENGTH_LONG).show()
        }else{
            addComment()
        }
        }
    }

    private fun addComment() {
        val commentsRef =
            FirebaseDatabase.getInstance().reference.child("Comments")
                .child(postId)

        val commentsMap = HashMap<String,Any>()

        commentsMap["comment"] = add_comment.text.toString()
        commentsMap["publisher"] = firebaseUser!!.uid

        commentsRef.push().setValue(commentsMap)

        addNotification()

        add_comment.text.clear()
    }

    private fun getUserInfo() {
        val usersRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile)
                        .into(profile_image_comment)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    private fun getPostImage() {
        val postRef =
            FirebaseDatabase.getInstance().reference.child("Posts").child(postId!!).child("postimage")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val image = snapshot.value.toString()

                    Picasso.get().load(image).placeholder(R.drawable.profile)
                        .into(post_image_comment)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    private fun readComments(){
        val commentsRef = FirebaseDatabase.getInstance().
        reference.child("Comments")
        .child(postId)

        commentsRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot)
            {
            if (snapshot.exists()){
                commentList!!.clear()

                for(snapshot in snapshot.children){
                    val comments = snapshot.getValue(Comment::class.java)
                    commentList!!.add(comments!!)
                }
                commentsAdapter!!.notifyDataSetChanged()
            }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    private fun addNotification(){
        val notiRef = FirebaseDatabase.getInstance().
        reference.child("Notifications")
            .child(publisherId)

        val notiMap = HashMap<String,Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "commented: " + add_comment.text.toString()
        notiMap["postid"] = postId
        notiMap["ispost"] = true

        notiRef.push().setValue(notiMap)
    }
}