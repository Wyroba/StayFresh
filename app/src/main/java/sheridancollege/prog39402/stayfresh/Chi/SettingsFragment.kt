package sheridancollege.prog39402.stayfresh.Chi

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import sheridancollege.prog39402.stayfresh.MainActivity
import sheridancollege.prog39402.stayfresh.Peter.ContentFragmentDirections
import sheridancollege.prog39402.stayfresh.Peter.TwoDayToExpireCheckReceiver
import sheridancollege.prog39402.stayfresh.R
import sheridancollege.prog39402.stayfresh.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment() {

    // Declare sharedPreferences as a class-level variable
    private lateinit var sharedPreferences: SharedPreferences

    private var _binding: FragmentSettingsBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        // Initialize sharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAppVersion()

        // Load the settings

        val isDarkModeEnabled = sharedPreferences.getBoolean("DarkMode", false)

        // Two day to expire notification switch initialization
        val notificationsEnabled = areNotificationsEnabled()
        val twoDayToExpireEnabled = sharedPreferences.getBoolean("Notification_TwoDayExpire", true)
        binding.switchNotificationTwoDayToExpire.isChecked = twoDayToExpireEnabled && notificationsEnabled


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

            // Send notification about the dark mode change
            sendDarkModeChangeNotification(isChecked)
        }

        // Two day to expire notification switch initialization
        binding.switchNotificationTwoDayToExpire.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !areNotificationsEnabled()) {
                showNotificationPermissionDialog()
                binding.switchNotificationTwoDayToExpire.isChecked = false
            } else {
                sharedPreferences.edit().putBoolean("Notification_TwoDayExpire", isChecked).apply()
                if (!isChecked) {
                    cancelAlarm(TwoDayToExpireCheckReceiver::class.java, 1)
                }
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
            val appVersion = "Stay Fresh\nVersion ${packageInfo.versionName}"
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

    // Show dialog prompting user to enable notifications
    private fun showNotificationPermissionDialog() {
        AlertDialog.Builder(requireContext())
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
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Method to check if notifications are enabled
    private fun areNotificationsEnabled(): Boolean {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.areNotificationsEnabled()
    }

    override fun onResume() {
        super.onResume()

        // Update the switch state for TwoDayToExpire notifications when the fragment resumes
        val notificationsEnabled = areNotificationsEnabled()
        val twoDayToExpireEnabled = sharedPreferences.getBoolean("Notification_TwoDayExpire", true)

        // Update the switch only if notifications are enabled
        if (notificationsEnabled) {
            binding.switchNotificationTwoDayToExpire.isChecked = twoDayToExpireEnabled
        }
    }

    private fun sendDarkModeChangeNotification(isDarkModeEnabled: Boolean) {
        // Define notification channel ID and notification ID.
        val channelId = "dark_mode_change_channel"
        val notificationId = channelId.hashCode()

        // Construct the intent that will be triggered when the notification is clicked.
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            requireContext(), 0, intent, pendingIntentFlags
        )
        // Construct the notification with required attributes.
        val notificationBuilder = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.hotpot)
            .setContentTitle("Dark Mode Changed")
            .setContentText("Dark Mode is now ${if (isDarkModeEnabled) "enabled" else "disabled"}.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Initialize the notification manager.
        val notificationManager = NotificationManagerCompat.from(requireContext())

        // For Android Oreo and later, create a notification channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Dark Mode Change", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Finally, send the constructed notification.
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}