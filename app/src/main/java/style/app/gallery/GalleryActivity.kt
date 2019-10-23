package style.app.gallery

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import style.app.R

class GalleryActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_IMAGE_CAPTURE = 0
    }
    private lateinit var galleryRecyclerView: RecyclerView
    private lateinit var photoGalleryAdapter: PhotoGalleryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        openCameraFAB.setOnClickListener {takePictureIntent()}
        setupGallery()
    }

    private fun takePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
            takePictureIntent -> takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun setupGallery() {
        val layoutManager = GridLayoutManager(this, 2)
        galleryRecyclerView = gallery
        galleryRecyclerView.setHasFixedSize(true)
        galleryRecyclerView.layoutManager = layoutManager
        photoGalleryAdapter = PhotoGalleryAdapter(this, Photo.getSunsetPhotos())
    }

    override fun onStart() {
        super.onStart()
        galleryRecyclerView.adapter = photoGalleryAdapter
    }
}
