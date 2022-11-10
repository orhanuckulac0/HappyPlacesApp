package orhan.uckulac.happyplaces

import android.app.Application

class HappyPlacesApp: Application() {
    val db by lazy { HappyPlacesDatabase.getInstance(this)  // only create when its needed
    }
}