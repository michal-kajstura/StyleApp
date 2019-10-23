package style.app.gallery

import android.os.Parcel
import android.os.Parcelable

data class Photo(val path: String?) : Parcelable {

    constructor(parcel: Parcel) : this(parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(path)
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

        fun getSunsetPhotos(): Array<Photo> {
            return arrayOf<Photo>(
                Photo("https://goo.gl/32YN2B"),
                Photo("https://goo.gl/Wqz4Ev"),
                Photo("https://goo.gl/U7XXdF"),
                Photo("https://goo.gl/ghVPFq"),
                Photo("https://goo.gl/qEaCWe"),
                Photo("https://goo.gl/vutGmM"))
        }
    }
}