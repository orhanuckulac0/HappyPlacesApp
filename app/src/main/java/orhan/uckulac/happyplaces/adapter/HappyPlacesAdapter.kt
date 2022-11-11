package orhan.uckulac.happyplaces.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import orhan.uckulac.happyplaces.database.HappyPlaceEntity
import orhan.uckulac.happyplaces.databinding.ItemHappyPlaceBinding

class HappyPlacesAdapter(private val list: ArrayList<HappyPlaceEntity>)
    : RecyclerView.Adapter<HappyPlacesAdapter.ViewHolder>() {

    class ViewHolder(binding: ItemHappyPlaceBinding) : RecyclerView.ViewHolder(binding.root){
        val tvTitle = binding.tvTitle
        val tvDescription = binding.tvDescription
        val sivImage = binding.sivPlaceImage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemHappyPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        holder.tvTitle.text = model.title
        holder.tvDescription.text = model.description
        holder.sivImage.setImageURI(Uri.parse(model.imagePath))

        //TODO set onclick listeners to edit and delete buttons which will be added
    }

    override fun getItemCount(): Int {
        return list.size
    }
}