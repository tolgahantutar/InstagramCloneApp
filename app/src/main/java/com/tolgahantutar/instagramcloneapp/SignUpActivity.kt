package com.tolgahantutar.instagramcloneapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_up.*
import android.app.ProgressDialog
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        signin_link_btn.setOnClickListener {
            startActivity(Intent(this,SigninActivity::class.java))
        }

        signup_btn.setOnClickListener {
            createAccount()
        }
    }

    private fun createAccount() {
        val fullName = fullname_signup.text.toString()
        val userName = username_signup.text.toString()
        val email = email_signup.text.toString()
        val password = password_signup.text.toString()
        when{
            TextUtils.isEmpty(fullName) -> Toast.makeText(this, "full name is required!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(userName) -> Toast.makeText(this, "user name is required!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this, "email is required!", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this, "password is required!", Toast.LENGTH_SHORT).show()
            else ->{
                val progressDialog = ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle("SignUp")
                progressDialog.setMessage("Please wait, this may take a while...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener { task->
                        if (task.isSuccessful){
                        saveUserInfo(fullName, userName, email,progressDialog)
                        }
                        else{
                            val message = task.exception!!.toString()
                            Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
        }
    }

    private fun saveUserInfo(fullName: String, userName: String, email: String, progressDialog: ProgressDialog) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String,Any>()
        userMap["uid"]=currentUserID
        userMap["fullname"] = fullName.toLowerCase()
        userMap["username"] = userName.toLowerCase()
        userMap["email"] = email
        userMap["bio"] = "Hey! I am using instagram clone :) "
        userMap["image"]="https://firebasestorage.googleapis.com/v0/b/instagram-clone-app-a4edb.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=94b790ba-0a3e-4b5a-a683-c9d54f2c39dd"

        usersRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account has been created successfully.", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@SignUpActivity,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }else{
                    val message = task.exception!!.toString()
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }
    }
}