package sheridancollege.prog39402.stayfresh

import com.google.firebase.Timestamp

data class Food(
        // This property represents the category of the grocery item.
        val Category: String = "",
        // This property represents the description of the grocery item.
        val Description: String = "",
        // This property represents the expiration date of the grocery item.
        val ExpirationDate: Timestamp? = null, // Changed type here
        // This property represents the image of the grocery item's category.
        val CategoryImage: String = "",
        // This property represents the unique identifier of the grocery item.
        var UID: String = ""
    )
