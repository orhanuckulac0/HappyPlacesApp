package orhan.uckulac.happyplaces.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HappyPlaceEntity::class], version = 1, exportSchema = false)
abstract class HappyPlacesDatabase: RoomDatabase() {

    abstract fun happyPlacesDAO(): HappyPlacesDAO

    companion object {
        @Volatile
        private var INSTANCE: HappyPlacesDatabase? = null

        fun getInstance(context: Context): HappyPlacesDatabase {
            synchronized(this){
                var instance = INSTANCE

                if (instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        HappyPlacesDatabase::class.java,
                        "happy_places_database"
                    ).fallbackToDestructiveMigration().build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}