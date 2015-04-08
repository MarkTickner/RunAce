package com.mtickner.runningmotivator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.util.Calendar;
import java.util.Random;

public class RunActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    // Location API. Source: http://stackoverflow.com/a/25173057/1164058
    private GoogleApiClient googleApiClient;
    private Location previousLocation;

    // Preferred distance unit
    private String distanceUnit;

    private Run currentRun;
    private Challenge currentChallenge;

    private int challengeComplete = -1;
    private boolean isVisible = true;
    private boolean isPaused = false;

    private static BufferedWriter bufferedWriter;

    // Called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        // Get preferred distance unit
        distanceUnit = Preferences.GetSettingDistanceUnit(RunActivity.this);

        // Set pace and distance preferred unit
        SetDistanceUnits(distanceUnit);

        // Instantiate current run
        currentRun = new Run(0, -1);

        Intent intent = getIntent();
        String challengeGson = intent.getStringExtra(Challenge.CHALLENGE_GSON);

        if (challengeGson == null) {
            // No challenge
            // Hide target pace
            findViewById(R.id.pace_target_heading).setVisibility(View.GONE);
            findViewById(R.id.pace_target_layout).setVisibility(View.GONE);
        } else {
            // Get challenge
            currentChallenge = new Gson().fromJson(challengeGson, Challenge.class);

            // Output challenge target time
            ((TextView) findViewById(R.id.time)).setText(MiscHelper.FormatSecondsToHoursMinutesSeconds(currentChallenge.GetRun().GetTotalTime()));

            // Get challenge target pace
            double targetPaceInMinutesPerKilometre = MiscHelper.CalculatePaceInMinutesPerKilometre(currentChallenge.GetRun().GetTotalTime(), currentChallenge.GetRun().GetDistanceTotal());

            if (distanceUnit.equals(getString(R.string.run_activity_run_complete_activity_distance_unit_kilometres))) {
                // Challenge target distance
                // Kilometres
                ((TextView) findViewById(R.id.distance)).setText(MiscHelper.FormatDouble(currentChallenge.GetRun().GetDistanceTotal()));

                // Challenge target pace
                // Minutes per kilometre
                ((TextView) findViewById(R.id.pace_target)).setText(MiscHelper.FormatDouble(targetPaceInMinutesPerKilometre));
            } else {
                // Challenge target distance
                // Miles
                ((TextView) findViewById(R.id.distance)).setText(MiscHelper.FormatDouble(MiscHelper.ConvertKilometresToMiles(currentChallenge.GetRun().GetDistanceTotal())));

                // Challenge target pace
                // Minutes per mile
                ((TextView) findViewById(R.id.pace_target)).setText(MiscHelper.FormatDouble(MiscHelper.ConvertMinutesPerKilometreToMinutesPerMile(targetPaceInMinutesPerKilometre)));
            }
        }

        // Change stop button colour to red and pause button to yellow
        Button stopRunningBtn = (Button) findViewById(R.id.stop_running_button);
        stopRunningBtn.getBackground().setColorFilter(getResources().getColor(R.color.runace_red_primary), PorterDuff.Mode.SRC_ATOP);

        Button pauseRunningBtn = (Button) findViewById(R.id.pause_running_button);
        pauseRunningBtn.getBackground().setColorFilter(getResources().getColor(R.color.runace_yellow_primary), PorterDuff.Mode.SRC_ATOP);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            stopRunningBtn.setTextColor(getResources().getColor(R.color.white));
            pauseRunningBtn.setTextColor(getResources().getColor(R.color.white));

            stopRunningBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_stop), null, null);
            pauseRunningBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_pause), null, null);
        }

        // Start location API
        StartRun();
    }

    // Called when the activity starts interacting with the user
    @Override
    protected void onResume() {
        super.onResume();

        // Set activity visibility flag
        isVisible = true;
    }

    // Called when the activity goes into the background
    @Override
    protected void onPause() {
        super.onPause();

        // Set activity visibility flag
        isVisible = false;
    }

    // Called when the activity has detected the user's press of the back key
    @Override
    public void onBackPressed() {
        // Call StopRun method
        StopRun(getWindow().getDecorView().getRootView());
    }

    // Called by GoogleApiClient.ConnectionCallbacks when the connect request has successfully completed
    @Override
    public void onConnected(Bundle connectionHint) {
        // Start timer
        timerHandler.postDelayed(timerRunnable, 0);

        // Log file creation
        bufferedWriter = MiscHelper.OpenLogFile(RunActivity.this);

        // Create the LocationRequest object
        LocationRequest locationRequest = LocationRequest.create();

        // Set the desired interval for active location updates, in milliseconds
        locationRequest.setInterval(3000);

        // Set the priority of the request
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Request location updates using static settings
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

        Toast.makeText(this, getString(R.string.run_activity_start_running_toast_text), Toast.LENGTH_SHORT).show();
    }

    // Called by GoogleApiClient.ConnectionCallbacks when the client is temporarily in a disconnected state
    @Override
    public void onConnectionSuspended(int cause) {
        // GoogleApiClient connection has been suspended
    }

    // Called by GoogleApiClient.OnConnectionFailedListener when there was an error connecting the client to the service
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // GoogleApiClient connection has failed
    }

    // Called by LocationListener when the location has changed
    @Override
    public void onLocationChanged(Location location) {
        // Check run is not paused
        if (!isPaused) {
            // Log current location
            MiscHelper.AppendLogFile(RunActivity.this, bufferedWriter, Calendar.getInstance().getTimeInMillis() + "," + location.getLatitude() + "," + location.getLongitude());

            // Determine new location accuracy in metres
            if (location.getAccuracy() <= 20) {
                if (previousLocation != null) {
                    // Previous location is not null
                    // Calculate distance from previous location to current location
                    float splitDistanceInMetres = previousLocation.distanceTo(location);

                    // Determine if the user has travelled 5 metres
                    if (splitDistanceInMetres > 5) {
                        // Metres to kilometres
                        //distanceTotalInKilometres += (splitDistanceInMetres / 1000);
                        currentRun.SetDistanceTotal(currentRun.GetDistanceTotal() + (splitDistanceInMetres / 1000));

                        // Set previous location to current location for next time
                        previousLocation = location;
                    }
                } else {
                    // Set previous location to initial location
                    previousLocation = location;
                }
            }
        }
    }

    // Called when the user presses the 'Stop' button
    public void StopRun(View view) {
        // Confirm with user
        final AlertDialog alert = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.run_activity_stop_running_dialog_text))
                .setPositiveButton(getString(R.string.run_activity_stop_running_dialog_continue_running_button_text).toUpperCase(), null)
                .setNegativeButton(getString(R.string.run_activity_stop_running_dialog_stop_running_button_text).toUpperCase(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // End the run
                        EndRun();

                        if (currentChallenge == null) {
                            // No challenge
                            Intent intent = new Intent(RunActivity.this, RunCompleteActivity.class);
                            intent.putExtra(Run.RUN_GSON, new Gson().toJson(currentRun));
                            startActivity(intent);
                        } else {
                            // Define the challenge complete activity
                            Intent intent = new Intent(RunActivity.this, RunChallengeCompleteActivity.class);
                            intent.putExtra(Run.RUN_GSON, new Gson().toJson(currentRun));
                            intent.putExtra(Challenge.CHALLENGE_GSON, new Gson().toJson(currentChallenge));
                            intent.putExtra(Challenge.CHALLENGE_COMPLETE, challengeComplete);
                            startActivity(intent);
                        }

                        finish();
                    }
                })
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

    // Called when the user presses the 'Pause' button
    public void PauseRun(View view) {
        Button pauseRunningBtn = (Button) view;

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            pauseRunningBtn.setTextColor(getResources().getColor(R.color.white));
            pauseRunningBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_pause), null, null);
        }

        if (!isPaused) {
            // Pause
            isPaused = true;

            // Set button text, colour and icon
            pauseRunningBtn.setText(getString(R.string.run_activity_resume_running_button_text));
            pauseRunningBtn.getBackground().setColorFilter(getResources().getColor(R.color.runace_green_primary), PorterDuff.Mode.SRC_ATOP);

            if (android.os.Build.VERSION.SDK_INT >= 21) {
                pauseRunningBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_play), null, null);
            } else {
                pauseRunningBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_play_dark), null, null);
            }

            // Stop timer
            timerHandler.removeCallbacks(timerRunnable);
        } else {
            // Resume
            isPaused = false;

            // Set button text, colour and icon
            pauseRunningBtn.setText(getString(R.string.run_activity_pause_running_button_text));
            pauseRunningBtn.getBackground().setColorFilter(getResources().getColor(R.color.runace_yellow_primary), PorterDuff.Mode.SRC_ATOP);

            if (android.os.Build.VERSION.SDK_INT >= 21) {
                pauseRunningBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_pause), null, null);
            } else {
                pauseRunningBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_pause_dark), null, null);
            }

            // Restart timer
            timerHandler.postDelayed(timerRunnable, 1000);
        }
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

    // Method that starts everything required to start a run
    private void StartRun() {
        // Create the location API
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Connect to the location API
        googleApiClient.connect();
    }

    // Method that ends everything required to end a run
    private void EndRun() {
        googleApiClient.disconnect();
        timerHandler.removeCallbacks(timerRunnable);

        MiscHelper.CloseLogFile(RunActivity.this, bufferedWriter);
    }

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        // The runnable will be executed every second. Source: http://stackoverflow.com/a/4598737/1164058
        @Override
        public void run() {
            // Increment seconds
            currentRun.SetTotalTime(currentRun.GetTotalTime() + 1);

            // Output run details
            if (currentChallenge == null) {
                // No challenge
                // Increment time
                ((TextView) findViewById(R.id.time)).setText(MiscHelper.FormatSecondsToHoursMinutesSeconds(currentRun.GetTotalTime()));

                // Increment distance
                if (distanceUnit.equals(getString(R.string.run_activity_run_complete_activity_distance_unit_kilometres))) {
                    // Kilometres
                    ((TextView) findViewById(R.id.distance)).setText(MiscHelper.FormatDouble(currentRun.GetDistanceTotal()));
                } else {
                    // Miles
                    ((TextView) findViewById(R.id.distance)).setText(MiscHelper.FormatDouble(MiscHelper.ConvertKilometresToMiles(currentRun.GetDistanceTotal())));
                }
            } else {
                // In challenge
                // Decrement time
                ((TextView) findViewById(R.id.time)).setText(MiscHelper.FormatSecondsToHoursMinutesSeconds(currentChallenge.GetRun().GetTotalTime() - currentRun.GetTotalTime()));

                // Prevent distance remaining from being negative
                double distanceRemaining = currentChallenge.GetRun().GetDistanceTotal() - currentRun.GetDistanceTotal();
                if (distanceRemaining < 0) distanceRemaining = 0;

                // Decrement distance
                if (distanceUnit.equals(getString(R.string.run_activity_run_complete_activity_distance_unit_kilometres))) {
                    // Kilometres
                    ((TextView) findViewById(R.id.distance)).setText(MiscHelper.FormatDouble(distanceRemaining));
                } else {
                    // Miles
                    ((TextView) findViewById(R.id.distance)).setText(MiscHelper.FormatDouble(MiscHelper.ConvertKilometresToMiles(distanceRemaining)));
                }

                // Determine whether challenge has been completed
                if ((currentChallenge.GetRun().GetTotalTime() - currentRun.GetTotalTime()) == 0) {
                    // Challenge failed
                    challengeComplete = 0;
                } else if (distanceRemaining == 0) {
                    // Challenge succeeded
                    challengeComplete = 1;
                }

                // Check if challenge has been completed
                if (challengeComplete != -1) {
                    // End the run
                    EndRun();

                    // Define the challenge complete activity
                    Intent intent = new Intent(RunActivity.this, RunChallengeCompleteActivity.class);
                    intent.putExtra(Run.RUN_GSON, new Gson().toJson(currentRun));
                    intent.putExtra(Challenge.CHALLENGE_GSON, new Gson().toJson(currentChallenge));
                    intent.putExtra(Challenge.CHALLENGE_COMPLETE, challengeComplete);

                    // Create notification if activity is not currently visible
                    if (!isVisible) {
                        MiscHelper.CreateNotification(RunActivity.this, intent, currentChallenge.GetId() + 1, getString(R.string.run_activity_challenge_finished_notification_text));
                    }

                    // Redirect to challenge complete activity
                    startActivity(intent);
                    finish();
                }
            }

            // Output pace every 5 seconds, after the first 5 seconds
            if (currentRun.GetTotalTime() % 5 == 0 && currentRun.GetTotalTime() != 0) {
                // Calculate pace (average)
                double paceInMinutesPerKilometre = MiscHelper.CalculatePaceInMinutesPerKilometre(currentRun.GetTotalTime(), currentRun.GetDistanceTotal());

                // Zero pace if it is not a number (on first run)
                if (Double.isNaN(paceInMinutesPerKilometre)) paceInMinutesPerKilometre = 0;

                // Output pace in preferred unit
                if (distanceUnit.equals(getString(R.string.run_activity_run_complete_activity_distance_unit_kilometres))) {
                    // Minutes per kilometre
                    ((TextView) findViewById(R.id.pace)).setText(MiscHelper.FormatDouble(paceInMinutesPerKilometre));
                } else {
                    // Minutes per mile
                    ((TextView) findViewById(R.id.pace)).setText(MiscHelper.FormatDouble(MiscHelper.ConvertMinutesPerKilometreToMinutesPerMile(paceInMinutesPerKilometre)));
                }

                if (currentChallenge != null) {
                    // Set pace to red if the current pace is slower than the target pace
                    double paceInMinutesPerKilometreTarget = MiscHelper.CalculatePaceInMinutesPerKilometre(currentChallenge.GetRun().GetTotalTime(), currentChallenge.GetRun().GetDistanceTotal());
                    if (paceInMinutesPerKilometre > paceInMinutesPerKilometreTarget) {
                        ((TextView) findViewById(R.id.pace)).setTextColor(getResources().getColor(R.color.red_warning));
                    }
                }
            }

            // Simulate run if the simulated running setting is enabled
            if (Preferences.GetSettingSimulatedRunningEnabled(RunActivity.this)) {
                Random random = new Random();

                // Simulate between 5-6 min/km
                double randomValue = 0.0028 + (0.0034 - 0.0028) * random.nextDouble();

                currentRun.SetDistanceTotal(currentRun.GetDistanceTotal() + randomValue);
            }

            // Not in challenge mode or challenge is not yet complete
            if (challengeComplete == -1) {
                // Restart runnable in 1 second (1000 milliseconds)
                timerHandler.postDelayed(this, 1000);
            }
        }
    };
}