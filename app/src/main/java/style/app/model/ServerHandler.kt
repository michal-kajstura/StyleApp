package style.app.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import com.jcraft.jsch.JSch
import okhttp3.*
import style.app.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import java.util.zip.ZipException
import java.util.zip.ZipFile

class ConnectionHandler {
    val httpClient = initOkHttpClient()

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

     private fun initOkHttpClient(): OkHttpClient {
        return OkHttpClient().newBuilder()
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .build()
    }
}

class PhotoHandler(private val context: Context,
                   private val client: OkHttpClient) {

    fun sendPhoto(photo: Photo, stylePhoto: Photo): Bitmap {
        val imageBytes = getImageBytes(photo)
        val title = stylePhoto.path?.split("/")?.last()
        val path = photo.uri?.path
        val filename = Paths.get(path)
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

class ImagesFetcher(private val client: OkHttpClient,
                    private val tempFilePath: File?) {
    private val TEMP_ZIP_FILENAME = "temp.zip"

     fun fetchStyles(): List<Photo> {
        val styleImagesPaths = listStylesDir()
        val stylePathsOneString = styleImagesPaths.joinToString (separator = "\n") {
                s -> s.split("/").last()
        }


        val requestUrl = SERVER_URL + "styles"
        val postBody = MultipartBody.Builder()
           .setType(MultipartBody.FORM)
           .addFormDataPart("image_list", stylePathsOneString) .build()

        val request = Request.Builder()
            .url(requestUrl)
            .post(postBody)
            .build()

        val responseBody = client.newCall(request)
            .execute()
            .body()
        val zipBytes = responseBody?.bytes()

        tempFilePath?.let {
            zipBytes?.let {
                saveTempZipFile(zipBytes, tempFilePath)
                unzipPhotos(tempFilePath)
                return getAllStylePhotos()
            }
        }
        return emptyList() }

    private fun getAllStylePhotos(): List<Photo> {
        val imagePaths = listStylesDir()
        val files = imagePaths.map {pth -> File(pth)}
        return files.map {
            f -> Photo(Uri.fromFile(f), f.absolutePath)
        }
    }

    private fun saveTempZipFile(zipBytes: ByteArray, tempFilePath: File) {
        val tempFile = getTempFileZipPath(tempFilePath)
        val os = FileOutputStream(tempFile)
        os.write(zipBytes)
    }

    private fun getTempFileZipPath(tempFilePath: File): File {
        return File(tempFilePath, TEMP_ZIP_FILENAME)
    }

    private fun unzipPhotos(tempFilePath: File) {
        val zipPath= getTempFileZipPath(tempFilePath)
        try {
            val zipFile = ZipFile(zipPath)
            unzip(zipFile)
        } catch (ex: ZipException) {
            // Catch if zipFile is empty (no new images were sent)
            return
        }

    }

    private fun unzip(zipFile: ZipFile) {
        for (entry in zipFile.entries()) {
            val inputStream = zipFile.getInputStream(entry)
            val result = BitmapFactory.decodeStream(inputStream)
            val filename = entry.name.split("/")[1]
            val file = File(tempFilePath, "styles/${filename}")
            FileOutputStream(file).use {
                result.compress(Bitmap.CompressFormat.PNG, 100, it)
           }
        }
    }

    private fun listStylesDir(): List<String> {
        val directory = File(tempFilePath, "styles")
        if (! directory.exists()) {
            directory.mkdir()
        }
        val files = Files.walk(directory.toPath())
        val result = files
            .filter{f -> Files.isRegularFile(f)}
            .map { x -> x.toString() }
            .collect(Collectors.toList())
        return result

    }

}
