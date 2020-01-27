package style.app.controller

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.SoundEffectConstants
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_style_photo.*
import style.app.DIM_ALPHA
import style.app.R
import style.app.TEMP_PHOTO_FILENAME
import style.app.model.Cache
import style.app.model.Photo
import style.app.model.saveBitmap
import style.app.model.saveImage
import style.app.network.ConnectionHandler
import style.app.network.ImagesFetcher
import style.app.network.PhotoSender
import java.io.File


class StyleActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PHOTO = "StyleActivity.EXTRA_PHOTO"
    }

    private lateinit var originalPhoto: Photo
    private lateinit var styledPhoto: Photo
    private lateinit var adapter: CustomAdapter
    private val cache = Cache()
    private val fetchTask: FetchStylesTask = FetchStylesTask()
    private lateinit var photoHandler: PhotoSender
    private lateinit var audomanager: AudioManager
    private var sendPhoto = true
    private var name = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audomanager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        photoHandler = PhotoSender(contentResolver, ConnectionHandler.getUrl("transfer"))
        setContentView(R.layout.activity_style_photo)
        assignPhoto()
        setupStyleBar()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    private fun assignPhoto() {
        val photo: Photo? = intent.getParcelableExtra(EXTRA_PHOTO)
        if (photo != null) {
            originalPhoto = photo
            name = originalPhoto.name
            cache.add(
                BitmapFactory.decodeStream(contentResolver.openInputStream(photo.uri))
            )
            fitPhoto(originalPhoto, originalPhoto.getRotation(contentResolver))
        } else
            throw NoPhotoException()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_save -> {
                savePhoto()
                return true
            }
            R.id.action_back -> {
                val bitmap = cache.back()
                bitmap?.let {
                    val file = savePhotoToFile(it)
                    fitPhoto(Photo(Uri.fromFile(file)))
                }
                audomanager.playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT)
                return true
            }
            R.id.action_forward -> {
                val bitmap = cache.forward()
                bitmap?.let {
                    val file = savePhotoToFile(bitmap)
                    fitPhoto(Photo(Uri.fromFile(file)))
                }
                audomanager.playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT)
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun savePhoto() {
        val storageDir = File(
            externalMediaDirs.first(),
            "styled_images"
        )

        if (::styledPhoto.isInitialized) {
            saveImage(styledPhoto, name, storageDir, contentResolver)
        }
            Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show()
    }

    private fun setupStyleBar() {
        val layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        styleBar.layoutManager = layoutManager
        fetchTask.execute()
    }

    private fun clickStyle(stylePhoto: Photo) {
        SendPhotoTask().execute(stylePhoto)
        name = originalPhoto.name + stylePhoto.name
    }


    private fun fitPhoto(photo: Photo, rotation: Float=0.0F) {
        Picasso.get()
            .load(photo.uri)
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .error(R.drawable.error)
            .fit()
            .rotate(rotation)
            .centerInside()
            .into(styled_photo)
    }

    inner class FetchStylesTask: AsyncTask<Unit, Void, List<Photo>>() {

        private lateinit var imageFetcher: ImagesFetcher

        override fun onPreExecute() {
            super.onPreExecute()
            loading_animation.visibility = View.VISIBLE
            imageFetcher = ImagesFetcher(
                getExternalFilesDir(null)
            )
        }


        override fun doInBackground(vararg params: Unit?): List<Photo> {
            return imageFetcher.fetchStyles()
        }

        override fun onPostExecute(result: List<Photo>?) {
            super.onPostExecute(result)
            if (result == null)
                return

            adapter = CustomAdapter(
                result, {p -> clickStyle(p)}, 350, 350,
                R.layout.style_item, contentResolver
            )
            styleBar.adapter = adapter
            loading_animation.visibility = View.INVISIBLE
        }
    }

    private fun savePhotoToFile(bitmap: Bitmap): File {
        val file = File(getExternalFilesDir(null)!!, TEMP_PHOTO_FILENAME)
        saveBitmap(bitmap, file)
        return file
    }

   inner class SendPhotoTask: AsyncTask<Photo, Void, Photo>() {

        override fun onPreExecute() {
            super.onPreExecute()
            styled_photo.imageAlpha = DIM_ALPHA
            loading_animation.visibility = View.VISIBLE
        }


        override fun doInBackground(vararg params: Photo?): Photo {
            val stylePhoto = params[0]!!
            val bitmap = photoHandler.send(originalPhoto, stylePhoto, sendPhoto)
            cache.add(bitmap)
            val file = savePhotoToFile(bitmap)
            return Photo(Uri.fromFile(file))
        }

        override fun onPostExecute(result: Photo?) {
            super.onPostExecute(result)
            if (result == null)
                return

            styledPhoto = result
            fitPhoto(result, originalPhoto.getRotation(contentResolver))
            loading_animation.visibility = View.INVISIBLE
            styled_photo.imageAlpha = 255
            sendPhoto = false
        }
    }
}

class NoPhotoException: Exception()
