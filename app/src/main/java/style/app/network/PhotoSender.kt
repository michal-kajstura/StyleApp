package style.app.network

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.*
import style.app.model.Photo
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.UnknownHostException

class PhotoSender(private val contentResolver: ContentResolver,
                  private val postUrl: HttpUrl
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
//        val postUrl = ConnectionHandler.serverAddress + "transfer"
        val request = Request.Builder()
            .url(postUrl)
            .post(postBody)
            .build()

        try {
            val body = ConnectionHandler.httpClient
                .newCall(request)
                .execute()
                .body()
            return getBitmapFromBody(body)
        } catch (e: UnknownHostException) {
            throw WrongHostnameException(postUrl.host())
        }
    }

    private fun getBitmapFromBody(body: ResponseBody?): Bitmap {
            if (body != null) {
                val inputStream = body.byteStream()
                return BitmapFactory.decodeStream(inputStream)
            } else {
                throw IOException("Response body is null")
            }
    }
}

class WrongHostnameException(private val host: String): Exception() {
    override val message: String?
        get() = "Host: $host is unknown"
}
