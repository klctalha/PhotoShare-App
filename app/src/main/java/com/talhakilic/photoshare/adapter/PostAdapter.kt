package com.talhakilic.photoshare.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.squareup.picasso.Picasso
import com.talhakilic.photoshare.databinding.RecyclerRowBinding
import com.talhakilic.photoshare.model.Post
import java.lang.System.load
import com.talhakilic.photoshare.R


class PostAdapter(private val postListesi : ArrayList<Post>) : RecyclerView.Adapter<PostAdapter.PostHolder>(){
    class PostHolder(val binding : RecyclerRowBinding): RecyclerView.ViewHolder(binding.root){}


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostHolder(binding)
    }

    override fun onBindViewHolder(
        holder: PostHolder,
        position: Int
    ) {
        holder.binding.recyclerEmailText.text = postListesi[position].emailA
        holder.binding.recyclerCommentText.text = postListesi[position].commentA

        val imageUrl = postListesi[position].downloadUrlA
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).into(holder.binding.recyclerImageView)
        } else {
            Picasso.get().load(R.drawable.img).into(holder.binding.recyclerImageView)
        }
    }

    override fun getItemCount(): Int {
        return postListesi.size
    }
}