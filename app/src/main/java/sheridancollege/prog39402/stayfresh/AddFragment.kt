package sheridancollege.prog39402.stayfresh

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import sheridancollege.prog39402.stayfresh.databinding.FragmentAddBinding

class AddFragment : Fragment() {

    private var dialogEditText: EditText? = null

    // A binding object instance, corresponding to the fragment layout
    var _binding: FragmentAddBinding? = null

    // This property delegates the access to _binding and throws an exception if it's null.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Inflate the layout using data binding and assign it to the _binding variable.
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create a hardcoded list of food categories for the grid.
        val catDataSet = listOf(
            CatGridItem(R.drawable.fruits, "Fruit", "fruits"),
            CatGridItem(R.drawable.vegetable, "Vegetable", "vegetable"),
            CatGridItem(R.drawable.meat, "Meat", "meat"),
            CatGridItem(R.drawable.seafood, "Seafood", "seafood"),
            CatGridItem(R.drawable.dairy, "Dairy", "dairy"),
            CatGridItem(R.drawable.grains, "Grains", "grains"),
            CatGridItem(R.drawable.canfood, "Canned Goods", "canfood"),
            CatGridItem(R.drawable.snack, "Snacks", "snack"),
            CatGridItem(R.drawable.bev, "Beverages", "bev"),
            CatGridItem(R.drawable.condiments, "Condiments", "condiments"),
            CatGridItem(R.drawable.bakery, "Baked Goods", "bakery"),
            CatGridItem(R.drawable.frozenfood, "Frozen Foods", "frozenfood"),
            CatGridItem(R.drawable.bento, "Prepped Meals", "bento"),
            CatGridItem(R.drawable.babyfood, "Baby Food", "babyfood"),
            CatGridItem(R.drawable.petfood, "Pet Food", "petfood"),
            CatGridItem(R.drawable.menu, "Other Food", "menu")
        )

        // Configure the RecyclerView layout manager to display items in a grid with 4 columns.
        val numColumns = 4 // or however many columns you want to display
        binding.myRecyclerView.layoutManager = GridLayoutManager(requireContext(), numColumns)

        // Define an onClick listener for each grid item.
        val onClick: (CatGridItem) -> Unit = { item ->
            addItem(item.text, item.imageName)
        }

    }

    // Function to display a dialog box for the user to add a new food item
    fun addItem(category: String, image: String){
        // Prepare a dialog builder
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add Food Item")
        builder.setMessage("Please enter the name of the food item:")

        // Inflate custom layout
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_input_layout, null)
        builder.setView(dialogView)

        // Access the EditText and the VoiceInput button from the custom layout
        dialogEditText = dialogView.findViewById(R.id.editTextInput)


        // Set the action for the OK button.
        builder.setPositiveButton("OK") { dialog, which ->
            val foodItemName = dialogEditText?.text.toString().trim()
            if (isValidFoodItemName(foodItemName)) {
                handleFoodItemName(foodItemName)
                Log.d("TAG", foodItemName)
                //val directions = AddFragmentDirections.actionAddFragmentToScannerFragment(foodItemName, category, image)
                //findNavController().navigate(directions)

            } else {
                // Show an error message if the entered food item name is invalid
                Toast.makeText(requireContext(), "Invalid food item name", Toast.LENGTH_SHORT).show()
            }
        }
        // Set the action for the Cancel button.
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }

        // Display the created alert dialog to the user.
        builder.show()
    }

    // Function to check if the entered food item name is valid.
    fun isValidFoodItemName(foodItemName: String): Boolean {
        // A name is valid if it's not blank and its length doesn't exceed 50
        return foodItemName.isNotBlank() && foodItemName.length <= 50
    }

    // Function to show a dialog to the user to enter a new food item's name.
    fun handleFoodItemName(foodItemName: String) {
        // Display a short message indicating that the food item has been added
        Toast.makeText(requireContext(), "Added $foodItemName to list", Toast.LENGTH_SHORT).show()
    }

}