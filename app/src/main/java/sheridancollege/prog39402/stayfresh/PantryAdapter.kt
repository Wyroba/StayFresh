package sheridancollege.prog39402.stayfresh

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sheridancollege.prog39402.stayfresh.databinding.ItemLayoutBinding
import java.text.SimpleDateFormat
import java.util.*


class PantryAdapter(private val onDeleteClick: (String) -> Unit) : RecyclerView.Adapter<PantryAdapter.PantryViewHolder>() {

    // List of pantry items to be displayed in the RecyclerView.
    private var pantryItems = listOf<Food>()

    // Update this method with the list of items.
    fun setPantryItems(items: List<Food>) {
        pantryItems = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PantryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLayoutBinding.inflate(inflater, parent, false)
        return PantryViewHolder(binding, onDeleteClick)
    }

    override fun onBindViewHolder(holder: PantryViewHolder, position: Int) {
        holder.bind(pantryItems[position])
    }

    override fun getItemCount(): Int = pantryItems.size

    class PantryViewHolder(private val binding: ItemLayoutBinding, private val onDeleteClick: (String) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(food: Food) {
            binding.categoryView.text = food.Category
            binding.descriptionView.text = food.Description

            // Format the Timestamp to a Date string
            food.ExpirationDate?.let {
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateString = formatter.format(it.toDate())
                binding.expirationView.text = dateString
            } ?: run {
                binding.expirationView.text = "" // Set to empty or some default text if null
            }

            binding.editButton.setOnClickListener {
                onDeleteClick(food.UID)
            }

            // Get the resource ID
            val context = binding.root.context
            val resourceId = context.resources.getIdentifier(food.CategoryImage, "drawable", context.packageName)
            if (resourceId != 0) {
                binding.myImageView.setImageResource(resourceId)
            }
            else {
                binding.myImageView.setImageResource(R.drawable.hotpot)
            }
        }
    }
}
