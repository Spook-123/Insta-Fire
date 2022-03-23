package com.example.instafirenew.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.instafirenew.MainActivity
import com.example.instafirenew.MainActivity.Companion.EXTRA_USER_NAME
import com.example.instafirenew.R
import com.example.instafirenew.daos.PostDao
import com.example.instafirenew.daos.UserDao
import com.example.instafirenew.models.Post
import com.example.instafirenew.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

class CreateActivity : AppCompatActivity() {

    companion object {
        private const val PICK_PHOTO_CODE = 4648
        private const val TAG = "CreateActivity"
    }

    private var photoUri: Uri? = null
    //private var signedInUser: User? = null
    //private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var storageReference:StorageReference
    private lateinit var postDao:PostDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        postDao = PostDao()
        storageReference = FirebaseStorage.getInstance().reference


        /**firestoreDb = FirebaseFirestore.getInstance()

        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(User::class.java)
                Log.i(TAG,"signed in user $signedInUser")
            }
            .addOnFailureListener { exception ->
                Log.i(TAG,"Failure fetching signed in user",exception)
            }**/




        btnPickImage.setOnClickListener {
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"
            if(imagePickerIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
            }
        }

        btnSubmit.setOnClickListener {
            handleSubmitButtonClick()
        }
    }

    private fun handleSubmitButtonClick() {
        if(photoUri == null) {
            Toast.makeText(this,"No photo selected", Toast.LENGTH_SHORT).show()
            return
        }
        if(etDescription.text.isBlank()) {
            Toast.makeText(this,"Description cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if( intent.getStringExtra(EXTRA_USER_NAME) == null) {
            Toast.makeText(this,"No signed in user,please wait", Toast.LENGTH_SHORT).show()
            return
        }
        btnSubmit.isEnabled = false
        val photoUploadUri = photoUri as Uri
        Toast.makeText(this,"Uploading please wait!!",Toast.LENGTH_SHORT).show()
        //Upload photo to Firebase Storage
        val imageFileName = "images/${System.currentTimeMillis()}-photo.jpg"
        val photoReference = storageReference.child(imageFileName).putFile(photoUploadUri)
        photoReference.addOnSuccessListener {

            val url = storageReference.child(imageFileName).downloadUrl
            Log.i(TAG,"url -> $url")

            url.addOnSuccessListener {
                Toast.makeText(this,"Uploaded Successfully",Toast.LENGTH_SHORT).show()
                Log.i(TAG,"url original -> $it")
                val text = etDescription.text.toString()
                val image  = it.toString()
                postDao.addPost(text,image)
                val profileIntent = Intent(this,MainActivity::class.java)
                //profileIntent.putExtra(EXTRA_USER_NAME, EXTRA_USER_NAME)
                startActivity(profileIntent)
                finish()
            }.addOnFailureListener {
                Log.i(TAG,"Failed --> $it")
                Toast.makeText(this,"Failed --> $it",Toast.LENGTH_LONG).show()
            }

        }.addOnFailureListener {
            Toast.makeText(this,"Failed to upload",Toast.LENGTH_SHORT).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == PICK_PHOTO_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                photoUri = data?.data
                imageView.setImageURI(photoUri)
            }
            else {
                Toast.makeText(this,"Image picker action canceled",Toast.LENGTH_SHORT).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}