package com.tolgahantutar.instagramcloneapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.tolgahantutar.instagramcloneapp.AccountSettingsActivity
import com.tolgahantutar.instagramcloneapp.R
import com.tolgahantutar.instagramcloneapp.adapter.MyImagesAdapter
import com.tolgahantutar.instagramcloneapp.model.Post
import com.tolgahantutar.instagramcloneapp.model.User
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*
import kotlin.collections.ArrayList

class ProfileFragment : Fragment() {
    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    var postList: List<Post>? = null
    var myImagesAdapter: MyImagesAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)
        if(pref != null)
        {
            this.profileId = pref.getString("profileId" , "none").toString()
        }
        if(profileId == firebaseUser.uid)
        {
            view.edit_account_settings_btn.text = "Edit Profile"
        }else if(profileId != firebaseUser.uid){
            checkFollowAndFollowingButtonStatus()
        }

        var recyclerViewUploadImages: RecyclerView
        recyclerViewUploadImages = view.findViewById(R.id.recycler_view_upload_pic)
        recyclerViewUploadImages.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(requireContext(),3)
        recyclerViewUploadImages.layoutManager = linearLayoutManager
        postList = ArrayList()
        myImagesAdapter = MyImagesAdapter(requireContext(), postList as ArrayList<Post>)
        recyclerViewUploadImages.adapter = myImagesAdapter

        view.edit_account_settings_btn.setOnClickListener {
            val getButtonText = view.edit_account_settings_btn.text.toString()
            when{
                getButtonText == "Edit Profile" ->startActivity(Intent(context,AccountSettingsActivity::class.java))
                getButtonText == "Follow"->{
                    firebaseUser?.uid.let{it1->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1)
                            .child("Following").child(profileId)
                            .setValue(true)
                    }
                    firebaseUser?.uid.let{it1->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1)
                            .setValue(true)
                    }
                }
                getButtonText == "Following"->{
                    firebaseUser?.uid.let{it1->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1)
                            .child("Following").child(profileId)
                            .removeValue()
                    }
                    firebaseUser?.uid.let{it1->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1)
                            .removeValue()
                    }
                }
            }
        }
        getFollowers()
        getFollowing()
        getUserInfo()
        myPhotos()
        return view
    }

    private fun checkFollowAndFollowingButtonStatus() {
        val followingRef = firebaseUser?.uid.let {it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }
        if(followingRef != null){
            followingRef.addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.child(profileId).exists()){
                        view?.edit_account_settings_btn?.text = "Following"
                    }else{
                        view?.edit_account_settings_btn?.text = "Follow"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }
    private fun getFollowers(){
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Followers")
        followersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    view?.total_followers?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    private fun getFollowing(){
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Following")

        followersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    view?.total_following?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun myPhotos(){
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    (postList as ArrayList<Post>).clear()
                    for (snapshot in snapshot.children){
                        val post = snapshot.getValue(Post::class.java)!!
                        if (post.publisher.equals(profileId)){
                            (postList as ArrayList<Post>).add(post)
                        }
                        Collections.reverse(postList)
                        myImagesAdapter!!.notifyDataSetChanged()

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    private fun getUserInfo(){
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)

        usersRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(view?.pro_image_profile_frag)
                    view?.profile_fragment_username?.text = user!!.username
                    view?.full_name_profile_frag?.text = user.fullname
                    view?.bio_profile_frag?.text = user.bio
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()
        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",firebaseUser.uid)
        pref?.apply()
    }
}