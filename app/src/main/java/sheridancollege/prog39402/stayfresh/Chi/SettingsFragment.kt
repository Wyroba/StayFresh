package sheridancollege.prog39402.stayfresh.Chi

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import sheridancollege.prog39402.stayfresh.Peter.ContentFragmentDirections
import sheridancollege.prog39402.stayfresh.Peter.TwoDayToExpireCheckReceiver
import sheridancollege.prog39402.stayfresh.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAppVersion()

        // Load the settings
        val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val isDarkModeEnabled = sharedPreferences.getBoolean("DarkMode", false)

        // Initialize the switch state
        binding.switchDarkMode.isChecked = isDarkModeEnabled

        // Initialize the switch state
        binding.switchDarkMode.isChecked = when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            else -> false
        }

        // Set the listener
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // Save the setting
            sharedPreferences.edit().putBoolean("DarkMode", isChecked).apply()

            if (isChecked) {
                // The user switched the theme to dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                // The user switched the theme to light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Two day to expire notification switch initialization
        binding.switchNotificationTwoDayToExpire!!.isChecked = sharedPreferences.getBoolean("Notification_TwoDayExpire", true)
        binding.switchNotificationTwoDayToExpire!!.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Notification_TwoDayExpire", isChecked).apply()
            if (!isChecked) {
                cancelAlarm(TwoDayToExpireCheckReceiver::class.java, 1)
            }
        }

        // Set the logout button click listener
        binding?.logoutButton?.setOnClickListener {
            // Implement your logout logic here
            // This is a dummy example, adjust according to your authentication system
            FirebaseAuth.getInstance().signOut()

            // Show a toast message
            Toast.makeText(requireContext(), "Successfully logged out", Toast.LENGTH_SHORT).show()

            // Navigate to login screen
            val action = ContentFragmentDirections.actionContentFragmentToStartFragment()
            findNavController().navigate(action)

        }

        binding.rlDarkMode?.setOnClickListener {
            binding.switchDarkMode.isChecked = !binding.switchDarkMode.isChecked
        }

        binding.rlExpiredTwoDay?.setOnClickListener {
            binding.switchNotificationTwoDayToExpire.isChecked = !binding.switchNotificationTwoDayToExpire.isChecked
        }

    }

    private fun setAppVersion() {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val appVersion = "GroceryManager\nVersion ${packageInfo.versionName}"
            binding.textViewAppVersion?.text = appVersion
    }

    private fun cancelAlarm(receiverClass: Class<*>, requestCode: Int) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val intent = Intent(requireContext(), receiverClass)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), requestCode, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
        )
        alarmManager?.cancel(pendingIntent)
    }
}