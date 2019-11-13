package style.app.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class Photo(val path: Uri?) : Parcelable {

    constructor(parcel: Parcel) : this(Uri.parse(parcel.readString()))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(path.toString())
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
}