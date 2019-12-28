package style.app.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.*
import style.app.SERVER_URL
import style.app.model.Photo
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.file.Paths

class PhotoSender(private val context: Context,
                  private val client: OkHttpClient
) {

    fun sendPhoto(photo: Photo, stylePhoto: Photo): Bitmap {
        val imageBytes = getImageBytes(photo)
        val title = stylePhoto.path
            .split("/")
            .last()

        val filename = Paths.get(photo.path)
                .fileName
                .toString()

        val postBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", filename,
                RequestBody.create(MediaType.parse("image/*jpg"), imageBytes))
            .addFormDataPart("style", title) .build()

        return postPhoto(postBody)
    }

    private fun getImageBytes(photo: Photo): ByteArray {
        val inputStream = context.contentResolver.openInputStream(photo.uri)
        val outputStream = ByteArrayOutputStream()
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        val bitmap = BitmapFactory.decodeStream(inputStream)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return outputStream.toByteArray()
    }

    private fun postPhoto(postBody: RequestBody): Bitmap {
        val postUrl = SERVER_URL + "transfer"
        val request = Request.Builder()
            .url(postUrl)
            .post(postBody)
            .build()

        val body = client.newCall(request)
            .execute()
            .body()

        if (body != null) {
            val inputStream = body.byteStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            return bitmap
        } else {
            throw IOException()
        }
    }
}
