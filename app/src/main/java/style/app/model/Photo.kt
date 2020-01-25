package style.app.model

import android.content.ContentResolver
import android.media.ExifInterface
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import java.io.IOException

data class Photo(var uri: Uri) : Parcelable {

    constructor(parcel: Parcel) : this(
        Uri.parse(parcel.readString())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uri.toString())
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

    val name: String = splitName()

    private fun splitName(): String {
        val splits = uri.path?.split("/")
        if (splits != null) {
            return splits.last()
        }
        throw IOException()
    }

    fun getRotation(contentResolver: ContentResolver): Float{
        var rotation = 0.0F
        val inputStream = contentResolver.openInputStream(uri)
        val exif = ExifInterface(inputStream)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_270 -> rotation = 270.0F
            ExifInterface.ORIENTATION_ROTATE_180 -> rotation = 180.0F
            ExifInterface.ORIENTATION_ROTATE_90 -> rotation = 90.0F
        }

        return rotation
    }
}