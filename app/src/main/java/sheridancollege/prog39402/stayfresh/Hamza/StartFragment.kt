package sheridancollege.prog39402.stayfresh.Hamza

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import sheridancollege.prog39402.stayfresh.R
import sheridancollege.prog39402.stayfresh.databinding.FragmentStartBinding

class StartFragment : Fragment(){
    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentAuth = FirebaseAuth.getInstance()

        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        val user = null
        if (user != null) {
            navigateToDestination()
        } else {
            binding.buttonLogin.setOnClickListener {
                val email = binding.editTextEmail.text.toString()
                val password = binding.editTextPassword.text.toString()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                addUserToFirestore(user)
                                updateUI(user)

                                user?.let {
                                    saveUserIdToSharedPreferences(it.uid)
                                }

                            } else {
                                Log.w(ContentValues.TAG, "signInWithEmail:failure", task.exception)
                                updateUI(null)
                            }
                        }
                } else {
                    Toast.makeText(context, "Please enter email and password.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.buttonRegister.setOnClickListener{

        }
    }

    private fun saveUserIdToSharedPreferences(uid: String) {
        val sharedPref = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("currentUserId", uid)
            apply()
        }
    }

    private fun addUserToFirestore(user: FirebaseUser?) {
        user?.let {
            val userInfo = hashMapOf(
                "uid" to it.uid
            )
            val userDocument = usersCollection.document(it.uid)

            userDocument.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (!document.exists()) {
                        // The document does not exist, we can safely write
                        userDocument.set(userInfo)
                            .addOnSuccessListener {
                                Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!")
                                // Call updateUI(user) here after the Firestore write operation has succeeded
                                updateUI(user)
                            }
                            .addOnFailureListener { e ->
                                Log.w(ContentValues.TAG, "Error writing document", e)
                            }
                    } else {
                        Log.d(ContentValues.TAG, "Document already exists!")
                        // Call updateUI(user) here as well, since the user document already exists
                        updateUI(user)
                    }
                } else {
                    Log.w(ContentValues.TAG, "Error checking document", task.exception)
                }
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            navigateToDestination()
        } else {
            Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToDestination() {
        val userId = auth.currentUser?.uid ?: return
        val userDocument = usersCollection.document(userId)

        // Fetch user details from Firestore and navigate to the appropriate destination
        userDocument.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (isAdded) {
                        Toast.makeText(context, "Authentication success.", Toast.LENGTH_SHORT).show()
                        if (findNavController().currentDestination?.id != R.id.contentFragment) {
                           findNavController().navigate(R.id.action_startFragment_to_contentFragment)
                       }
                    }
                } else {
                    Log.e(ContentValues.TAG, "Error getting user document", task.exception)
                    // Handle the exception here.
                }
            }
    }
}