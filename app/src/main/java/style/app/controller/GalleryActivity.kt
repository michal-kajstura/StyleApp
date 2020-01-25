package style.app.controller

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import style.app.IMAGES_IN_ROW
import style.app.R
import style.app.data.ImageProvider
import style.app.model.Photo
import style.app.network.ConnectionHandler
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class GalleryActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_IMAGE_CAPTURE = 0
    }
    private lateinit var gallery: RecyclerView
    private lateinit var adapter: CustomAdapter
    private val imageProvider = ImageProvider(this)
    private lateinit var takenPhotoUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions()
        openCameraFAB.setOnClickListener {takePictureIntent()}
        val imageUris = imageProvider.getAllImageFiles()
        setupGallery(imageUris)
    }

    private fun takePictureIntent() {

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val takenPhotoFile = createImageFile()
                takenPhotoUri = FileProvider.getUriForFile(this,
                    "style.app.android.fileprovider", takenPhotoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, takenPhotoUri)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(storageDir, "JPEG_${timeStamp}_.jpg")
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val photo = Photo(takenPhotoUri)
            val intent = Intent(this, StyleActivity::class.java).apply {
                putExtra(StyleActivity.EXTRA_PHOTO, photo)
            }

            startActivity(intent)
        }
    }

    private fun setupGallery(photos: List<Photo>) {
        val layoutManager = GridLayoutManager(this, IMAGES_IN_ROW)
        gallery = galleryRecycler
        gallery.setHasFixedSize(true)
        gallery.layoutManager = layoutManager
        adapter = CustomAdapter( photos, this::clickPhoto, 500, 500,
            R.layout.gallery_item, contentResolver)
    }

    private fun clickPhoto(photo: Photo) {
        if (!ConnectionHandler.connected) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val port = sharedPreferences.getString("port_key", "")
            if (!port.isNullOrEmpty()) {
                val connected = ConnectionHandler.establishConnection(port.toInt())
                if (connected)
                    return
            }
            askForPortNumber()
            return
        }

        val intent = Intent(this, StyleActivity::class.java).apply {
                putExtra(StyleActivity.EXTRA_PHOTO, photo)
            }
        startActivity(intent)
    }

    private fun askForPortNumber() {
            val view = LayoutInflater.from(
                this@GalleryActivity).inflate(R.layout.port_dialog, null)
            AlertDialog.Builder(this@GalleryActivity)
                .setView(view)
                .setPositiveButton(
                    "OK") {
                        _, _ ->
                            val inputEditText = view.findViewById<EditText>(R.id.port_edit_text)
                            val portNumber = inputEditText.text.toString()
                            ConnectionHandler.establishConnection(portNumber.toInt())
                            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                            sharedPreferences.edit()
                                .putString("port_key", portNumber)
                                .apply()
                    }
                .show()
    }

    override fun onStart() {
        super.onStart()
        gallery.adapter = adapter
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
