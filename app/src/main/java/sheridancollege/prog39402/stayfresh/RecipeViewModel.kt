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
    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> = _recipes

    fun fetchRecipes(userId: String) {
        viewModelScope.launch {
            try {
                val fetchedRecipes = withContext(Dispatchers.IO) {
                    val db = FirebaseFirestore.getInstance()
                    val recipesRef = db.collection("users").document(userId).collection("recipes")
                    recipesRef.get().await().map { document ->
                        document.toObject(Recipe::class.java).apply { id = document.id }
                    }
                }
                _recipes.postValue(fetchedRecipes)
            } catch (exception: Exception) {
                _recipes.postValue(emptyList())
                // Log the error or inform the user
            }
        }
    }

    fun deleteRecipe(userId: String, recipeId: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val db = FirebaseFirestore.getInstance()
                    val recipeRef = db.collection("users").document(userId).collection("recipes").document(recipeId)
                    recipeRef.delete().await()
                }
                fetchRecipes(userId) // Refresh the list
            } catch (exception: Exception) {
                // Handle the error case, e.g., log the error or inform the user
            }
        }
    }
}
