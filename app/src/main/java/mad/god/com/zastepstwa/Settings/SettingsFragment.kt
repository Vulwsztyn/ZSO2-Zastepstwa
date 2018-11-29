package mad.god.com.zastepstwa.Settings

import android.os.Bundle
import android.preference.PreferenceFragment
import mad.god.com.zastepstwa.R

class SettingsFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}