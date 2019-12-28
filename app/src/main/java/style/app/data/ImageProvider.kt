package style.app.data

import android.content.Context
import android.provider.MediaStore
import style.app.model.Photo
import java.io.File

class ImageProvider(private val context: Context) {

    fun getAllImageFiles(): List<Photo> {
        val externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC"
        val columns = arrayOf(
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
            val pathColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (cursor.moveToNext()) {
                val path = cursor.getString(pathColumnIndex)
                val image = Photo(File(path))
                images.add(image)
            }
            cursor.close()
        }
        return images
    }
}
