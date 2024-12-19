package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    interface Callback<T> {
        fun onSuccess(data: T)
        fun onError(e: Exception)
    }

    fun getAllAsync(callback: Callback<List<Post>>)
    fun save(post: Post, callback: Callback<Post>)
    fun removeById(id: Long, callback: Callback<Unit>)
    fun likeById(id: Long, callback: Callback<Post>)
    fun unlikeById(id: Long, callback: Callback<Post>)
}
