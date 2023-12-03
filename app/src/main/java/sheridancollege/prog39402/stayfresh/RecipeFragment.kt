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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import sheridancollege.prog39402.stayfresh.databinding.FragmentRecipeBinding
import java.util.*

class RecipeFragment : Fragment() {

    private lateinit var viewModel: RecipeViewModel
    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!
    private val functions = FirebaseFunctions.getInstance()
    private val userId: String get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[RecipeViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        setupSwipeToDelete()
        setupAddRecipeButton()
        viewModel.fetchRecipes(userId)
    }

    private fun setupRecyclerView() {
        binding.recipesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = RecipeAdapter()
        adapter.onRecipeClicked = { recipe ->
            AlertDialog.Builder(requireContext())
                .setTitle("Recipe Details")
                .setMessage(recipe.recipe)
                .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                .show()
        }
        binding.recipesRecyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            (binding.recipesRecyclerView.adapter as RecipeAdapter).submitList(recipes)
            binding.noRecipesTextView.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false  // Not needed for swipe functionality
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val adapter = binding.recipesRecyclerView.adapter as RecipeAdapter
                val recipeToDelete = adapter.getRecipeAt(position) // Use your custom method here
                recipeToDelete?.let { recipe ->
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

    private fun setupAddRecipeButton() {
        binding.addRecipeButton.setOnClickListener {
            callOpenAIForRecipes()
        }
    }

    private fun callOpenAIForRecipes() {
        FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
            if (tokenTask.isSuccessful) {
                val token = tokenTask.result?.token ?: ""
                val data = hashMapOf("token" to token)

                binding.loadingProgressBar.visibility = View.VISIBLE
                functions.getHttpsCallable("callOpenAIForRecipes")
                    .call(data)
                    .addOnCompleteListener { task ->
                        binding.loadingProgressBar.visibility = View.GONE
                        if (!task.isSuccessful) {
                            val e = task.exception
                            Toast.makeText(context, "Error: ${e?.message}", Toast.LENGTH_SHORT).show()
                            return@addOnCompleteListener
                        }
                        val recipe = task.result?.data as? String
                        recipe?.let {
                            showRecipeDialog(it)
                        }
                    }
            } else {
                Toast.makeText(context, "Authentication failed: ${tokenTask.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


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

    private fun saveRecipeToDatabase(recipeText: String) {
        val recipeMap = hashMapOf(
            "recipe" to recipeText,
            "creationDate" to Calendar.getInstance().time
        )
        FirebaseFirestore.getInstance().collection("users").document(userId).collection("recipes").add(recipeMap)
            .addOnSuccessListener {
                Toast.makeText(context, "Recipe saved successfully!", Toast.LENGTH_SHORT).show()
                viewModel.fetchRecipes(userId)  // Refresh the list
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error saving recipe: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Correctly clear the binding reference
    }
}
