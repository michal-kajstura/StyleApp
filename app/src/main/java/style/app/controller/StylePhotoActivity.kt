package style.app.controller

import android.content.Intent
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_style_photo.*
import style.app.R
import style.app.TEMP_PHOTO_FILENAME
import style.app.model.Photo
import style.app.model.saveBitmap
import style.app.network.ConnectionHandler
import style.app.network.ImagesFetcher
import style.app.network.PhotoSender
import java.io.IOException
import java.net.ConnectException


class StylePhotoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PHOTO = "StylePhotoActivity.EXTRA_PHOTO"
    }

    private lateinit var originalPhoto: Photo
    private lateinit var adapter: CustomAdapter
    private lateinit var imageFetcher: ImagesFetcher
    private val serverHandler = ConnectionHandler()
    private val photoHandler = PhotoSender(this, serverHandler.httpClient)
    private var rotation = 0.0F


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_style_photo)
        imageFetcher = ImagesFetcher(
            serverHandler.httpClient,
            getExternalFilesDir(null)
        )
        originalPhoto = intent.getParcelableExtra(EXTRA_PHOTO)
        rotation = getCameraPhotoOrientation(originalPhoto.path!!)

        // TODO get rid of this
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        setupStyleBar()
    }

    private fun setupStyleBar() {
        val layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        styleBar.layoutManager = layoutManager
        try {
            val styleImages = imageFetcher.fetchStyles()
            adapter = CustomAdapter(
            styleImages, this::clickStyle, 300, 300,
            R.layout.style_item
        )
        } catch (e: ConnectException) {
            Toast.makeText(this, "Connection error", Toast.LENGTH_LONG).show()
            val intent = Intent(this, GalleryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun clickStyle(stylePhoto: Photo) {
        runOnUiThread {
            val bitmap = photoHandler.sendPhoto(originalPhoto, stylePhoto)
            styled_photo.setImageBitmap(bitmap)
            val file = saveBitmap(bitmap, TEMP_PHOTO_FILENAME, getExternalFilesDir(null)!!)
            val photo = Photo(Uri.fromFile(file), file.absolutePath)
            fitPhoto(photo)
        }
    }


    override fun onStart() {
        super.onStart()
        styleBar.adapter = adapter
        fitPhoto(originalPhoto)
    }

    private fun fitPhoto(photo: Photo) {
        Picasso.get()
            .load(photo.uri)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error)
            .rotate(rotation)
            .fit()
            .centerInside()
            .into(styled_photo)
    }

    private fun getCameraPhotoOrientation(imageFilePath: String): Float{
        var rotate = 0.0F
        try {
            val exif = ExifInterface(imageFilePath)
            val exifOrientation = exif
                .getAttribute(ExifInterface.TAG_ORIENTATION)
            Log.d("exifOrientation", exifOrientation)
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
