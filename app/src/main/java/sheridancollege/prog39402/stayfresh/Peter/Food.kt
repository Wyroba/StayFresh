package sheridancollege.prog39402.stayfresh.Peter

import com.google.firebase.Timestamp

data class Food(
        // This property represents the category of the grocery item.
        val category: String = "",
        // This property represents the description of the grocery item.
        val description: String = "",
        // This property represents the expiration date of the grocery item.
        val expirationDate: Timestamp? = null, // Changed type here
        // This property represents the image of the grocery item's category.
        val categoryImage: String = "",
        // This property represents the unique identifier of the grocery item.
        var UID: String = ""
    )
