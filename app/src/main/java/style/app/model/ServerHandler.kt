package style.app.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import com.jcraft.jsch.JSch
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

class ConnectionHandler {

    class OpenSshTask: AsyncTask<Unit, Void, Unit>() {
        override fun doInBackground(vararg params: Unit?){
            val jsch = JSch()
            val session = jsch.getSession(USERNAME, HOSTNAME, PORT)
            session.setConfig("StrictHostKeyChecking", "no")
            session.setPassword(PASSWORD)
            session.timeout = 100000
            session.connect()
            session.setPortForwardingL(LOCAL_PORT, "localhost", REMOTE_PORT)
        }
    }

    fun establishConnection() {
        OpenSshTask().execute()
    }
}

class PhotoHandler {
    private val client = initOkHttpClient()

    private fun initOkHttpClient(): OkHttpClient {
        return OkHttpClient().newBuilder()
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .build()
    }


    fun sendPhoto(photo: Photo): Bitmap {
        val postUrl = "http://${LOCALHOST}:${LOCAL_PORT}/"

        val imageBytes = getImageBytes(photo)

        val filename = Paths.get(photo.path)
            .fileName
            .toString()
        val postBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", filename,
                RequestBody.create(MediaType.parse("image/*jpg"), imageBytes))
            .build()

        return postRequest(postUrl, postBody)
    }

    private fun getImageBytes(photo: Photo): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        val bitmap = BitmapFactory.decodeFile(photo.path, options)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return outputStream.toByteArray()
    }

    private fun postRequest(postUrl: String, postBody: RequestBody): Bitmap {
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


