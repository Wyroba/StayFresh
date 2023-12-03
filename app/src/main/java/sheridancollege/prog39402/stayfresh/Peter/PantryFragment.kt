package sheridancollege.prog39402.stayfresh.Peter

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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

    private var notificationDialog: AlertDialog? = null



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

        // Check for user's notification permissions.
        if (!areNotificationsEnabled() && !hasUserChosenCancelBefore()) {
            showNotificationPermissionDialog()
        }

    }

    // Check if notifications are enabled for the app.
    private fun areNotificationsEnabled(): Boolean {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.areNotificationsEnabled()
    }

    // Show dialog prompting user to enable notifications.
    private fun showNotificationPermissionDialog() {
        notificationDialog = AlertDialog.Builder(requireContext())
            .setTitle("Notification Permission")
            .setMessage("To ensure you receive important alerts, please enable notifications for this app in the settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                try {
                    val intent = Intent().apply {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                    }
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    val intent = Intent(Settings.ACTION_SETTINGS)
                    startActivity(intent)
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                saveUserChoiceCancel()
            }
            .show()
    }

    private fun hasUserChosenCancelBefore(): Boolean {
        val sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(PREF_NOTIFICATION_DIALOG_SHOWN, false)
    }

    private fun saveUserChoiceCancel() {
        val sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(PREF_NOTIFICATION_DIALOG_SHOWN, true)
            apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }


    companion object {
        private const val PREFS_NAME = "PantryFragmentPreferences"
        private const val PREF_NOTIFICATION_DIALOG_SHOWN = "notificationDialogShown"
    }

}