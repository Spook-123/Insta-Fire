package com.example.instafirenew

import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.instafirenew.models.Post
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_post.view.*
import org.w3c.dom.Text

private const val TAG = "PostAdapter"
class PostAdapter(options: FirestoreRecyclerOptions<Post>,val listener:IPostAdapter): FirestoreRecyclerAdapter<Post, PostAdapter.PostViewHolder>(
    options
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post,parent,false)
        val viewHolder = PostViewHolder(view)
        viewHolder.ivLikeButton.setOnClickListener {
            listener.onLikeClicked(snapshots.getSnapshot(viewHolder.adapterPosition).id)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int, model: Post) {
        holder.tvUserName.text = model.createdBy!!.displayName
        holder.tvPostDescription.text = model.description
        holder.tvCreatedAt.text = Utils.getTimeAgo(model.createdAt)
        Glide.with(holder.ivProfileImage.context).load(model.createdBy.imageUrl).circleCrop().into(holder.ivProfileImage)
        Glide.with(holder.ivUploadedImage.context).load(model.imageUrlPost).apply(RequestOptions().transform(
            CenterCrop(),RoundedCorners(20)
        )).into(holder.ivUploadedImage)
        holder.tvLikeCount.text = model.likedBy.size.toString()
        //Picasso.get().load(model.imageUrlPost).fit().into(holder.ivUploadedImage)


        val auth = Firebase.auth
        val currentUserId = auth.currentUser!!.uid
        val isLiked = model.likedBy.contains(currentUserId)
        if(isLiked) {
            holder.ivLikeButton.setImageDrawable(ContextCompat.getDrawable(holder.ivLikeButton.context,R.drawable.ic_liked))
        }
        else {
            holder.ivLikeButton.setImageDrawable(ContextCompat.getDrawable(holder.ivLikeButton.context,R.drawable.ic_unliked))
        }
    }
    inner class PostViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val tvPostDescription = itemView.findViewById<TextView>(R.id.tvDescription)
        val tvUserName = itemView.findViewById<TextView>(R.id.tvUserName)
        val tvCreatedAt = itemView.findViewById<TextView>(R.id.tvCreatedAt)
        val ivProfileImage = itemView.findViewById<ImageView>(R.id.ivProfileImage)
        val ivUploadedImage = itemView.findViewById<ImageView>(R.id.ivImageUploaded)
        val ivLikeButton = itemView.findViewById<ImageView>(R.id.ivLikeButton)
        val tvLikeCount = itemView.findViewById<TextView>(R.id.tvLikeCount)
    }
}

interface IPostAdapter {

    fun onLikeClicked(postId:String)
}