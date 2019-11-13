package style.app.controller

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import style.app.R
import style.app.model.Photo

class PhotoGalleryAdapter(val context: Context, val photos: List<Photo>)
    : RecyclerView.Adapter<PhotoGalleryAdapter.PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val photoView = inflater.inflate(R.layout.gallery_item, parent, false)
        return PhotoViewHolder(photoView)
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]
        val imageView = holder.photoImageView

        Picasso.get()
            .load(photo.path)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error)
            .resize(500, 500)
            .centerCrop()
            .into(imageView)
    }

    inner class PhotoViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var photoImageView: ImageView = itemView.findViewById(R.id.gallery_photo)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val photo = photos[position]
                val intent = Intent(context, StylePhotoActivity::class.java).apply {
                    putExtra(StylePhotoActivity.EXTRA_PHOTO, photo)
                }
                context.startActivity(intent)
            }
        }


    }


}
