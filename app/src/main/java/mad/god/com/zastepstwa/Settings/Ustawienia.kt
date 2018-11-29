package mad.god.com.zastepstwa.Settings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class Ustawienia : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Display the fragment as the main content.
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }
}
