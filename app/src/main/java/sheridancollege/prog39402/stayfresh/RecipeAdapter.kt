package sheridancollege.prog39402.stayfresh

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sheridancollege.prog39402.stayfresh.databinding.RecipeItemBinding

class RecipeAdapter : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    private var recipes = listOf<Recipe>()
    var onRecipeClicked: ((Recipe) -> Unit)? = null

    fun submitList(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }

    fun getRecipeAt(position: Int): Recipe {
        return recipes[position]
    }

    inner class RecipeViewHolder(private val binding: RecipeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRecipeClicked?.invoke(recipes[position])
                }
            }
        }

        fun bind(recipe: Recipe) {
            val formattedDate = recipe.creationDate?.toDate()?.toString() ?: "Unknown Date"
            binding.creationDateTextView.text = formattedDate

            val lines = recipe.recipe.split("\n")
            val firstTwoLines = lines.take(3).joinToString("\n").trim()
            binding.recipeTextView.text = firstTwoLines
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = RecipeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        Log.d(TAG, "Binding data for position $position: ${recipes[position]}")
        holder.bind(recipes[position])
    }

    override fun getItemCount() = recipes.size
}
