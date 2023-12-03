package sheridancollege.prog39402.stayfresh

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RecipeViewModel : ViewModel() {
    // MutableLiveData to hold the list of recipes. Private to avoid exposing modification methods.
    private val _recipes = MutableLiveData<List<Recipe>>()

    // Public LiveData for observing the recipes from the UI components.
    val recipes: LiveData<List<Recipe>> = _recipes

    // Function to fetch recipes from Firestore for a specific user.
    fun fetchRecipes(userId: String) {
        // Launch a coroutine in the ViewModel's scope.
        viewModelScope.launch {
            try {
                // Perform the Firestore query in the IO dispatcher for network operations.
                val fetchedRecipes = withContext(Dispatchers.IO) {
                    val db = FirebaseFirestore.getInstance()
                    val recipesRef = db.collection("users").document(userId).collection("recipes")
                    // Await the result of the asynchronous operation.
                    recipesRef.get().await().map { document ->
                        // Convert each document to Recipe object and set its ID.
                        document.toObject(Recipe::class.java).apply { id = document.id }
                    }
                }
                // Post the fetched recipes to the MutableLiveData.
                _recipes.postValue(fetchedRecipes)
            } catch (exception: Exception) {
                // Post an empty list and handle the exception (e.g., logging, user notification).
                _recipes.postValue(emptyList())
                // Log the error or inform the user
            }
        }
    }

    // Function to delete a specific recipe from Firestore.
    fun deleteRecipe(userId: String, recipeId: String) {
        // Launch a coroutine in the ViewModel's scope.
        viewModelScope.launch {
            try {
                // Perform the Firestore delete operation in the IO dispatcher.
                withContext(Dispatchers.IO) {
                    val db = FirebaseFirestore.getInstance()
                    val recipeRef = db.collection("users").document(userId).collection("recipes").document(recipeId)
                    recipeRef.delete().await()
                }
                // Refresh the recipe list after deletion.
                fetchRecipes(userId)
            } catch (exception: Exception) {
                // Handle the error case (e.g., logging, user notification).
            }
        }
    }
}
