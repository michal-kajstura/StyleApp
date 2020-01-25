package style.app.model

import android.content.ContentResolver
import java.io.IOException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream


fun saveBitmap(bmp: Bitmap, file: File) {
    try {
        if (!file.exists())
            file.createNewFile()
        FileOutputStream(file).use { out ->
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun saveImage(photo: Photo, name: String, storageDir: File, contentResolver: ContentResolver) {
    if (!storageDir.exists())
        storageDir.mkdirs()

    val image = File(storageDir, name + "_t.png")

    val fos = FileOutputStream(image)
    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, photo.uri)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
    fos.flush()
    fos.close()
}

