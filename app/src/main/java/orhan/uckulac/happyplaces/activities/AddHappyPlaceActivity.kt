package orhan.uckulac.happyplaces.activities

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.coroutines.launch
import orhan.uckulac.happyplaces.R
import orhan.uckulac.happyplaces.database.HappyPlaceEntity
import orhan.uckulac.happyplaces.database.HappyPlacesApp
import orhan.uckulac.happyplaces.database.HappyPlacesDAO
import orhan.uckulac.happyplaces.databinding.ActivityAddHappyPlaceBinding
import orhan.uckulac.happyplaces.models.HappyPlaceModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
        private const val AUTOCOMPLETE_REQUEST_CODE = 1
    }

    private var binding: ActivityAddHappyPlaceBinding? = null
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage: Uri? = null
    private var longitude = 0.0
    private var latitude = 0.0

    private var mHappyPlaceDetails: HappyPlaceModel? = null

    // create a result launcher to get the image URI from the phone gallery
    // first define what kind of a launcher will it be? 'intent'
    // register for activity result to get result
    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            // check the result, if It's okay and if the result data is not empty,
            // then get the location of the data, URI, and assign it as background image
            if (result.resultCode == RESULT_OK && result.data != null){
                val contentURI = result.data?.data
                val imageBitmap: Bitmap?
                try{
//                  getBitmap() is deprecated on sdk > 28
                    if(Build.VERSION.SDK_INT < 28) {
                        imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, contentURI)
                        saveImageToInternalStorage = saveImageToInternalStorage(imageBitmap)

                    }else{   // more up to date approach -- if sdk > 28
                        val source: ImageDecoder.Source = ImageDecoder.createSource(contentResolver, contentURI!!)
                        imageBitmap = ImageDecoder.decodeBitmap(source)
                        saveImageToInternalStorage = saveImageToInternalStorage(imageBitmap)
                    }

                    binding?.ivPlaceImage?.setImageURI(contentURI)
                }
                catch (e: IOException){
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to load image from gallery", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val openCameraLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if (result.resultCode == RESULT_OK && result.data != null){
                try {
                    val photoTaken : Bitmap = result.data?.extras!!.get("data") as Bitmap
                    saveImageToInternalStorage = saveImageToInternalStorage(photoTaken)
                    binding?.ivPlaceImage?.setImageBitmap(photoTaken)
                }
                catch (e: IOException){
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to load image taken with camera", Toast.LENGTH_SHORT).show() }

            } else {
                showRationaleDialog("Happy Places App needs camera permission for you to use your camera. " +
                            "Would you like to go to your app settings to allow permission?"
                )
            }
        }

    private val requestPermission: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                permission ->
            if (permission) {
                // start an intent to access Media in the phone
                val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                openGalleryLauncher.launch(pickIntent)

            } else {
                showRationaleDialog("Happy Places App needs media permission for you to use your gallery. " +
                        "Would you like to go to your app settings to allow permission?"
                )
            }
        }

    private val addressAutoCompleteLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result ->
            if (result.resultCode == RESULT_OK && result.data != null){
                try{
                    val data = result.data
                    val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                    binding?.etLocation?.setText(place.address)
                    latitude = place.latLng!!.latitude
                    longitude = place.latLng!!.longitude

                }catch(e:Exception){
                    e.printStackTrace()
                }
            }else if (result.resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                Log.e("AutoComplete Cancelled", "User canceled autocomplete");
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolbarAddPlace?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()  // update view when user selects a date
        }

        binding?.etDate?.setOnClickListener(this@AddHappyPlaceActivity)
        binding?.tvAddImage?.setOnClickListener(this@AddHappyPlaceActivity)
        binding?.etLocation?.setOnClickListener(this@AddHappyPlaceActivity)  // for google maps api

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetails = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }

        if (mHappyPlaceDetails != null){
            supportActionBar?.title = "Edit Happy Place"
            binding?.etTitle?.setText(mHappyPlaceDetails!!.title)
            binding?.etDescription?.setText(mHappyPlaceDetails!!.description)
            binding?.etDate?.setText(mHappyPlaceDetails!!.date)
            binding?.etLocation?.setText(mHappyPlaceDetails!!.location)

            latitude = mHappyPlaceDetails!!.latitude
            longitude = mHappyPlaceDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(mHappyPlaceDetails!!.image)
            binding?.ivPlaceImage?.setImageURI(saveImageToInternalStorage)

            binding?.btnSave?.text = "UPDATE"

        }else{
            supportActionBar?.title = "Add Happy Place"
        }

        val dao = (application as HappyPlacesApp).db.happyPlacesDAO()

        // check if item is being edited or added for the first time
        if (binding?.btnSave?.text == "SAVE"){
            binding?.btnSave?.setOnClickListener {
                addPlaceToDB(dao)
                val intent = Intent(this@AddHappyPlaceActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }else{
            binding?.btnSave?.setOnClickListener {
                // pass the current place ID to update it
                updatePlaceInDB(mHappyPlaceDetails!!.id, dao)
            }
        }

        if (!Places.isInitialized()){
            Places.initialize(this@AddHappyPlaceActivity, resources.getString(R.string.google_maps_api_key))

        }

    }

    override fun onClick(v: View?) {
        when(v!!.id){
            binding?.etDate?.id ->{
                DatePickerDialog(
                    this@AddHappyPlaceActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            binding?.tvAddImage?.id ->{
                val alertdialog = AlertDialog.Builder(this@AddHappyPlaceActivity)
                alertdialog.setTitle("Select Action")
                val alertDialogItems = arrayOf("Select photo from Gallery.", "Capture photo from camera.")
                alertdialog.setItems(alertDialogItems){
                    _, which ->
                    when(which){
                        0 -> requestStoragePermission()
                        1 -> requestCameraPermission()
                    }
                }
                alertdialog.show()
            }
            binding?.etLocation?.id ->{
                // google maps auto complete api setup
                // startActivityForResult is deprecated, this is more up to date approach.
                val fields = listOf(
                    Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
                )
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
                addressAutoCompleteLauncher.launch(intent)
            }
        }
    }

    private fun updateDateInView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding?.etDate?.setText(sdf.format(cal.time).toString())
    }

    private fun requestStoragePermission(){
        // check if gallery permission is not granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Pass any permission you want while launching
            requestPermission.launch(Manifest.permission.CAMERA)

        }else{
            // if it is granted
            val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            openGalleryLauncher.launch(pickIntent)
        }
    }

    private fun requestCameraPermission(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            openCameraLauncher.launch(intent)
        }else{
            requestPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun showRationaleDialog(message: String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Happy Places App")
            .setMessage(message)
            .setNegativeButton("Cancel"){
                    dialog, _-> dialog.dismiss()
            }
            .setPositiveButton("Yes"){
                // redirect user to app settings to allow permission for gallery
                    _, _-> startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            })
            }

        builder.create().show()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)  // extends context
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)  // make file only accessible only to this app
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)  // Creates a file output stream to write to the file with the specified name.
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()

        }catch (e: IOException){
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }

    private fun addPlaceToDB(happyPlacesDAO: HappyPlacesDAO){
        lifecycleScope.launch {
            happyPlacesDAO.insertPlace(
                HappyPlaceEntity(
                    0,
                    title = binding?.etTitle?.text.toString(),
                    imagePath = saveImageToInternalStorage.toString(),
                    description = binding?.etDescription?.text.toString(),
                    date = binding?.etDate?.text.toString(),
                    location = binding?.etLocation?.text.toString(),
                    longitude = longitude,
                    latitude = latitude
                )
            )
        }
    }

    private fun updatePlaceInDB(id: Int, happyPlacesDAO: HappyPlacesDAO){
        lifecycleScope.launch {
            val updatedTitle = binding?.etTitle?.text.toString()
            val updatedDescription = binding?.etDescription?.text.toString()
            val updatedImagePath = saveImageToInternalStorage.toString()
            val updatedDate = binding?.etDate?.text.toString()
            val updatedLocation = binding?.etLocation?.text.toString()

            if (updatedTitle.isNotEmpty()
                && updatedDescription.isNotEmpty()
                && updatedImagePath.isNotEmpty()
                && updatedDate.isNotEmpty()
                && updatedLocation.isNotEmpty()
            ){
                happyPlacesDAO.updatePlace(HappyPlaceEntity(
                    id,
                    updatedTitle,
                    updatedImagePath,
                    updatedDescription,
                    updatedDate,
                    updatedLocation,
                    longitude,
                    latitude
                    )
                )
                val intent = Intent(this@AddHappyPlaceActivity, MainActivity::class.java)
                startActivity(intent)
                finish()

            }else{
                Toast.makeText(applicationContext, "Title, Description, Image, Date or Location can not be empty.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (binding != null){
            binding = null
        }
    }
}