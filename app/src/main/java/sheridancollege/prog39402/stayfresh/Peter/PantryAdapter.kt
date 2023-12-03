package sheridancollege.prog39402.stayfresh.Peter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sheridancollege.prog39402.stayfresh.R
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
        return PantryViewHolder(binding, parent.context, onDeleteClick)
    }

    override fun onBindViewHolder(holder: PantryViewHolder, position: Int) {
        holder.bind(pantryItems[position])
    }

    override fun getItemCount(): Int = pantryItems.size

    class PantryViewHolder(private val binding: ItemLayoutBinding, private val context: Context, private val onDeleteClick: (String) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(food: Food) {
            binding.categoryView.text = food.category
            binding.descriptionView.text = food.description

            // Format the Timestamp to a Date string
            food.expirationDate?.let {
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateString = formatter.format(it.toDate())
                binding.expirationView.text = dateString
            } ?: run {
                binding.expirationView.text = "" // Set to empty or some default text if null
            }

            binding.editButton.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Confirm Delete")
                builder.setMessage("Are you sure you want to delete this item?")
                builder.setCancelable(false)  // Prevent dismissal by touch outside or back press
                builder.setPositiveButton("Yes") { _, _ ->
                    onDeleteClick(food.UID)
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    // Restore item in the view (prevent swipe away)
                    dialog.dismiss()
                }
                builder.show()
            }

            // Get the resource ID
            val context = binding.root.context
            val resourceId = context.resources.getIdentifier(food.categoryImage, "drawable", context.packageName)
            if (resourceId != 0) {
                binding.myImageView.setImageResource(resourceId)
            }
            else {
                binding.myImageView.setImageResource(R.drawable.hotpot)
            }
        }
    }
}
