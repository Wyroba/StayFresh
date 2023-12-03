package sheridancollege.prog39402.stayfresh.Peter

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PantryViewModel : ViewModel() {
    // MutableLiveData to hold the list of recipes. Private to avoid exposing modification methods.
    private val _pantryItems = MutableLiveData<List<Food>>()

    // Public LiveData for observing the recipes from the UI components.
    val pantryItems: LiveData<List<Food>> = _pantryItems

    // Function to fetch pantry items from Firestore for a specific user.
    fun fetchPantry(userId: String) {
        // Launch a coroutine in the ViewModel's scope.
        viewModelScope.launch {
            try {
                // Perform the Firestore query in the IO dispatcher for network operations.
                Log.d("PantryViewModel", "Fetching pantry items for user: $userId")
                val fetchedFood = withContext(Dispatchers.IO) {
                    val db = FirebaseFirestore.getInstance()
                    val pantryRef = db.collection("users").document(userId).collection("pantry")
                    // Await the result of the asynchronous operation.
                    pantryRef.get().await().map { document ->
                        // Convert each document to Food object and set its ID.
                        document.toObject(Food::class.java).apply { UID = document.id }
                    }
                }
                // Post the fetched recipes to the MutableLiveData.
                _pantryItems.postValue(fetchedFood)
                Log.d("PantryViewModel", "Fetched ${fetchedFood.size} items")
            } catch (exception: Exception) {
                Log.e("PantryViewModel", "Error fetching pantry items", exception)
                // Post an empty list and handle the exception (e.g., logging, user notification).
                _pantryItems.postValue(emptyList())
            }
        }
    }

    // Function to delete a specific recipe from Firestore.
    fun deleteFood(userId: String, foodId: String) {
        // Launch a coroutine in the ViewModel's scope.
        viewModelScope.launch {
            try {
                Log.d("PantryViewModel", "Deleting recipe with ID: $foodId for user: $userId")
                // Perform the Firestore delete operation in the IO dispatcher.
                withContext(Dispatchers.IO) {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(userId).collection("pantry").document(foodId).delete().await()
                    fetchPantry(userId) // Refresh the list
                }
                // Refresh the recipe list after deletion.
                fetchPantry(userId)
            } catch (exception: Exception) {
                Log.e("PantryViewModel", "Error deleting recipe", exception)
                // Handle the error case (e.g., logging, user notification).
            }
        }
    }

    fun addFoodItem(userId: String, food: Food) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(userId).collection("pantry").add(food).await()
                fetchPantry(userId) // Refresh the list
            } catch (exception: Exception) {
                Log.e("PantryViewModel", "Error adding food item", exception)
            }
        }
    }

}