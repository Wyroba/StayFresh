package sheridancollege.prog39402.stayfresh.Chi

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sheridancollege.prog39402.stayfresh.databinding.RecipeItemBinding

class RecipeAdapter : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    // List of recipes to be displayed in the RecyclerView.
    private var recipes = listOf<Recipe>()

    // Callback for handling recipe item clicks.
    var onRecipeClicked: ((Recipe) -> Unit)? = null

    // Function to update the list of recipes and refresh the RecyclerView.
    fun submitList(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }

    // Function to get a recipe at a specific position.
    fun getRecipeAt(position: Int): Recipe {
        return recipes[position]
    }

    inner class RecipeViewHolder(private val binding: RecipeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // Set an onClickListener for recipe items.
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRecipeClicked?.invoke(recipes[position])
                }
            }
        }

        // Bind data to the ViewHolder.
        fun bind(recipe: Recipe) {
            // Format and set the creation date of the recipe.
            val formattedDate = recipe.creationDate?.toDate()?.toString() ?: "Unknown Date"
            binding.creationDateTextView.text = formattedDate

            // Split the recipe text into lines and display the first two lines.
            val lines = recipe.recipe.split("\n")
            val firstTwoLines = lines.take(3).joinToString("\n").trim()
            binding.recipeTextView.text = firstTwoLines
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        // Inflate the recipe item layout and create a RecipeViewHolder.
        val binding = RecipeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        // Bind data to the ViewHolder at the specified position.
        Log.d(TAG, "Binding data for position $position: ${recipes[position]}")
        holder.bind(recipes[position])
    }

    override fun getItemCount() = recipes.size
}
