package style.app.controller

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import style.app.R
import style.app.model.Photo

class GalleryActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_IMAGE_CAPTURE = 0
    }
    private lateinit var galleryRecyclerView: RecyclerView
    private lateinit var photoGalleryAdapter: PhotoGalleryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions()
        openCameraFAB.setOnClickListener {takePictureIntent()}
        val imageUris = getImagesFromGallery()
        setupGallery(imageUris)
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

    private fun setupGallery(imageUris: List<Uri>) {
        val layoutManager = GridLayoutManager(this, 2)
        galleryRecyclerView = gallery
        galleryRecyclerView.setHasFixedSize(true)
        galleryRecyclerView.layoutManager = layoutManager
        val photos = imageUris.map {uri -> Photo(uri) }
        photoGalleryAdapter = PhotoGalleryAdapter(this, photos)
    }

    override fun onStart() {
        super.onStart()
        galleryRecyclerView.adapter = photoGalleryAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val intent = Intent(this, StylePhotoActivity::class.java).apply {
                putExtra(StylePhotoActivity.EXTRA_PHOTO, imageBitmap)
            }
            startActivity(intent)
        }
    }

    fun getImagesFromGallery(): List<Uri> {
        val externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val orderBy = MediaStore.Images.Media.DATE_MODIFIED;
        val columns = arrayOf(
            MediaStore.Images.Media._ID
        )
        val cursor = contentResolver.query(
            externalUri,
            columns,
            null,
            null,
            orderBy
        )

        val images = mutableListOf<Uri>()
        if (cursor != null) {
            cursor.moveToFirst()
            val dataColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val imageId = cursor.getLong(dataColumnIndex)
                val imageUri = Uri.withAppendedPath(externalUri, "" + imageId)
                images.add(imageUri)
            }
            cursor.close()
        }
        return images
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

    private fun somePermissionsNotGranted(): Boolean {
        return requestedPermissions.any {
            p -> ContextCompat.checkSelfPermission(activity, p) == PackageManager.PERMISSION_DENIED
        }
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
