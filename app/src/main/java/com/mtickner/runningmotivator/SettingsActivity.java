package com.mtickner.runningmotivator;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Add action bar to activity. Source: http://stackoverflow.com/a/27455363/1164058
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.activity_settings, root, false);
        root.addView(bar, 0);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SetupSettings();
    }

    private void SetupSettings() {
        // Add settings from XML
        addPreferencesFromResource(R.xml.activity_settings_list_general);

        if (Preferences.GetLoggedInUser(this).GetUserType().GetId() == 1) {
            // Enable developer options
            addPreferencesFromResource(R.xml.activity_settings_list_developer);
        }

        // Bind the summaries of List preferences
        UpdatePreferenceSummary(findPreference("com.mtickner.runningmotivator.distance_unit"));
    }

    // A preference value change listener that updates the preference's summary to reflect its new value
    private static Preference.OnPreferenceChangeListener PreferenceChangeListenerUpdateSummary = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // Look up list preference display value in the 'entries' list
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to the new value
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

            } else {
                // For other preferences, set the summary to the value's string representation
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    // Updates a preference's summary to its latest value
    private static void UpdatePreferenceSummary(Preference preference) {
        // Set the listener to watch for value changes
        preference.setOnPreferenceChangeListener(PreferenceChangeListenerUpdateSummary);

        // Trigger the listener immediately with the preference's current value
        PreferenceChangeListenerUpdateSummary.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }
}