package orhan.uckulac.happyplaces

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "happy-places-table")
data class HappyPlaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String,
    val imagePath: String,
    val description: String,
    val date: String,
    val location: String,
    val longitude: Double,
    val latitude: Double
)