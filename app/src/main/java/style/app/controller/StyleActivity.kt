package style.app.controller

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_style_photo.*
import style.app.DIM_ALPHA
import style.app.R
import style.app.TEMP_PHOTO_FILENAME
import style.app.model.Photo
import style.app.model.saveBitmap
import style.app.network.ConnectionHandler
import style.app.network.ImagesFetcher
import style.app.network.PhotoSender
import java.io.File


class StyleActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PHOTO = "StyleActivity.EXTRA_PHOTO"
    }

    private lateinit var originalPhoto: Photo
    private lateinit var adapter: CustomAdapter
    private val serverHandler = ConnectionHandler()
    private val fetchTask: FetchStylesTask = FetchStylesTask()
    private val photoHandler = PhotoSender(this, serverHandler.httpClient)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_style_photo)

        val photo: Photo? = intent.getParcelableExtra(EXTRA_PHOTO)
        if (photo != null)
            originalPhoto = photo

        setupStyleBar()
    }

    private fun setupStyleBar() {
        val layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        styleBar.layoutManager = layoutManager
        fetchTask.execute()
    }

    private fun clickStyle(stylePhoto: Photo) {
        StylePhotoTask().execute(stylePhoto)
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
                serverHandler.httpClient,
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
                R.layout.style_item
            )
            styleBar.adapter = adapter
            fitPhoto(originalPhoto)
            loading_animation.visibility = View.INVISIBLE
        }
    }

   inner class StylePhotoTask: AsyncTask<Photo, Void, Photo>() {
        override fun onPreExecute() {
            super.onPreExecute()
            styled_photo.imageAlpha = DIM_ALPHA
            loading_animation.visibility = View.VISIBLE
        }


        override fun doInBackground(vararg params: Photo?): Photo {
            if (params.isEmpty() && params[0] == null)
                return Photo(File(""))

            val stylePhoto = params[0]!!
            val bitmap = photoHandler.sendPhoto(originalPhoto, stylePhoto)
            val file = File(getExternalFilesDir(null)!!, TEMP_PHOTO_FILENAME)
            saveBitmap(bitmap, file)
            return Photo(file)
        }

        override fun onPostExecute(result: Photo?) {
            super.onPostExecute(result)
            if (result == null)
                return

            fitPhoto(result, originalPhoto.rotation)
            loading_animation.visibility = View.INVISIBLE
            styled_photo.imageAlpha = 255
        }
    }


}
