package orhan.uckulac.happyplaces

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import orhan.uckulac.happyplaces.databinding.ActivityAddHappyPlaceBinding
import permissions.dispatcher.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private var binding: ActivityAddHappyPlaceBinding? = null
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    // create a result launcher to get the image URI from the phone gallery
    // first define what kind of a launcher will it be? 'intent'
    // register for activity result to get result
    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            // check the result, if It's okay and if the result data is not empty,
            // then get the location of the data, URI, and assign it as background image
            if (result.resultCode == RESULT_OK && result.data != null){
                val imageBackground: AppCompatImageView? = binding?.ivPlaceImage
                imageBackground?.setImageURI(result.data?.data)
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
                showRationaleDialog("Happy Places App",
                    "Happy Places App needs media permission for you to use your gallery. " +
                        "Would you like to go to your app settings to allow permission?"
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolbarAddPlace?.setNavigationOnClickListener {
            onBackPressed()
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()  // update view when user selects a date
        }

        binding?.etDate?.setOnClickListener(this@AddHappyPlaceActivity)
        binding?.tvAddImage?.setOnClickListener(this@AddHappyPlaceActivity)
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
                        1 -> Toast.makeText(this@AddHappyPlaceActivity, "Camera selection coming soon", Toast.LENGTH_LONG).show()
                    }
                }
                alertdialog.show()
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

    private fun showRationaleDialog(
        title: String,
        message: String,
    ){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
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
}