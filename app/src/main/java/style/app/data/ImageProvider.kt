package style.app.data

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import style.app.model.Photo

class ImageProvider(private val context: Context) {

    fun getAllImageFiles(): List<Photo> {
        val externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC"
        val columns = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA
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
            val dataColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val pathColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (cursor.moveToNext()) {
                val imageId = cursor.getLong(dataColumnIndex)
                val path = cursor.getString(pathColumnIndex)
                val imageUri = Uri.withAppendedPath(externalUri, "" + imageId)
                val image = Photo(imageUri, path)
                images.add(image)
            }
            cursor.close()
        }
        return images
    }
}
