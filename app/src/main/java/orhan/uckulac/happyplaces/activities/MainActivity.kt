package orhan.uckulac.happyplaces.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import orhan.uckulac.happyplaces.AddHappyPlaceActivity
import orhan.uckulac.happyplaces.adapter.HappyPlacesAdapter
import orhan.uckulac.happyplaces.database.HappyPlaceEntity
import orhan.uckulac.happyplaces.database.HappyPlacesApp
import orhan.uckulac.happyplaces.database.HappyPlacesDAO
import orhan.uckulac.happyplaces.databinding.ActivityMainBinding
import orhan.uckulac.happyplaces.utils.SwipeToDeleteCallback

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    companion object {
        private const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        internal const val EXTRA_PLACE_DETAILS = "extra_place_details"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.fabAddHappyPlace?.setOnClickListener {
            val intent = Intent(this@MainActivity, AddHappyPlaceActivity::class.java)
            startActivity(intent)
        }

        val dao = (application as HappyPlacesApp).db.happyPlacesDAO()

        lifecycleScope.launch {
            dao.fetchAllPlaces().collect(){
                val listOfPlaces = ArrayList(it)
                setupListOfPlacesIntoRecyclerView(listOfPlaces, dao)
            }
        }
    }

    private fun setupListOfPlacesIntoRecyclerView(happyPlacesList: ArrayList<HappyPlaceEntity>, happyPlacesDAO: HappyPlacesDAO){

        if (happyPlacesList.isNotEmpty()){
            lifecycleScope.launch{
                val placesAdapter = HappyPlacesAdapter(this@MainActivity.applicationContext, happyPlacesList)
                binding?.rvHappyPlaces?.layoutManager = LinearLayoutManager(this@MainActivity)
                binding?.rvHappyPlaces?.adapter = placesAdapter
                binding?.rvHappyPlaces?.setHasFixedSize(true)
                binding?.rvHappyPlaces?.visibility = View.VISIBLE
                binding?.tvNoRecordsAvailable?.visibility = View.GONE
            }
        }else{
            binding?.rvHappyPlaces?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }

        val editSwipeHandler = object: SwipeToDeleteCallback(this@MainActivity){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.rvHappyPlaces?.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(
                    this@MainActivity,
                    viewHolder.adapterPosition,
                    ADD_PLACE_ACTIVITY_REQUEST_CODE
                )
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(binding?.rvHappyPlaces)

    }
}