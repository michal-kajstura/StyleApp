package style.app.controller

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import style.app.IMAGES_IN_ROW
import style.app.R
import style.app.data.ImageProvider
import style.app.model.Photo
import java.io.File
import java.io.FileOutputStream

class GalleryActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_IMAGE_CAPTURE = 0
    }
    private lateinit var gallery: RecyclerView
    private lateinit var adapter: CustomAdapter
    private val imageProvider = ImageProvider(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions()
        openCameraFAB.setOnClickListener {takePictureIntent()}
        val imageUris = imageProvider.getAllImageFiles()
        setupGallery(imageUris)

        galllery_settings_fab.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_settings -> {

            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun takePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
            takePictureIntent -> takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent,
                    REQUEST_IMAGE_CAPTURE
                )
            }
        }
    }

    private fun setupGallery(photos: List<Photo>) {
        val layoutManager = GridLayoutManager(this, IMAGES_IN_ROW)
        gallery = galleryRecycler
        gallery.setHasFixedSize(true)
        gallery.layoutManager = layoutManager
        adapter = CustomAdapter( photos, this::clickPhoto, 500, 500,
            R.layout.gallery_item)
    }

    private fun clickPhoto(photo: Photo) {
    val intent = Intent(this, StyleActivity::class.java).apply {
            putExtra(StyleActivity.EXTRA_PHOTO, photo)
        }
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        gallery.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val file = File(getExternalFilesDir(null), "temp.png")
            FileOutputStream(file).use {
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            val photo = Photo(file)

            val intent = Intent(this, StyleActivity::class.java).apply {
                putExtra(StyleActivity.EXTRA_PHOTO, photo)
            }
            startActivity(intent)
        }
    }


    private fun requestPermissions() {
        val permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val permissionManager = PermissionManager(this, permissions, 123)
        permissionManager.checkPermissions()
    }
}

class PermissionManager(private val activity: Activity,
                        private val requestedPermissions: List<String>,
                        private val code: Int) {

    fun checkPermissions() {
        val notGranted = findDeniedPermissions()
        if (notGranted.isNotEmpty())
            requestPermissons(notGranted)

    }

    private fun findDeniedPermissions(): List<String> {
        return requestedPermissions.filter {
                p -> ContextCompat
                        .checkSelfPermission(activity, p) == PackageManager.PERMISSION_DENIED
        }
    }

    private fun requestPermissons(permissions: List<String>) {
        ActivityCompat.requestPermissions(activity, permissions.toTypedArray(), code)
    }
}
