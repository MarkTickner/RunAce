package com.mtickner.runningmotivator;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

public class ChallengeViewActivity extends ActionBarActivity {

    private Challenge challenge;

    // Called when the activity starts interacting with the user
    @Override
    protected void onResume() {
        super.onResume();

        // Display loading layout
        setContentView(R.layout.activity_loading);

        // Get challenge
        Intent intent = getIntent();
        String challengeGson = intent.getStringExtra(Challenge.CHALLENGE_GSON);
        challenge = new Gson().fromJson(challengeGson, Challenge.class);

        // Get challenge
        new HttpHelper.GetChallenge(challenge.GetId(), true) {
            // Called after the background task finishes
            @Override
            protected void onPostExecute(String jsonResult) {
                // Check server connection was successful
                if (jsonResult != null) {
                    // Challenges retrieved successfully
                    challenge = JsonHelper.GetChallenge(jsonResult);

                    // Display main layout
                    setContentView(R.layout.activity_challenge_view);

                    if (challenge.GetDateCompleted() != null) {
                        // Already completed
                        // Set icon
                        ImageView image = (ImageView) findViewById(R.id.challenge_success_icon);
                        Drawable tickDrawable = getResources().getDrawable(R.drawable.ic_tick);
                        tickDrawable.setColorFilter(getResources().getColor(R.color.runace_green_dark), PorterDuff.Mode.MULTIPLY);
                        image.setImageDrawable(tickDrawable);

                        // Append date completed to challenge success message
                        ((TextView) findViewById(R.id.challenge_success_text)).append(MiscHelper.FormatDateForDisplay(challenge.GetDateCompleted()));

                        // Hide failed layout
                        findViewById(R.id.challenge_failed_layout).setVisibility(View.GONE);

                        // Hide accept button and details
                        findViewById(R.id.accept_button).setVisibility(View.GONE);
                        findViewById(R.id.challenge_accept_details).setVisibility(View.GONE);
                    } else {
                        // Not already completed
                        // Set icon
                        ImageView image = (ImageView) findViewById(R.id.challenge_failed_icon);
                        Drawable crossDrawable = getResources().getDrawable(R.drawable.ic_cross);
                        crossDrawable.setColorFilter(getResources().getColor(R.color.runace_red_dark), PorterDuff.Mode.MULTIPLY);
                        image.setImageDrawable(crossDrawable);

                        // Hide success layout
                        findViewById(R.id.challenge_success_layout).setVisibility(View.GONE);

                        // Change button colour to green
                        findViewById(R.id.accept_button).getBackground().setColorFilter(getResources().getColor(R.color.runace_green_primary), PorterDuff.Mode.SRC_ATOP);
                    }

                    // Output challenge user
                    ((TextView) findViewById(R.id.heading)).setText((getString(R.string.challenge_view_activity_subtitle) + challenge.GetRun().GetUser().GetName()).toUpperCase());

                    if (!challenge.GetMessage().equals("")) {
                        // Output message
                        ((TextView) findViewById(R.id.message)).setText("\"" + challenge.GetMessage() + "\"");
                    } else {
                        findViewById(R.id.message).setVisibility(View.GONE);
                    }

                    // Output time
                    ((TextView) findViewById(R.id.time)).setText(MiscHelper.FormatSecondsToHoursMinutesSeconds(challenge.GetRun().GetTotalTime()));

                    // Output distance and pace to preferred unit
                    String distanceUnit = Preferences.GetSettingDistanceUnit(ChallengeViewActivity.this);
                    double paceInMinutesPerKilometre = MiscHelper.CalculatePaceInMinutesPerKilometre(challenge.GetRun().GetTotalTime(), challenge.GetRun().GetDistanceTotal());

                    if (distanceUnit.equals(getString(R.string.run_activity_run_complete_activity_distance_unit_kilometres))) {
                        // Kilometres
                        ((TextView) findViewById(R.id.distance)).setText(MiscHelper.FormatDouble(challenge.GetRun().GetDistanceTotal()));

                        // Minutes per kilometre
                        ((TextView) findViewById(R.id.pace)).setText(MiscHelper.FormatDouble(paceInMinutesPerKilometre));

                        // Pace unit
                        ((TextView) findViewById(R.id.pace_distance_unit)).setText(getString(R.string.run_activity_run_complete_activity_pace_distance_unit_kilometres));
                    } else {
                        // Miles
                        ((TextView) findViewById(R.id.distance)).setText(MiscHelper.FormatDouble(MiscHelper.ConvertKilometresToMiles(challenge.GetRun().GetDistanceTotal())));

                        // Minutes per mile
                        ((TextView) findViewById(R.id.pace)).setText(MiscHelper.FormatDouble(MiscHelper.ConvertMinutesPerKilometreToMinutesPerMile(paceInMinutesPerKilometre)));

                        // Pace unit
                        ((TextView) findViewById(R.id.pace_distance_unit)).setText(getString(R.string.run_activity_run_complete_activity_pace_distance_unit_miles));
                    }

                    // Output distance preferred unit
                    ((TextView) findViewById(R.id.distance_unit)).setText(distanceUnit);
                } else {
                    // Error retrieving challenges
                    // Set error layout
                    setContentView(R.layout.activity_connection_error);
                }
            }
        }.execute();
    }

    // Called when the activity has detected the user's press of the back key
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ChallengeListActivity.class);
        startActivity(intent);
    }

    // Handler for the 'Accept Now' button
    public void AcceptChallenge(View view) {
        // Accept challenge
        Intent intent = new Intent(ChallengeViewActivity.this, RunActivity.class);
        intent.putExtra(Challenge.CHALLENGE_GSON, new Gson().toJson(challenge));
        startActivity(intent);
    }
}