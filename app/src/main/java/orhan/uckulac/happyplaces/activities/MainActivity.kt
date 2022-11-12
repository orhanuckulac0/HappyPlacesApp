package orhan.uckulac.happyplaces.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import orhan.uckulac.happyplaces.adapter.HappyPlacesAdapter
import orhan.uckulac.happyplaces.database.HappyPlaceEntity
import orhan.uckulac.happyplaces.database.HappyPlacesApp
import orhan.uckulac.happyplaces.database.HappyPlacesDAO
import orhan.uckulac.happyplaces.databinding.ActivityMainBinding
import orhan.uckulac.happyplaces.models.HappyPlaceModel
import orhan.uckulac.happyplaces.utils.SwipeToDeleteCallback
import orhan.uckulac.happyplaces.utils.SwipeToEditCallback

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
        getAllPlacesFromDB(dao)

        if (intent.hasExtra("deleted_place")){
            val receivedPlaceToDelete: HappyPlaceModel =intent?.getSerializableExtra("deleted_place") as HappyPlaceModel

            lifecycleScope.launch {
                dao.fetchPlaceById(receivedPlaceToDelete.id).collect { singlePlace ->
                    dao.deletePlace(singlePlace)

                    dao.fetchAllPlaces().collect { allPlaces ->
                        val listOfPlaces = ArrayList(allPlaces)
                        setupListOfPlacesIntoRecyclerView(listOfPlaces)
                    }
                }
            }
        }
    }

    private fun getAllPlacesFromDB(dao: HappyPlacesDAO){
        lifecycleScope.launch {
            dao.fetchAllPlaces().collect(){
                val listOfPlaces = ArrayList(it)
                setupListOfPlacesIntoRecyclerView(listOfPlaces)
            }
        }
    }

    private fun setupListOfPlacesIntoRecyclerView(happyPlacesList: ArrayList<HappyPlaceEntity>){

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

        val editSwipeHandler = object: SwipeToEditCallback(this@MainActivity){
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


        val deleteSwipeHandler = object: SwipeToDeleteCallback(this@MainActivity){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.rvHappyPlaces?.adapter as HappyPlacesAdapter
                adapter.removeAt(this@MainActivity, viewHolder.adapterPosition, ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }
        }

        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(binding?.rvHappyPlaces)
    }

    override fun onResume() {
        super.onResume()
        val dao = (application as HappyPlacesApp).db.happyPlacesDAO()

        lifecycleScope.launch {
            dao.fetchAllPlaces().collect() {
                val listOfPlaces = ArrayList(it)
                setupListOfPlacesIntoRecyclerView(listOfPlaces)
            }
        }
    }

}