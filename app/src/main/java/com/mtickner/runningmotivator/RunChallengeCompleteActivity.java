package com.mtickner.runningmotivator;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

public class RunChallengeCompleteActivity extends ActionBarActivity {

    private static final String TAG = "RunChallengeComplete";
    private ProgressDialog savingRunProgressDialog;
    private Run run;
    private Challenge challenge;
    private boolean challengeSuccess;

    // Called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_challenge_complete);

        String distanceUnit = Preferences.GetSettingDistanceUnit(this);
        Intent intent = getIntent();
        run = new Gson().fromJson(intent.getStringExtra(Run.RUN_GSON), Run.class);
        challenge = new Gson().fromJson(intent.getStringExtra(Challenge.CHALLENGE_GSON), Challenge.class);
        int challengeComplete = intent.getIntExtra(Challenge.CHALLENGE_COMPLETE, -1);

        // Output run time
        ((TextView) findViewById(R.id.time)).setText(MiscHelper.FormatSecondsToHoursMinutesSeconds(run.GetTotalTime()));

        // Output challenge target time
        ((TextView) findViewById(R.id.time_target)).setText(MiscHelper.FormatSecondsToHoursMinutesSeconds(challenge.GetRun().GetTotalTime()));

        // Get run and challenge target pace
        double paceInMinutesPerKilometre = MiscHelper.CalculatePaceInMinutesPerKilometre(run.GetTotalTime(), run.GetDistanceTotal());
        double targetPaceInMinutesPerKilometre = MiscHelper.CalculatePaceInMinutesPerKilometre(challenge.GetRun().GetTotalTime(), challenge.GetRun().GetDistanceTotal());

        if (distanceUnit.equals(getString(R.string.run_activity_run_complete_activity_distance_unit_kilometres))) {
            // Kilometres
            // Run distance
            ((TextView) findViewById(R.id.distance)).setText(MiscHelper.FormatDouble(run.GetDistanceTotal()));

            // Run minutes per kilometre
            ((TextView) findViewById(R.id.pace)).setText(MiscHelper.FormatDouble(paceInMinutesPerKilometre));

            // Challenge target distance
            ((TextView) findViewById(R.id.distance_target)).setText(MiscHelper.FormatDouble(challenge.GetRun().GetDistanceTotal()));

            // Challenge target minutes per kilometre
            ((TextView) findViewById(R.id.pace_target)).setText(MiscHelper.FormatDouble(targetPaceInMinutesPerKilometre));
        } else {
            // Miles
            // Run distance
            ((TextView) findViewById(R.id.distance)).setText(MiscHelper.FormatDouble(MiscHelper.ConvertKilometresToMiles(run.GetDistanceTotal())));

            // Run minutes per mile
            ((TextView) findViewById(R.id.pace)).setText(MiscHelper.FormatDouble(MiscHelper.ConvertMinutesPerKilometreToMinutesPerMile(paceInMinutesPerKilometre)));

            // Challenge target distance
            ((TextView) findViewById(R.id.distance_target)).setText(MiscHelper.FormatDouble(MiscHelper.ConvertKilometresToMiles(challenge.GetRun().GetDistanceTotal())));

            // Challenge target minutes per mile
            ((TextView) findViewById(R.id.pace_target)).setText(MiscHelper.FormatDouble(MiscHelper.ConvertMinutesPerKilometreToMinutesPerMile(targetPaceInMinutesPerKilometre)));
        }

        // Set pace and distance preferred unit
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

        // Display correct message
        if (challengeComplete == 1) {
            challengeSuccess = true;

            // Set icon
            ImageView image = (ImageView) findViewById(R.id.challenge_success_icon);
            Drawable tickDrawable = getResources().getDrawable(R.drawable.ic_tick);
            tickDrawable.setColorFilter(getResources().getColor(R.color.runace_green_dark), PorterDuff.Mode.MULTIPLY);
            image.setImageDrawable(tickDrawable);

            // Hide failed layout
            findViewById(R.id.challenge_failed_layout).setVisibility(View.GONE);
        } else {
            challengeSuccess = false;

            // Set icon
            ImageView image = (ImageView) findViewById(R.id.challenge_failed_icon);
            Drawable crossDrawable = getResources().getDrawable(R.drawable.ic_cross);
            crossDrawable.setColorFilter(getResources().getColor(R.color.runace_red_dark), PorterDuff.Mode.MULTIPLY);
            image.setImageDrawable(crossDrawable);

            // Hide success layout
            findViewById(R.id.challenge_success_layout).setVisibility(View.GONE);
        }
    }

    // Called whenever an item in the options menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Respond to the action bar up button
                GoBackToChallenge();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Called when the activity has detected the user's press of the back key
    @Override
    public void onBackPressed() {
        GoBackToChallenge();
    }

    // Method that is called when the save run button is pressed
    public void SaveRun(final View view) {
        // Display progress dialog to user
        progressHandler.postDelayed(progressRunnable, 500);

        final String postUri = urlPrefix + "run-save.php";

        // Save run and mark challenge as complete
        RequestQueue queue = Volley.newRequestQueue(RunChallengeCompleteActivity.this);
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
                    Toast.makeText(RunChallengeCompleteActivity.this, getString(R.string.run_complete_activity_saving_run_saved_toast) + points + getString(R.string.run_complete_activity_saving_run_points_toast), Toast.LENGTH_LONG).show();

                    // Remove the save run button and show challenge friend button
                    (findViewById(R.id.save_run_button)).setVisibility(View.GONE);
                    (findViewById(R.id.challenge_friend_button)).setVisibility(View.VISIBLE);
                } else {
                    // Error saving run
                    // Display error retry dialog to user
                    final AlertDialog alert = new AlertDialog.Builder(RunChallengeCompleteActivity.this)
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
                params.put("userId", Integer.toString((Preferences.GetLoggedInUser(RunChallengeCompleteActivity.this)).GetId()));
                params.put("distanceTotal", MiscHelper.FormatDouble(run.GetDistanceTotal()));
                params.put("totalTime", Integer.toString(run.GetTotalTime()));
                params.put("challengeId", Integer.toString(challenge.GetId()));
                params.put("challengeSuccess", Boolean.toString(challengeSuccess));

                return params;
            }
        });
    }

    // Method that is called when the challenge friend button is pressed
    public void ChallengeFriend(View view) {
        Intent intent = new Intent(RunChallengeCompleteActivity.this, ChallengeFriendActivity.class);
        intent.putExtra(Run.RUN_GSON, new Gson().toJson(run));

        startActivity(intent);
    }

    // Method that sets the run and challenge target pace and distance preferred unit
    private void SetDistanceUnits(String distanceUnit) {
        // Set pace preferred unit
        if (distanceUnit.equals(getString(R.string.run_activity_run_complete_activity_distance_unit_kilometres))) {
            // Kilometres
            // Run pace unit
            ((TextView) findViewById(R.id.pace_distance_unit)).setText(getString(R.string.run_activity_run_complete_activity_pace_distance_unit_kilometres));

            // Challenge target pace unit
            ((TextView) findViewById(R.id.pace_target_distance_unit)).setText(getString(R.string.run_activity_run_complete_activity_pace_distance_unit_kilometres));
        } else {
            // Miles
            // Run pace unit
            ((TextView) findViewById(R.id.pace_distance_unit)).setText(getString(R.string.run_activity_run_complete_activity_pace_distance_unit_miles));

            // Challenge target pace unit
            ((TextView) findViewById(R.id.pace_target_distance_unit)).setText(getString(R.string.run_activity_run_complete_activity_pace_distance_unit_miles));
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

    // Method that returns the user to view the challenge
    private void GoBackToChallenge() {
        // Clear notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(challenge.GetId() + 1);

        // Back to challenge
        Intent intent = new Intent(this, ChallengeViewActivity.class);
        intent.putExtra(Challenge.CHALLENGE_GSON, new Gson().toJson(challenge));
        startActivity(intent);
    }

    // Display a progress dialog after a 500ms delay, so it does not show if there is a quick connection. Source: http://stackoverflow.com/a/10947069/1164058
    private final Handler progressHandler = new Handler();
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            savingRunProgressDialog = ProgressDialog.show(RunChallengeCompleteActivity.this, null, getString(R.string.run_complete_activity_saving_run_dialog_text));
        }
    };
}