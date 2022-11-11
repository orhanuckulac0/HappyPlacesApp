package orhan.uckulac.happyplaces.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import orhan.uckulac.happyplaces.activities.HappyPlaceDetailActivity
import orhan.uckulac.happyplaces.database.HappyPlaceEntity
import orhan.uckulac.happyplaces.database.HappyPlaceModel
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

        // enable user to go place details activity
        // pass current obj to HappyPlaceDetailActivity
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, HappyPlaceDetailActivity::class.java)
            val currentObject = HappyPlaceModel(
                id=model.id,
                title=model.title,
                image = model.imagePath,
                description = model.description,
                date = model.date,
                location = model.location,
                latitude =  model.latitude,
                longitude = model.longitude
            )
            intent.putExtra("extra_place_details", currentObject as java.io.Serializable)
            it.context.startActivity(intent)
        }

        //TODO set onclick listeners to edit and delete buttons which I will be adding next
    }

    override fun getItemCount(): Int {
        return list.size
    }

    companion object {
        var EXTRA_PLACE_DETAILS = "extra_place_details"
    }
}