package style.app.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import style.app.model.Photo
import java.io.File

class ImageProvider(private val context: Context) {

    fun getAllImageFiles(): List<Photo> {
        val externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC"
        val columns = arrayOf(
            MediaStore.Images.ImageColumns._ID
        )
        val cursor = context.contentResolver.query(
            externalUri,
            columns,
            null,
            null,
            orderBy
        )

        val images = mutableListOf<Photo>()
        if (cursor != null) {
            cursor.moveToFirst()
//            val pathColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
            while (cursor.moveToNext()) {
                val imageUri = ContentUris
                    .withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)).toLong()
                )
                val photo = Photo(imageUri)
                images.add(photo)
            }
            cursor.close()
        }
        return images
    }
}
