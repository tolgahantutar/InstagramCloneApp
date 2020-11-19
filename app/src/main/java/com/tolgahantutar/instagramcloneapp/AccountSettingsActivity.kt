package com.tolgahantutar.instagramcloneapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.tolgahantutar.instagramcloneapp.model.User
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class AccountSettingsActivity : AppCompatActivity() {
    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        logout_btn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this,SigninActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        save_info_profile_btn.setOnClickListener {
            if(checker == "clicked")
            else{
                updateUserInfoOnly()
            }
        }
        getUserInfo()
    }

    private fun updateUserInfoOnly() {
        when {
            full_name_profile_frag.text.toString()=="" -> {
                Toast.makeText(this, "Please write fullname first", Toast.LENGTH_SHORT).show()
            }
            username_profile_frag.text.toString()=="" -> {
                Toast.makeText(this, "Please write username first", Toast.LENGTH_SHORT).show()
            }
            bio_profile_frag.text.toString()=="" -> {
                Toast.makeText(this, "Please write bio first", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
                val userMap = HashMap<String,Any>()
                userMap["fullname"] = full_name_profile_frag.text.toString().toLowerCase()
                userMap["username"] = username_profile_frag.text.toString().toLowerCase()
                userMap["bio"] = bio_profile_frag.text.toString().toLowerCase()

                usersRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(this, "Account information has been updated successfully", Toast.LENGTH_SHORT).show()

                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()
                  }
           }
        }

    private fun getUserInfo(){
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.uid)

        usersRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(profile_image_view_profile_frag)
                    username_profile_frag.setText(user!!.username)
                    full_name_profile_frag.setText(user!!.fullname)
                    bio_profile_frag.setText(user!!.bio)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}