package sheridancollege.prog39402.stayfresh.Chi

import android.app.Application
import com.google.firebase.FirebaseApp

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize FirebaseApp when the application starts
        FirebaseApp.initializeApp(this)
    }
}
