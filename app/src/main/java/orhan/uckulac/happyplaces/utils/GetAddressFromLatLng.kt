package orhan.uckulac.happyplaces.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class GetAddressFromLatLng(context: Context,
                           private val latitude: Double,
                           private val longitude: Double){

    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
    private lateinit var mAddressListener: AddressListener

    suspend fun launchBackgroundProcessForRequest() {
        val address = getAddress()  // not related to Main UI at the moment.

        withContext(Dispatchers.Main){
            // switch to UI because we will write on UI
            // this scope will run on UI
            if (address.isNotEmpty()){
                mAddressListener.onAddressFound(address)
            }else{
                mAddressListener.onError()
            }
        }
    }


    private fun getAddress(): String {
        try {
            val addressList: List<Address>? = geocoder.getFromLocation(latitude,longitude, 1)
            if (addressList != null && addressList.isNotEmpty()){
                val address: Address = addressList[0]
                val stringBuilder = StringBuilder()
                for (i in 0..address.maxAddressLineIndex){
                    stringBuilder.append(address.getAddressLine(i)).append(" ")
                }

                stringBuilder.deleteCharAt(stringBuilder.length-1)
                return stringBuilder.toString()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        return ""
    }

    fun setCustomAddressListener(addressListener: AddressListener){
        //to attach the listener to the class property
        this.mAddressListener=addressListener
    }

    interface AddressListener {
        fun onAddressFound(address: String?)
        fun onError()
    }
}