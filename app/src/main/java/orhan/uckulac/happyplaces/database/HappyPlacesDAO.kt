package orhan.uckulac.happyplaces.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HappyPlacesDAO {
    @Insert
    suspend fun insertPlace(happyPlaceEntity: HappyPlaceEntity)

    @Update
    suspend fun updatePlace(happyPlaceEntity: HappyPlaceEntity)

    @Delete
    suspend fun deletePlace(happyPlaceEntity: HappyPlaceEntity)

    @Query("SELECT * FROM 'happy-places-table'")
    fun fetchAllPlaces(): Flow<List<HappyPlaceEntity>>

    @Query("SELECT * FROM 'happy-places-table' where id=:id")
    fun fetchPlaceById(id:Int):Flow<HappyPlaceEntity>

}