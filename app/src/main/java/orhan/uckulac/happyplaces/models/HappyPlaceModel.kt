package orhan.uckulac.happyplaces.models

data class HappyPlaceModel(
    val id: Int,
    val title: String,
    val image: String,
    val description: String,
    val date: String,
    val location: String,
    val longitude: Double,
    val latitude: Double
): java.io.Serializable