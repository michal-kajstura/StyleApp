package style.app.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import style.app.SERVER_URL
import style.app.TEMP_ZIP_FILENAME
import style.app.model.Photo
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.stream.Collectors
import java.util.zip.ZipException
import java.util.zip.ZipFile

class ImagesFetcher(private val client: OkHttpClient,
                    private val tempFilePath: File?) {

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
        val files = imagePaths.map {pth -> File(pth) }
        return files.map {
            f -> Photo(f)
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