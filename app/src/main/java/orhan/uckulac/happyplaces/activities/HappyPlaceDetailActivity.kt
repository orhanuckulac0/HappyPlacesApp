package orhan.uckulac.happyplaces.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import orhan.uckulac.happyplaces.models.HappyPlaceModel
import orhan.uckulac.happyplaces.databinding.ActivityHappyPlaceDetailBinding

class HappyPlaceDetailActivity : AppCompatActivity() {
    private var binding: ActivityHappyPlaceDetailBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarHappyPlaceDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolbarHappyPlaceDetail?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val receivedPlaceDetail: HappyPlaceModel =intent?.getSerializableExtra("extra_place_details") as HappyPlaceModel
        showCurrentPlaceDetails(receivedPlaceDetail)
    }

    private fun showCurrentPlaceDetails(placeDetail: HappyPlaceModel){
        supportActionBar!!.title = placeDetail.title

        binding?.ivPlaceImageDetail?.setImageURI(Uri.parse(placeDetail.image))
        binding?.tvDescriptionDetail?.text = placeDetail.description
        binding?.tvLocationDetail?.text = placeDetail.location
    }
}