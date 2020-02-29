import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import edu.miracosta.cs134.flagquiz.R;

public class SettingsActivityFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

}
