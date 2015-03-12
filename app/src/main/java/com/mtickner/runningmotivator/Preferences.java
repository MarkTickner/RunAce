package com.mtickner.runningmotivator;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;

public class Preferences {
    // Source: http://stackoverflow.com/a/11894634/1164058

    private static final String LOGGED_IN_USER = "com.mtickner.runningmotivator.logged_in_user";


    public static SharedPreferences GetSharedPreference(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }


    public static void SetLoggedInUser(Context context, User loggedInUser) {
        Editor editor = GetSharedPreference(context).edit();
        editor.putString(LOGGED_IN_USER, new Gson().toJson(loggedInUser));

        editor.apply();
    }

    public static User GetLoggedInUser(Context context) {
        String userGson = GetSharedPreference(context).getString(LOGGED_IN_USER, null);

        return new Gson().fromJson(userGson, User.class);
    }

    public static void ClearLoggedInUser(Context context) {
        Editor editor = GetSharedPreference(context).edit();
        editor.remove(LOGGED_IN_USER);

        editor.apply();
    }


    public static String GetSettingDistanceUnit(Context context) {
        int distanceUnit = Integer.parseInt(GetSharedPreference(context).getString("com.mtickner.runningmotivator.distance_unit", "0"));

        if (distanceUnit == 1) {
            return context.getString(R.string.run_activity_run_complete_activity_distance_unit_miles);
        } else {
            return context.getString(R.string.run_activity_run_complete_activity_distance_unit_kilometres);
        }
    }

    public static boolean GetSettingNotificationsEnabled(Context context) {
        return GetSharedPreference(context).getBoolean("com.mtickner.runningmotivator.enable_notifications", true);
    }

    public static boolean GetSettingLocationLoggingEnabled(Context context) {
        return GetSharedPreference(context).getBoolean("com.mtickner.runningmotivator.enable_location_logging", false);
    }

    public static boolean GetSettingSimulatedRunningEnabled(Context context) {
        return GetSharedPreference(context).getBoolean("com.mtickner.runningmotivator.enable_simulated_running", false);
    }
}