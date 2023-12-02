package sheridancollege.prog39402.stayfresh

// Data class to represent a recipe with an ID, creation date, and recipe text
data class Recipe(
    var id: String = "",
    val creationDate: com.google.firebase.Timestamp? = null,
    val recipe: String = ""
)
