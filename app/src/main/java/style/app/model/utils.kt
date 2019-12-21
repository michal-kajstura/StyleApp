package style.app.model

import java.io.IOException
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream


fun saveBitmap(bmp: Bitmap, filename: String, external: File): File {
    val file = File(external, filename)
    try {
        if (!file.exists())
            file.createNewFile()
        FileOutputStream(file).use { out ->
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return file
}

