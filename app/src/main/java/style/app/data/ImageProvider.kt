package style.app.data

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import style.app.model.Photo
import java.io.File

class ImageProvider(val context: Context) {

    fun getStyleImageFiles(): List<Photo> {
        val styleFilenames = context.assets.list("styles")
        if (styleFilenames != null) {
            val stylePaths = styleFilenames.map { fname ->
                "//android_asset/styles/${fname}"
            }
            val styleUris = stylePaths.map { path ->
                Uri.fromFile(File(path))
            }
            val photos = (styleUris zip stylePaths).map { (uri, path) -> Photo(uri, path) }
            return photos
        }
        return emptyList()
    }

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
