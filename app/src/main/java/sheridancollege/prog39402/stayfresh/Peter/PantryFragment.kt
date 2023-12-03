package sheridancollege.prog39402.stayfresh.Peter

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import sheridancollege.prog39402.stayfresh.databinding.FragmentPantryBinding

class PantryFragment : Fragment() {

    // Binding variables for the fragment.
    private var _binding: FragmentPantryBinding? = null
    private val binding get() = _binding!!

    private lateinit var pantryAdapter: PantryAdapter
    private lateinit var viewModel: PantryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPantryBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(PantryViewModel::class.java)

        // Get the current user's ID from FirebaseAuth
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("PantryFragment", "No user is logged in.")
            // Handle the case where there is no logged-in user
            return binding.root
        }

        pantryAdapter = PantryAdapter { foodId ->
            viewModel.deleteFood(userId, foodId)
        }

        pantryAdapter = PantryAdapter { foodId ->
            viewModel.deleteFood(userId, foodId)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pantryAdapter
        }

        viewModel.pantryItems.observe(viewLifecycleOwner) { items ->
            pantryAdapter.setPantryItems(items)
        }

        viewModel.fetchPantry(userId)

        viewModel.pantryItems.observe(viewLifecycleOwner) { items ->
            pantryAdapter.setPantryItems(items)
            Log.d("PantryFragment", "Items Loaded: ${items.size}")
        }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addItemButton?.setOnClickListener {
            val action = ContentFragmentDirections.actionContentFragmentToAddFragment()
            findNavController().navigate(action)
        }



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }

}