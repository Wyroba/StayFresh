package sheridancollege.prog39402.stayfresh

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
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
            // Create an instance of the RecipeFragment
            val recipeFragment = RecipeFragment()

            // Add the fragment to the 'fragment_container' FrameLayout
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, recipeFragment)
                .commit()
        }
    }

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
