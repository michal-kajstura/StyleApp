package style.app.controller

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import style.app.R
import style.app.model.Photo

class CustomAdapter(private val photos: List<Photo>,
                    private val onClickFn: (Photo) -> Unit,
                    private val imageWidth: Int,
                    private val imageHeight: Int,
                    private val item: Int)
    : RecyclerView.Adapter<CustomAdapter.PhotoViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val photoView = inflater.inflate(item, parent, false)
        return PhotoViewHolder(photoView)
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]
        val imageView = holder.photoImageView

        Picasso.get()
            .load(photo.uri)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.error)
            .resize(imageWidth, imageHeight)
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
                onClickFn(photo)
            }
        }
    }
}