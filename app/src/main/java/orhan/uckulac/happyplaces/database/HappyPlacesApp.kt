package orhan.uckulac.happyplaces.database

import android.app.Application
import orhan.uckulac.happyplaces.database.HappyPlacesDatabase

class HappyPlacesApp: Application() {
    val db by lazy { HappyPlacesDatabase.getInstance(this)  // only create when its needed
    }
}