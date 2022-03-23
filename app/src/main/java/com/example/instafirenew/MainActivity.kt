package com.example.instafirenew

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instafirenew.activities.CreateActivity
import com.example.instafirenew.activities.ProfileActivity
import com.example.instafirenew.daos.PostDao
import com.example.instafirenew.models.Post
import com.example.instafirenew.models.User
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*

open class MainActivity : AppCompatActivity(), IPostAdapter {

    companion object {
        const val EXTRA_USER_NAME = "EXTRA_USER_NAME"
        private const val TAG = "MainActivity"

    }
    private var signInUser: User = User()
    private lateinit var postDao: PostDao
    private lateinit var adapter:PostAdapter
    private lateinit var post:MutableList<Post>
    private lateinit var firestoreDb: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = "All Posts"

        firestoreDb = FirebaseFirestore.getInstance()


        firestoreDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signInUser = userSnapshot.toObject(User::class.java)!!
                Log.i(TAG,"signed in user $signInUser")
            }
            .addOnFailureListener { exception ->
                Log.i(TAG,"Failure fetching signed in user",exception)
            }

        fabCreate.setOnClickListener {
            val intent = Intent(this,CreateActivity::class.java)
            intent.putExtra(EXTRA_USER_NAME,signInUser?.displayName)
            startActivity(intent)
        }

        setUpRecyclerView()


    }

    private fun setUpRecyclerView() {
        post = mutableListOf()
        postDao = PostDao()
        val postsCollection = postDao.postsCollection
        var query = postsCollection.orderBy("createdAt", Query.Direction.DESCENDING)
        val username = intent.getStringExtra(EXTRA_USER_NAME)
        if(username != null) {
            supportActionBar?.title = username
            query = query.whereEqualTo("createdBy.displayName",username)
        }

        val rvOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query,Post::class.java).build()
        adapter = PostAdapter(rvOptions,this)
        rvPosts.adapter = adapter
        rvPosts.layoutManager = LinearLayoutManager(this)

    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    /**override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }**/


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.menu_profile) {
            val intent = Intent(this,ProfileActivity::class.java)
            intent.putExtra(EXTRA_USER_NAME,signInUser?.displayName)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onLikeClicked(postId: String) {
        postDao.updateLikes(postId)
    }
}