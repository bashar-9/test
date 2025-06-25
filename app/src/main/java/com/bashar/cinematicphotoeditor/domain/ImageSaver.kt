package com.bashar.cinematicphotoeditor.domain

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import java.io.OutputStream

object ImageSaver {

    // This function returns true if saving was successful, false otherwise.
    fun saveBitmap(context: Context, bitmap: Bitmap, displayName: String): Boolean {
        // Create a unique name for the image using the current time
        val name = "$displayName-${System.currentTimeMillis()}.jpeg"

        // Prepare the metadata for the new image file
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            // This is important for newer Android versions to place it in the right folder
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CinematicEditor")
                put(MediaStore.MediaColumns.IS_PENDING, 1) // Mark as pending while we write the data
            }
        }

        // Get the ContentResolver, which is how we interact with the MediaStore
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        // Open an output stream to the new file's URI and save the bitmap
        uri?.let {
            try {
                val outputStream: OutputStream? = resolver.openOutputStream(it)
                outputStream?.use { stream ->
                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                        // Compression failed
                        return false
                    }
                }

                // If on a newer Android version, mark the file as no longer pending
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
                }

                return true // Success!
            } catch (e: Exception) {
                e.printStackTrace()
                // If something went wrong, try to delete the pending file entry
                resolver.delete(it, null, null)
                return false // Failure
            }
        }
        return false // Failure if URI was null
    }
}
