package style.app.controller

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.StrictMode
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_style_photo.*
import style.app.R
import style.app.model.Photo
import style.app.model.PhotoHandler


class StylePhotoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PHOTO = "StylePhotoActivity.EXTRA_PHOTO"
    }

    private lateinit var photo: Photo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_style_photo)


//        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)

        photo = intent.getParcelableExtra(EXTRA_PHOTO)
//        val handler = PhotoHandler()
//        runOnUiThread {
//            val bitmap = handler.sendPhoto(photo)
//            styled_photo.setImageBitmap(bitmap)
//        }

    }

    override fun onStart() {
        super.onStart()
        Picasso.get()
            .load(photo.uri)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error)
            .resize(0, 1000)
            .into(styled_photo)
//            .into(styled_photo, object : Callback {
//                override fun onSuccess() {
//                    val bitmap = (styled_photo.drawable as BitmapDrawable).bitmap
//                    onPalette(Palette.from(bitmap).generate())
//                }
//
//                override fun onError(e: Exception?) {
//                }
//            })

    }

    fun onPalette(palette: Palette) {
        val parent = styled_photo.parent.parent as ViewGroup
        parent.setBackgroundColor(palette.getDominantColor(Color.GRAY))
    }
}
