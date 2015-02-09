package com.mtickner.runningmotivator;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public class RunChallengeCompleteActivity extends ActionBarActivity {

    private ProgressDialog savingRunProgressDialog;
    private Run run;
    private Challenge challenge;
    private int challengeComplete;
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
        challengeComplete = intent.getIntExtra(Challenge.CHALLENGE_COMPLETE, -1);

        // Output run time
        ((TextView) findViewById(R.id.time)).setText(MiscHelper.FormatSecondsToHoursMinutesSeconds(run.GetTotalTime()));

        // Output challenge target time
        ((TextView) findViewById(R.id.time_target)).setText(MiscHelper.FormatSecondsToHoursMinutesSeconds(challenge.GetRun().GetTotalTime()));

        // Get run and challenge target pace
        double paceInMinutesPerKilometre = MiscHelper.CalculatePaceInMinutesPerKilometre(run.GetTotalTime(), run.GetDistanceTotal());
        double targetPaceInMinutesPerKilometre = MiscHelper.CalculatePaceInMinutesPerKilometre(challenge.GetRun().GetTotalTime(), challenge.GetRun().GetDistanceTotal());

        if (distanceUnit.equals(getString(R.string.run_activity_run_complete_activity_distance_unit_kilometres_placeholder))) {
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

        // Display correct message
        if (challengeComplete == 1) {
            challengeSuccess = true;

            // Hide failed layout
            findViewById(R.id.challenge_failed_layout).setVisibility(View.GONE);
        } else {
            challengeSuccess = false;

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
    public void SaveRun(View view) {
        // Display progress dialog to user
        progressHandler.postDelayed(progressRunnable, 500);

        // Save run and mark challenge as complete
        new HttpHelper.SaveRun((Preferences.GetLoggedInUser(RunChallengeCompleteActivity.this)).GetId(), run.GetDistanceTotal(), run.GetTotalTime(), challenge.GetId(), challengeSuccess) {
            // Called after the background task finishes
            @Override
            protected void onPostExecute(String jsonResult) {
                // Dismiss progress dialog
                progressHandler.removeCallbacks(progressRunnable);
                if (savingRunProgressDialog != null) {
                    savingRunProgressDialog.dismiss();
                }

                // Check server connection was successful
                if (JsonHelper.GetRun(jsonResult) != null) {
                    // Run saved successfully
                    // Display success toast to user
                    Toast.makeText(RunChallengeCompleteActivity.this, getString(R.string.run_complete_activity_saving_run_saved_toast), Toast.LENGTH_SHORT).show();

                    // Remove the save run button and show challenge friend button
                    (findViewById(R.id.save_run_button)).setVisibility(View.GONE);
                    (findViewById(R.id.challenge_friend_button)).setVisibility(View.VISIBLE);
                } else {
                    // Error saving run
                    // Display error retry dialog to user
                    new AlertDialog.Builder(RunChallengeCompleteActivity.this)
                            .setMessage(getString(R.string.run_complete_activity_saving_run_error_text))
                            .setPositiveButton(getString(R.string.run_complete_activity_saving_run_error_retry_button_text), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Try saving again
                                    Intent intent = new Intent(RunChallengeCompleteActivity.this, RunChallengeCompleteActivity.class);
                                    intent.putExtra(Run.RUN_GSON, new Gson().toJson(run));
                                    intent.putExtra(Challenge.CHALLENGE_GSON, new Gson().toJson(challenge));
                                    intent.putExtra(Challenge.CHALLENGE_COMPLETE, challengeComplete);

                                    startActivity(intent);

                                    finish();
                                }
                            })
                            .setNegativeButton(getString(R.string.run_complete_activity_saving_run_error_cancel_button_text), null).show();
                }
            }
        }.execute();
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
        if (distanceUnit.equals(getString(R.string.run_activity_run_complete_activity_distance_unit_kilometres_placeholder))) {
            // Kilometres
            // Run pace unit
            ((TextView) findViewById(R.id.pace_distance_unit)).setText(getString(R.string.run_activity_run_complete_activity_pace_distance_unit_kilometres_placeholder));

            // Challenge target pace unit
            ((TextView) findViewById(R.id.pace_target_distance_unit)).setText(getString(R.string.run_activity_run_complete_activity_pace_distance_unit_kilometres_placeholder));
        } else {
            // Miles
            // Run pace unit
            ((TextView) findViewById(R.id.pace_distance_unit)).setText(getString(R.string.run_activity_run_complete_activity_pace_distance_unit_miles_placeholder));

            // Challenge target pace unit
            ((TextView) findViewById(R.id.pace_target_distance_unit)).setText(getString(R.string.run_activity_run_complete_activity_pace_distance_unit_miles_placeholder));
        }

        // Output distance preferred unit
        ((TextView) findViewById(R.id.distance_unit)).setText(distanceUnit);
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