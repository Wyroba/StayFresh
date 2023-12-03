package sheridancollege.prog39402.stayfresh.Chi

// Data class to represent a recipe with an ID, creation date, and recipe text
data class Recipe(
    var id: String = "",                 // Unique identifier for the recipe (optional, can be empty)
    val creationDate: com.google.firebase.Timestamp? = null, // Timestamp representing the creation date of the recipe (optional, can be null)
    val recipe: String = ""              // Text of the recipe
)
