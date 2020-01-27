package style.app.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import okhttp3.MultipartBody
import okhttp3.Request
import style.app.TEMP_ZIP_FILENAME
import style.app.model.Photo
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.stream.Collectors
import java.util.zip.ZipException
import java.util.zip.ZipFile

class ImagesFetcher(private val tempFilePath: File?) {

     fun fetchStyles(): List<Photo> {
        val styleImagesPaths = listStylesDir()
        val stylePathsOneString = styleImagesPaths.joinToString (separator = "\n") {
                s -> s.name.split("/").last()
        }

        val requestUrl = ConnectionHandler.getUrl("styles")
        val postBody = MultipartBody.Builder()
           .setType(MultipartBody.FORM)
           .addFormDataPart("image_list", stylePathsOneString).build()

        val request = Request.Builder()
            .url(requestUrl)
            .post(postBody)
            .build()

        val responseBody = ConnectionHandler.httpClient
            .newCall(request)
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
        val imageFiles = listStylesDir()
        return imageFiles.map { f -> Photo(Uri.fromFile(f)) }
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

    private fun listStylesDir(): List<File> {
        val directory = File(tempFilePath, "styles")
        if (! directory.exists()) {
            directory.mkdir()
        }
        val files = Files.walk(directory.toPath())
        return files
            .filter{f -> Files.isRegularFile(f)}
            .map{f -> f.toFile()}
            .collect(Collectors.toList())
    }
}