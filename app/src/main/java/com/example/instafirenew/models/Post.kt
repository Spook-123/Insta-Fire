package com.example.instafirenew.models

data class Post(
    val description:String = "",
    val createdBy:User = User(),
    val createdAt:Long = 0L,
    val imageUrlPost:String = "",
    val likedBy:ArrayList<String> = ArrayList()
)
