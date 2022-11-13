package orhan.uckulac.happyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import orhan.uckulac.happyplaces.R
import orhan.uckulac.happyplaces.databinding.ActivityMapBinding
import orhan.uckulac.happyplaces.models.HappyPlaceModel

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private var binding: ActivityMapBinding? = null

    private var mHappyPlaceDetail: HappyPlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (intent.hasExtra("current_place_detail")){
            mHappyPlaceDetail = intent.getSerializableExtra("current_place_detail") as HappyPlaceModel
        }

        if (mHappyPlaceDetail != null){
            setSupportActionBar(binding?.mapToolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = mHappyPlaceDetail!!.title
            binding?.mapToolbar?.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }


        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.apply {
            val location = LatLng(mHappyPlaceDetail!!.latitude, mHappyPlaceDetail!!.longitude)
            addMarker(
                MarkerOptions()
                    .position(location)
                    .title(mHappyPlaceDetail!!.location)
            )

            val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(location, 10.0F)
            googleMap.animateCamera(newLatLngZoom);
        }

        googleMap.setOnMapClickListener(object :GoogleMap.OnMapClickListener {
            override fun onMapClick(latlng :LatLng) {
                // Clears the previously touched position
                googleMap.clear();
                // Animating to the touched position
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));

                val location = LatLng(latlng.latitude,latlng.longitude)
                googleMap.addMarker(MarkerOptions().position(location))
            }
        })
    }
}