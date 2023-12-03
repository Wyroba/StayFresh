package sheridancollege.prog39402.stayfresh

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import sheridancollege.prog39402.stayfresh.databinding.FragmentRecipeBinding
import java.util.*

// RecipeFragment is a Fragment for displaying and managing recipes.
class RecipeFragment : Fragment() {

    // ViewModel and data binding variables.
    private lateinit var viewModel: RecipeViewModel
    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!
    private val functions = FirebaseFunctions.getInstance()
    private val userId: String get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // onCreateView is called to inflate the fragment's layout.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[RecipeViewModel::class.java]
        return binding.root
    }

    // onViewCreated is called after the view is created.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView() // Sets up the RecyclerView for displaying recipes.
        observeViewModel() // Observes data changes in the ViewModel.
        setupSwipeToDelete() // Sets up swipe to delete functionality for recipes.
        setupAddRecipeButton() // Sets up the listener for the add recipe button.
        viewModel.fetchRecipes(userId) // Fetches recipes from the database.
    }

    // Sets up the RecyclerView with a LinearLayoutManager and an adapter.
    private fun setupRecyclerView() {
        binding.recipesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = RecipeAdapter()
        adapter.onRecipeClicked = { recipe ->
            // Displays an AlertDialog with recipe details on recipe click.
            AlertDialog.Builder(requireContext())
                .setTitle("Recipe Details")
                .setMessage(recipe.recipe)
                .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                .show()
        }
        binding.recipesRecyclerView.adapter = adapter
    }

    // Observes the 'recipes' LiveData from the ViewModel and updates the UI accordingly.
    private fun observeViewModel() {
        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            (binding.recipesRecyclerView.adapter as RecipeAdapter).submitList(recipes)
            // Toggles visibility of a TextView based on whether the recipe list is empty.
            binding.noRecipesTextView.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    // Sets up swipe to delete functionality for the RecyclerView.
    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false  // Not needed for swipe functionality
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Handles swipe action to delete a recipe.
                val position = viewHolder.adapterPosition
                val adapter = binding.recipesRecyclerView.adapter as RecipeAdapter
                val recipeToDelete = adapter.getRecipeAt(position)
                recipeToDelete?.let { recipe ->
                    // Confirmation dialog for deleting a recipe.
                    AlertDialog.Builder(requireContext())
                        .setTitle("Delete Recipe")
                        .setMessage("Are you sure you want to delete this recipe?")
                        .setCancelable(false)
                        .setPositiveButton("Delete") { dialog, _ ->
                            viewModel.deleteRecipe(userId, recipe.id)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            adapter.notifyItemChanged(position)
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.recipesRecyclerView)
    }

    // Sets up the listener for the add recipe button.
    private fun setupAddRecipeButton() {
        binding.addRecipeButton.setOnClickListener {
            callOpenAIForRecipes() // Calls a function to get AI-generated recipes.
        }
    }

    // Calls a Firebase cloud function to get an AI-generated recipe.
    private fun callOpenAIForRecipes() {
        FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
            if (tokenTask.isSuccessful) {
                // Passes the Firebase Auth token to the cloud function.
                val token = tokenTask.result?.token ?: ""
                val data = hashMapOf("token" to token)

                binding.loadingProgressBar.visibility = View.VISIBLE
                // Calls the 'callOpenAIForRecipes' cloud function.
                functions.getHttpsCallable("callOpenAIForRecipes")
                    .call(data)
                    .addOnCompleteListener { task ->
                        binding.loadingProgressBar.visibility = View.GONE
                        if (!task.isSuccessful) {
                            // Handles errors in calling the cloud function.
                            val e = task.exception
                            Toast.makeText(context, "Error: ${e?.message}", Toast.LENGTH_SHORT).show()
                            return@addOnCompleteListener
                        }
                        // Displays the received recipe in a dialog.
                        val recipe = task.result?.data as? String
                        recipe?.let {
                            showRecipeDialog(it)
                        }
                    }
            } else {
                // Handles authentication failures.
                Toast.makeText(context, "Authentication failed: ${tokenTask.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Shows a dialog with the AI-generated recipe and options to save or dismiss it.
    private fun showRecipeDialog(recipe: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("AI Generated Recipe Just For You!")
            .setMessage(recipe)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Save") { dialog, _ ->
                saveRecipeToDatabase(recipe)
                dialog.dismiss()
            }
            .show()
    }

    // Saves the recipe to the Firestore database.
    private fun saveRecipeToDatabase(recipeText: String) {
        val recipeMap = hashMapOf(
            "recipe" to recipeText,
            "creationDate" to Calendar.getInstance().time
        )
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("recipes").add(recipeMap)
            .addOnSuccessListener {
                // Notifies the user when a recipe is successfully saved.
                Toast.makeText(context, "Recipe saved successfully!", Toast.LENGTH_SHORT).show()
                viewModel.fetchRecipes(userId)  // Refreshes the recipe list.
            }
            .addOnFailureListener { e ->
                // Handles errors in saving the recipe.
                Toast.makeText(context, "Error saving recipe: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Called when the view is being destroyed.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Clears the binding reference to prevent memory leaks.
    }
}
