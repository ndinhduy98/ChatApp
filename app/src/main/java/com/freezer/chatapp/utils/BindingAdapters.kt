package com.freezer.chatapp.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freezer.chatapp.R
import com.google.firebase.storage.FirebaseStorage

class BindingAdapters {
    companion object {
        @BindingAdapter("recyclerViewData")
        @JvmStatic
        fun <T> setRecyclerViewProperties(recyclerView: RecyclerView, data : T) {
            if(recyclerView.adapter is BindableAdapter<*>) {
                (recyclerView.adapter as BindableAdapter<T>).setData(data)
            }
        }

        @BindingAdapter("imageUrl")
        @JvmStatic
        fun setImageFromFirestoreUrl(imageView: ImageView, url: String) {
            if(url.isEmpty())
                 return

            val avatarRef = FirebaseStorage.getInstance("gs://chatapp-68a8d.appspot.com")
                .reference.child(url)

            GlideApp.with(imageView.context)
                .load(avatarRef)
                .placeholder(R.drawable.ic_avatar)
                .circleCrop()
                .into(imageView)
        }
    }
}