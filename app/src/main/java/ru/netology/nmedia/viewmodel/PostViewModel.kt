package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
        repository.getAllAsync(object : PostRepository.Callback<List<Post>> {
            override fun onSuccess(posts: List<Post>) {
                _data.value = FeedModel(posts = posts, empty = posts.isEmpty())
            }

            override fun onError(e: Exception) {
                _data.value = FeedModel(error = true)
            }
        })
    }

    fun save() {
        edited.value?.let {
            repository.save(it, object : PostRepository.Callback<Post> {
                override fun onSuccess(post: Post) {
                    _postCreated.value = Unit
                    loadPosts()
                }

                override fun onError(e: Exception) {
                    _data.value = FeedModel(error = true)
                }
            })
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) {
        val post = _data.value?.posts?.find { it.id == id }
        if (post != null) {
            if (post.likedByMe) {
                // Если пост уже лайкнут, снимаем лайк
                repository.unlikeById(id, object : PostRepository.Callback<Post> {
                    override fun onSuccess(post: Post) {
                        loadPosts()
                    }

                    override fun onError(e: Exception) {
                        _data.value = FeedModel(error = true)
                    }
                })
            } else {
                repository.likeById(id, object : PostRepository.Callback<Post> {
                    override fun onSuccess(post: Post) {
                        loadPosts()
                    }

                    override fun onError(e: Exception) {
                        _data.value = FeedModel(error = true)
                    }
                })
            }
        }
    }

    fun removeById(id: Long) {
        repository.removeById(id, object : PostRepository.Callback<Unit> {
            override fun onSuccess(unit: Unit) {
                loadPosts()
            }

            override fun onError(e: Exception) {
                _data.value = FeedModel(error = true)
            }
        })
    }
}
