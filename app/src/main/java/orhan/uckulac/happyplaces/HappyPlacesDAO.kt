package orhan.uckulac.happyplaces

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HappyPlacesDAO {
    @Insert
    suspend fun insert(happyPlaceEntity: HappyPlaceEntity)

    @Update
    suspend fun updateUsers(happyPlaceEntity: HappyPlaceEntity)

    @Delete
    suspend fun delete(happyPlaceEntity: HappyPlaceEntity)

    @Query("SELECT * FROM 'happy-places-table'")
    fun fetchAllPlace(): Flow<List<HappyPlaceEntity>>
}