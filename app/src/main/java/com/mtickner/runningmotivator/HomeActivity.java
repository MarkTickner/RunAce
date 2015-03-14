package com.mtickner.runningmotivator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

public class HomeActivity extends ActionBarActivity {

    private static Button challengesMenuButton;

    // Called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Start the challenges service
        MiscHelper.StartChallengeService(HomeActivity.this);

        // Set profile button text to user's name
        ((Button) findViewById(R.id.profile_button)).setText(Preferences.GetLoggedInUser(this).GetName());

        // Change button colour to red
        Button startRunningBtn = (Button) findViewById(R.id.start_running_button);
        startRunningBtn.getBackground().setColorFilter(getResources().getColor(R.color.runace_red_primary), PorterDuff.Mode.SRC_ATOP);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            startRunningBtn.setTextColor(getResources().getColor(R.color.white));
            startRunningBtn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_play), null, null, null);
        }
    }

    // Called when the activity starts interacting with the user
    @Override
    protected void onResume() {
        super.onResume();

        // Refresh the options menu to update challenge count
        invalidateOptionsMenu();


        //todo below
        try {
            if (Preferences.GetLoggedInUser(this).GetUserType().GetId() == 1) {
                UserType a = Preferences.GetLoggedInUser(this).GetUserType();
            }
        } catch (Exception e) {
            new AlertDialog.Builder(this)
                    .setMessage("Please log back in again.")
                    .setPositiveButton("OK", null)
                    .show();

            Preferences.ClearLoggedInUser(this);

            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);

            finish();
        }
        //todo above
    }

    // Initialise the contents of the Activity's standard options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.menu_home, menu);

        // Get challenges button
        View challengesMenu = menu.findItem(R.id.menu_challenges).getActionView();
        challengesMenuButton = (Button) challengesMenu.findViewById(R.id.challenges_button);

        // Change button colour to white
        challengesMenuButton.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

        // Handler for the challenges button
        challengesMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ChallengeListActivity.class);
                startActivity(intent);
            }
        });

        // Set challenges badge count
        new HttpHelper.GetChallenges((Preferences.GetLoggedInUser(HomeActivity.this)).GetId(), false) {
            // Called after the background task finishes
            @Override
            protected void onPostExecute(String jsonResult) {
                // Check server connection was successful
                if (jsonResult != null) {
                    // Challenges retrieved successfully
                    ArrayList<Challenge> challengeArrayList = JsonHelper.GetChallenges(jsonResult, false);

                    int unreadChallengeCount = 0;

                    for (int i = 0; i < challengeArrayList.size(); i++) {
                        Challenge challenge = challengeArrayList.get(i);

                        if (!challenge.IsRead()) unreadChallengeCount++;
                    }

                    SetChallengeBadgeCount(unreadChallengeCount);
                } else {
                    // Error retrieving challenges
                    // Display error toast to user
                    Toast.makeText(HomeActivity.this, ErrorCodes.GetErrorMessage(HomeActivity.this, 101), Toast.LENGTH_LONG).show();
                }
            }
        }.execute();

        return true;
    }

    // Called whenever an item in the options menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                String versionDetails = "";
                try {
                    // Source: http://envyandroid.com/archives/94/get-android-versioncode-and-versionname
                    PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    versionDetails = " " + packageInfo.versionName + getString(R.string.home_activity_about_dialog_version_text) + packageInfo.versionCode + ")";
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                final AlertDialog alert = new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.app_name) + versionDetails)
                        .setMessage(getString(R.string.home_activity_about_dialog_text))
                        .setPositiveButton(getString(R.string.home_activity_about_dialog_close_button_text).toUpperCase(), null)
                        .create();
                // Set button colour
                alert.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.runace_red_primary));
                    }
                });
                alert.show();

                return true;
            case R.id.menu_privacy_policy:
                Intent privacyPolicyIntent = new Intent(this, PrivacyPolicyActivity.class);
                startActivity(privacyPolicyIntent);

                return true;
            case R.id.menu_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);

                return true;
            case R.id.menu_logout:
                Preferences.ClearLoggedInUser(this);
                Toast.makeText(this, getString(R.string.home_activity_user_logged_out_toast), Toast.LENGTH_SHORT).show();

                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivity(mainIntent);

                finish();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Called when the activity has detected the user's press of the back key
    @Override
    public void onBackPressed() {
        // Return the user to the home screen, preventing back button infinite loop
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // Handler for the 'Profile' button
    public void GoToProfile(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(User.USER_GSON, new Gson().toJson(Preferences.GetLoggedInUser(this)));
        startActivity(intent);
    }

    // Handler for the 'Friends' button
    public void GoToFriendsList(View view) {
        Intent intent = new Intent(this, FriendListActivity.class);
        startActivity(intent);
    }

    // Handler for the 'Run History' button
    public void GoToRunList(View view) {
        Intent intent = new Intent(this, RunListActivity.class);
        startActivity(intent);
    }

    // Handler for the 'Start Run' button
    public void StartRun(View view) {
        Intent intent = new Intent(this, RunActivity.class);
        startActivity(intent);
    }

    // Method that updates the challenge count button text
    private void SetChallengeBadgeCount(int challengeCount) {
        challengesMenuButton.setText(Integer.toString(challengeCount));
    }
}