package com.example.instafirenew.daos


import com.example.instafirenew.models.Post
import com.example.instafirenew.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class PostDao {



    val db = FirebaseFirestore.getInstance()
    val postsCollection = db.collection("posts")
    val auth = Firebase.auth

    fun addPost(description:String,imageUrlUpload:String) {
        val currentUserId = auth.currentUser!!.uid
        GlobalScope.launch {
            val userDao = UserDao()
            val user = userDao.getUserById(currentUserId).await().toObject(User::class.java)!!
            val currentTime = System.currentTimeMillis()
            val post = Post(description,user,currentTime,imageUrlUpload)
            postsCollection.document().set(post)
        }

    }

    fun getPostById(postId:String): Task<DocumentSnapshot> {
        return postsCollection.document(postId).get()

    }

    fun updateLikes(postId:String) {
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val post = getPostById(postId).await().toObject(Post::class.java)
            val isLiked = post!!.likedBy.contains(currentUserId)

            if(isLiked) {
                post.likedBy.remove(currentUserId)
            }
            else {
                post.likedBy.add(currentUserId)
            }
            postsCollection.document(postId).set(post)


        }
    }
}