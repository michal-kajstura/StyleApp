package style.app.controller

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import style.app.R
import style.app.model.Photo

abstract class CustomAdapter<T>(protected val context: Context,
                                val photos: List<T>, val onClickFn: (T) -> Unit)
    : RecyclerView.Adapter<CustomAdapter<T>.PhotoViewHolder>() {
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

        loadInto(photo, imageView)

    }

    abstract fun loadInto(image: T, imageView: ImageView)

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
                onClickFn(photo)
            }
        }
    }
}

class PhotoGalleryAdapter(context_: Context, photos_: List<Photo>, fn_: (Photo) -> Unit)
    : CustomAdapter<Photo>(context_, photos_, fn_) {

    override fun loadInto(image: Photo, imageView: ImageView) {
        Picasso.get()
            .load(image.uri)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error)
            .resize(500, 500)
            .centerCrop()
            .into(imageView)
    }
}


class StyleBarAdaper(context_: Context, photos_: List<Bitmap>, fn_: (Bitmap) -> Unit)
    : CustomAdapter<Bitmap>(context_, photos_, fn_) {

    override fun loadInto(image: Bitmap, imageView: ImageView) {
        imageView.setImageBitmap(image)
    }
}