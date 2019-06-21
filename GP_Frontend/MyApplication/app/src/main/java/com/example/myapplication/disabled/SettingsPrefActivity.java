package com.example.myapplication.disabled;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.view.MenuItem;

import com.example.myapplication.R;

public class SettingsPrefActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // load settings fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }

    @SuppressLint("ValidFragment")
    public class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);

            //preferences click listener
            Preference Pref1 = findPreference(getString(R.string.key_trusted_contacts));
            Pref1.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    finish();
                    Intent intent = new Intent(SettingsPrefActivity.this, ContactsActivity.class);
                    startActivity(intent);
                    return true;
                }
            });

            Preference Pref2 = findPreference(getString(R.string.key_home));
            Pref2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    finish();
                    Intent intent = new Intent(SettingsPrefActivity.this, DisabledHomeActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        finish();
        startActivity(new Intent(this, DisabledHomeActivity.class));
        return true;
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else if (preference instanceof SwitchPreference) {
                if (preference.getKey().equals("key_apply_voice")) {
                    preference.setSummary(stringValue);
                }
            } else if (preference instanceof EditTextPreference) {
                if (preference.getKey().equals("key_trusted_contacts")) {
                    preference.setSummary(stringValue);
                } else if (preference.getKey().equals("key_location")) {
                    preference.setSummary(stringValue);
                } else if (preference.getKey().equals("key_home")) {
                    preference.setSummary(stringValue);
                }
            }
            return true;
        }
    };
}