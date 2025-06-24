package com.bashar.cinematicphotoeditor.presentation

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {

    private val _images = MutableStateFlow<List<Uri>>(emptyList())
    val images = _images.asStateFlow()

    fun loadImages(context: Context) {
        viewModelScope.launch {
            val imageList = withContext(Dispatchers.IO) {
                val images = mutableListOf<Uri>()
                val projection = arrayOf(MediaStore.Images.Media._ID)
                val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

                val cursor = context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    sortOrder
                )

                cursor?.use {
                    val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    while (it.moveToNext()) {
                        val id = it.getLong(idColumn)
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        images.add(contentUri)
                    }
                }
                images
            }
            _images.value = imageList
        }
    }
}
