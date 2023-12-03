package sheridancollege.prog39402.stayfresh

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import sheridancollege.prog39402.stayfresh.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hardcoded credentials for testing
        val testEmail = "stayfreshtestuser0001@gmail.com"  // Replace with your test account email
        val testPassword = "222222"   // Replace with your test account password

        // Sign in with Firebase Authentication
        signInWithFirebase(testEmail, testPassword)

        // Check that the activity is using the layout version with the fragment container
        if (savedInstanceState == null) {

            // Add the fragment to the 'fragment_container' FrameLayout
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, PantryFragment())
                .commit()
        }
    }

    // Function to sign in with Firebase Authentication
    private fun signInWithFirebase(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Log in successful
                    Log.d("FirebaseAuth", "signInWithEmail:success")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("FirebaseAuth", "signInWithEmail:failure", task.exception)
                }
            }
    }
}
