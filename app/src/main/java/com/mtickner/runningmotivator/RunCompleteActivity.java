package com.mtickner.runningmotivator;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.mtickner.runningmotivator.HttpHelper.urlPrefix;

public class RunCompleteActivity extends ActionBarActivity {

    private static final String TAG = "RunCompleteActivity";
    private ProgressDialog savingRunProgressDialog;
    private Run run;

    // Called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_complete);

        String distanceUnit = Preferences.GetSettingDistanceUnit(this);
        Intent intent = getIntent();
        run = new Gson().fromJson(intent.getStringExtra(Run.RUN_GSON), Run.class);

        // Output time
        ((TextView) findViewById(R.id.time)).setText(MiscHelper.FormatSecondsToHoursMinutesSeconds(run.GetTotalTime()));

        // Output distance and pace to preferred unit
        double paceInMinutesPerKilometre = MiscHelper.CalculatePaceInMinutesPerKilometre(run.GetTotalTime(), run.GetDistanceTotal());

        if (distanceUnit.equals(getString(R.string.run_activity_run_complete_activity_distance_unit_kilometres))) {
            // Kilometres
            ((TextView) findViewById(R.id.distance)).setText(MiscHelper.FormatDouble(run.GetDistanceTotal()));

            // Minutes per kilometre
            ((TextView) findViewById(R.id.pace)).setText(MiscHelper.FormatDouble(paceInMinutesPerKilometre));
        } else {
            // Miles
            ((TextView) findViewById(R.id.distance)).setText(MiscHelper.FormatDouble(MiscHelper.ConvertKilometresToMiles(run.GetDistanceTotal())));

            // Minutes per mile
            ((TextView) findViewById(R.id.pace)).setText(MiscHelper.FormatDouble(MiscHelper.ConvertMinutesPerKilometreToMinutesPerMile(paceInMinutesPerKilometre)));
        }

        SetDistanceUnits(distanceUnit);

        // Change save run and challenge friend buttons colour to blue
        Button saveRunBtn = (Button) findViewById(R.id.save_run_button);
        Button challengeFriendBtn = (Button) findViewById(R.id.challenge_friend_button);

        saveRunBtn.getBackground().setColorFilter(getResources().getColor(R.color.runace_blue_primary), PorterDuff.Mode.SRC_ATOP);
        challengeFriendBtn.getBackground().setColorFilter(getResources().getColor(R.color.runace_blue_primary), PorterDuff.Mode.SRC_ATOP);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            saveRunBtn.setTextColor(getResources().getColor(R.color.white));
            challengeFriendBtn.setTextColor(getResources().getColor(R.color.white));
        }

        // Minimum distance and pace restrictions
        TextView runRestrictionErrorText = (TextView) findViewById(R.id.run_restriction_error_text);

        if (run.GetDistanceTotal() < 0.48) {
            // Minimum distance not reached. Slightly less than 0.5 km to allow for 0.3 miles
            // Hide the save run button
            saveRunBtn.setVisibility(View.GONE);

            // Set error message
            runRestrictionErrorText.setVisibility(View.VISIBLE);
            runRestrictionErrorText.setText(getString(R.string.a));
        } else if (paceInMinutesPerKilometre < 2) {
            // Pace was quicker than the minimum threshold. Set at 2 min/km as that is current world record
            // Hide the save run button
            saveRunBtn.setVisibility(View.GONE);

            // Set error message
            runRestrictionErrorText.setVisibility(View.VISIBLE);
            runRestrictionErrorText.setText(getString(R.string.b));
        }
    }

    // Called whenever an item in the options menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Respond to the action bar up button
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Called when the activity has detected the user's press of the back key
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    // Method that is called when the save run button is pressed
    public void SaveRun(final View view) {
        // Display progress dialog to user
        progressHandler.postDelayed(progressRunnable, 500);

        final String postUri = urlPrefix + "run-save.php";

        // Save run
        RequestQueue queue = Volley.newRequestQueue(RunCompleteActivity.this);
        queue.add(new StringRequest(Method.POST, postUri, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);

                // Dismiss progress dialog
                progressHandler.removeCallbacks(progressRunnable);
                if (savingRunProgressDialog != null) {
                    savingRunProgressDialog.dismiss();
                }

                // Check server connection was successful
                if (JsonHelper.GetRun(response) != null) {
                    // Run saved successfully
                    // Get newly awarded badges
                    ArrayList<Badge> awardedBadgeArrayList = JsonHelper.GetNewlyAwardedBadges(response);
                    if (awardedBadgeArrayList.size() > 0) {
                        // Loop over every newly awarded badge. Loop is reversed as alerts are stacked from bottom to top
                        for (int i = awardedBadgeArrayList.size() - 1; i >= 0; i--) {
                            // Display dialog for each newly awarded badge
                            AwardBadge(awardedBadgeArrayList.get(i));
                        }
                    }

                    // Display success toast to user
                    int points = JsonHelper.GetPoints(response);
                    Toast.makeText(RunCompleteActivity.this, getString(R.string.run_complete_activity_saving_run_saved_toast) + points + getString(R.string.run_complete_activity_saving_run_points_toast), Toast.LENGTH_LONG).show();

                    // Remove the save run button and show challenge friend button
                    (findViewById(R.id.save_run_button)).setVisibility(View.GONE);
                    (findViewById(R.id.challenge_friend_button)).setVisibility(View.VISIBLE);
                } else {
                    // Error saving run
                    // Display error retry dialog to user
                    final AlertDialog alert = new AlertDialog.Builder(RunCompleteActivity.this)
                            .setMessage(getString(R.string.run_complete_activity_saving_run_error_text))
                            .setPositiveButton(getString(R.string.run_complete_activity_saving_run_error_retry_button_text).toUpperCase(), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Try saving again
                                    SaveRun(view);
                                }
                            })
                            .setNegativeButton(getString(R.string.run_complete_activity_saving_run_error_cancel_button_text).toUpperCase(), null)
                            .create();
                    // Set button colour
                    alert.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.runace_red_primary));
                            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.runace_grey_primary));
                        }
                    });
                    alert.show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getLocalizedMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("requestFromApplication", "true");
                params.put("userId", Integer.toString((Preferences.GetLoggedInUser(RunCompleteActivity.this)).GetId()));
                params.put("distanceTotal", MiscHelper.FormatDouble(run.GetDistanceTotal()));
                params.put("totalTime", Integer.toString(run.GetTotalTime()));
                params.put("challengeId", Integer.toString(0));
                params.put("challengeSuccess", Boolean.toString(false));

                return params;
            }
        });
    }

    // Method that is called when the challenge friend button is pressed
    public void ChallengeFriend(View view) {
        Intent intent = new Intent(RunCompleteActivity.this, ChallengeFriendActivity.class);
        intent.putExtra(Run.RUN_GSON, new Gson().toJson(run));
        startActivity(intent);
    }

    // Method that sets the pace and distance preferred unit
    private void SetDistanceUnits(String distanceUnit) {
        // Set pace preferred unit
        if (distanceUnit.equals(getString(R.string.run_activity_run_complete_activity_distance_unit_kilometres))) {
            // Kilometres
            // Pace unit
            ((TextView) findViewById(R.id.pace_distance_unit)).setText(getString(R.string.run_activity_run_complete_activity_pace_distance_unit_kilometres));
        } else {
            // Miles
            // Pace unit
            ((TextView) findViewById(R.id.pace_distance_unit)).setText(getString(R.string.run_activity_run_complete_activity_pace_distance_unit_miles));
        }

        // Output distance preferred unit
        ((TextView) findViewById(R.id.distance_unit)).setText(distanceUnit);
    }

    // Method that displays a dialog for the specified badge
    public void AwardBadge(Badge badge) {
        // Get award badge layout
        View dialogAwardBadgeLayout = (LayoutInflater.from(this)).inflate(R.layout.dialog_badge_award, null);

        // Set badge image
        LinearLayout badgeLayout = (LinearLayout) dialogAwardBadgeLayout.findViewById(R.id.badgeLayout);
        switch (badge.GetType()) {
            case CHALLENGE:
                badgeLayout.setBackground(getResources().getDrawable(R.drawable.bg_badge_green));
                break;
            case RUN:
                badgeLayout.setBackground(getResources().getDrawable(R.drawable.bg_badge_purple));
                break;
        }

        // Output badge level
        TextView levelTextView = (TextView) dialogAwardBadgeLayout.findViewById(R.id.levelTextView);
        levelTextView.setText(Integer.toString(badge.GetLevel()));

        // Output badge type
        TextView typeTextView = (TextView) dialogAwardBadgeLayout.findViewById(R.id.typeTextView);
        if (badge.GetLevel() == 1) {
            // Single badge type
            typeTextView.setText(badge.GetType().toString());
        } else {
            // Plural badge type
            typeTextView.setText(badge.GetType() + "S");
        }

        // Display dialog box for newly awarded badge
        final AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.run_complete_activity_new_badge_heading))
                .setMessage(getString(R.string.run_complete_activity_new_badge_text))
                .setView(dialogAwardBadgeLayout)
                .setPositiveButton(getString(R.string.run_complete_activity_new_badge_close_button_text).toUpperCase(), null)
                .create();
        // Set button colour
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.runace_red_primary));
            }
        });
        alert.show();
    }

    // Display a progress dialog after a 500ms delay, so it does not show if there is a quick connection. Source: http://stackoverflow.com/a/10947069/1164058
    private final Handler progressHandler = new Handler();
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            savingRunProgressDialog = ProgressDialog.show(RunCompleteActivity.this, null, getString(R.string.run_complete_activity_saving_run_dialog_text));
        }
    };
}