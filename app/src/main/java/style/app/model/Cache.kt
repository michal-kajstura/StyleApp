package style.app.model

import android.graphics.Bitmap


class Cache {
    private var list: List<Bitmap> = ArrayList()
    private var currentPosition: Int=-1

    fun add(bmp: Bitmap) {
        list = list.dropLast(list.size - currentPosition - 1) + bmp
        currentPosition += 1
    }

    fun back(): Bitmap? {
        if (currentPosition != 0) {
            currentPosition -= 1
            return list[currentPosition]
        }
        return null
    }

    fun forward(): Bitmap? {
        if (currentPosition < list.size - 1) {
            currentPosition += 1
            return list[currentPosition]
        }
        return null
    }
}