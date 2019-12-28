package style.app.model

import android.media.ExifInterface
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import java.io.File
import java.io.IOException

data class Photo(var file: File) : Parcelable {

    constructor(parcel: Parcel) : this(
        File(parcel.readString()!!)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(file.absolutePath)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Photo> {
        override fun createFromParcel(parcel: Parcel): Photo {
            return Photo(parcel)
        }

        override fun newArray(size: Int): Array<Photo?> {
            return arrayOfNulls(size)
        }

    }

    val uri: Uri = Uri.fromFile(file)

    val rotation: Float = getCameraPhotoOrientation()

    val path: String = file.absolutePath

    private fun getCameraPhotoOrientation(): Float{
        var rotate = 0.0F
        try {
            val exif = ExifInterface(file.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270.0F
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180.0F
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90.0F
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return rotate
    }
}