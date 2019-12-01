package style.app.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.provider.MediaStore
import androidx.palette.graphics.Palette
import com.jcraft.jsch.JSch
import com.squareup.picasso.Picasso
import okhttp3.*
import style.app.*
import java.io.*
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

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

class PhotoHandler(private val context: Context) {
    private val TEMP_ZIP_FILENAME = "temp.zip"
    private val client = initOkHttpClient()
    private val serverUrl = "http://$LOCALHOST:$LOCAL_PORT/"

    private fun initOkHttpClient(): OkHttpClient {
        return OkHttpClient().newBuilder()
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    fun fetchStyles(): List<Photo> {
        val url = serverUrl + "styles"
        val request = Request.Builder()
            .url(url)
            .build()
        val responseBody = client.newCall(request)
            .execute()
            .body()
        val zipBytes = responseBody?.bytes()
        val tempFilePath = context.getExternalFilesDir(null)

        tempFilePath?.let {
            zipBytes?.let {
                val zipfile = saveTempZipFile(zipBytes, tempFilePath)
                return unzipPhotos(zipfile, tempFilePath)
            }
        }
        return emptyList()
        }

    private fun saveTempZipFile(zipBytes: ByteArray, tempFilePath: File): ZipFile {
        val tempFile = File(tempFilePath, TEMP_ZIP_FILENAME)
        val os = FileOutputStream(tempFile)
        os.write(zipBytes)
        return ZipFile(tempFile)
    }

    private fun unzipPhotos(zipfile: ZipFile, tempFilePath: File): List<Photo> {
        val stylePhotos = arrayListOf<Photo>()
        for (entry in zipfile.entries()) {
            val inputStream = zipfile.getInputStream(entry)
            val result = BitmapFactory.decodeStream(inputStream)
            val file = File(tempFilePath, entry.name)
            FileOutputStream(file).use {
                result.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            val photo = Photo(Uri.fromFile(file), file.absolutePath)
            stylePhotos.add(photo)

        }
        return stylePhotos
    }

    fun sendPhoto(photo: Photo): Bitmap {
        val imageBytes = getImageBytes(photo)

        val path = photo.uri?.path
        val filename = Paths.get(path)
                .fileName
                .toString()
        val postBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", filename,
                RequestBody.create(MediaType.parse("image/*jpg"), imageBytes))
            .build()

        return postPhoto(postBody)
    }

    private fun getImageBytes(photo: Photo): ByteArray {
        val uri = photo.uri
        if (uri == null)
            return byteArrayOf()

        val inputStream = context.contentResolver.openInputStream(photo.uri)
        val outputStream = ByteArrayOutputStream()
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        val bitmap = BitmapFactory.decodeStream(inputStream)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return outputStream.toByteArray()
    }

    private fun postPhoto(postBody: RequestBody): Bitmap {
        val postUrl = serverUrl + "transfer"
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


