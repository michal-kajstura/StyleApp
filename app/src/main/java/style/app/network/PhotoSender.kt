package style.app.network

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.*
import style.app.SERVER_URL
import style.app.model.Photo
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.file.Paths

class PhotoSender(private val contentResolver: ContentResolver,
                  private val client: OkHttpClient
) {

    fun send(photo: Photo, stylePhoto: Photo, sendPhoto: Boolean): Bitmap {
        val imageBytes = getImageBytes(photo)
        val title = stylePhoto.name

        val filename = File(photo.uri.path).name

        var postBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("style", title)

        if (sendPhoto)
            postBuilder = postBuilder.addFormDataPart("image", filename,
                                RequestBody.create(MediaType.parse("image/*jpg"), imageBytes))


        return postPhoto(postBuilder.build())
    }

    private fun getImageBytes(photo: Photo): ByteArray {
        val inputStream = contentResolver.openInputStream(photo.uri)
        val outputStream = ByteArrayOutputStream()
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        val bitmap = BitmapFactory.decodeStream(inputStream)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
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
            return BitmapFactory.decodeStream(inputStream)
        } else {
            throw IOException()
        }
    }
}
