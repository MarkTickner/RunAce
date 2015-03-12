package com.mtickner.runningmotivator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

public class ProfileActivity extends ActionBarActivity {

    private ArrayList<Badge> badgeArrayList = new ArrayList<>();

    // Called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display loading layout
        setContentView(R.layout.activity_loading);

        // Get user
        Intent intent = getIntent();
        User user = new Gson().fromJson(intent.getStringExtra(User.USER_GSON), User.class);

        // Create and output table of user's badges
        CreateBadgesTable(user);
    }

    // Method that creates a table of badges
    private void CreateBadgesTable(final User user) {
        // Get badges
        new HttpHelper.GetBadges((Preferences.GetLoggedInUser(ProfileActivity.this)).GetId()) {
            // Called after the background task finishes
            @Override
            protected void onPostExecute(String jsonResult) {
                // Check server connection was successful
                if (jsonResult != null) {
                    // Badges retrieved successfully
                    badgeArrayList = JsonHelper.GetBadges(jsonResult);

                    // Set main layout
                    setContentView(R.layout.activity_profile);

                    // Badges container linear layout parameters (for its child elements)
                    LinearLayout.LayoutParams badgesContainerLayoutParameters = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                    // Check if friend has any badges
                    if (badgeArrayList.size() > 0) {
                        // User has badges
                        // Badges table layout
                        TableLayout badgesTableLayout = new TableLayout(ProfileActivity.this);
                        badgesTableLayout.setStretchAllColumns(true);

                        // Badges table layout parameters (for its child elements)
                        TableLayout.LayoutParams badgesTableLayoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                        badgesTableLayoutParams.setMargins(0, 0, 0, MiscHelper.ConvertDpToPx(ProfileActivity.this, 15));

                        // Badges table row layout parameters (for its child elements)
                        TableRow.LayoutParams badgesTableRowLayoutParams = new TableRow.LayoutParams(MiscHelper.ConvertDpToPx(ProfileActivity.this, 90), MiscHelper.ConvertDpToPx(ProfileActivity.this, 90));
                        badgesTableRowLayoutParams.gravity = Gravity.CENTER_VERTICAL;

                        // Badge layout parameters (for the text view child elements)
                        LinearLayout.LayoutParams badgeLayoutTextViewLayoutParams = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        badgeLayoutTextViewLayoutParams.setMargins(0, MiscHelper.ConvertDpToPx(ProfileActivity.this, -5), 0, 0);

                        // Loop badges array list, with 3 badges per row
                        for (int rowNo = 0; rowNo < badgeArrayList.size(); rowNo = rowNo + 3) {
                            // Badges table row
                            TableRow badgesTableRow = new TableRow(ProfileActivity.this);

                            // Loop badges array list
                            for (int columnNo = 0; (columnNo < 3) && ((rowNo + columnNo + 1) <= badgeArrayList.size()); columnNo++) {
                                // Create column
                                // Get current badge
                                final Badge currentBadge = badgeArrayList.get(rowNo + columnNo);

                                // Get award badge layout
                                View dialogAwardBadgeLayout = (LayoutInflater.from(ProfileActivity.this)).inflate(R.layout.dialog_badge_award_small, null);
                                LinearLayout badgeLayoutContainer = (LinearLayout) dialogAwardBadgeLayout.findViewById(R.id.badgeLayoutContainer);

                                // Set badge image
                                LinearLayout badgeLayout = (LinearLayout) badgeLayoutContainer.findViewById(R.id.badgeLayout);
                                switch (currentBadge.GetType()) {
                                    case CHALLENGE:
                                        badgeLayout.setBackground(getResources().getDrawable(R.drawable.bg_badge_blue));
                                        break;
                                    case RUN:
                                        badgeLayout.setBackground(getResources().getDrawable(R.drawable.bg_badge_red));
                                        break;
                                }

                                // Output badge level
                                TextView levelTextView = (TextView) dialogAwardBadgeLayout.findViewById(R.id.levelTextView);
                                levelTextView.setText(Integer.toString(currentBadge.GetLevel()));

                                // Output badge type
                                TextView typeTextView = (TextView) dialogAwardBadgeLayout.findViewById(R.id.typeTextView);
                                if (currentBadge.GetLevel() == 1) {
                                    // Single badge type
                                    typeTextView.setText(currentBadge.GetType().toString());
                                } else {
                                    // Plural badge type
                                    typeTextView.setText(currentBadge.GetType() + "S");
                                }

                                // Set badge click handler
                                badgeLayout.setOnClickListener(new View.OnClickListener() {
                                    // Handler for when the badge is pressed
                                    @Override
                                    public void onClick(View view) {
                                        // Display toast to user
                                        Toast.makeText(ProfileActivity.this, getString(R.string.profile_activity_badge_awarded_toast_text) + MiscHelper.FormatDateForDisplay(currentBadge.GetDateAwarded()), Toast.LENGTH_SHORT).show();
                                    }
                                });

                                // Add column to row
                                badgesTableRow.addView(badgeLayoutContainer, badgesTableRowLayoutParams);
                            }

                            // Add row to table
                            badgesTableLayout.addView(badgesTableRow, badgesTableLayoutParams);
                        }

                        // Add table to badges container linear layout
                        ((LinearLayout) findViewById(R.id.badges_container)).addView(badgesTableLayout, badgesContainerLayoutParameters);
                    } else {
                        // User has no badges
                        // Create text view to display message
                        TextView noBadgesTextView = new TextView(ProfileActivity.this);
                        noBadgesTextView.setText(getString(R.string.profile_activity_badges_none_text));
                        noBadgesTextView.setGravity(Gravity.CENTER_HORIZONTAL);

                        // Set margin of text view
                        badgesContainerLayoutParameters.setMargins(0, 0, 0, MiscHelper.ConvertDpToPx(ProfileActivity.this, 15));

                        // Add text view to badges container linear layout
                        ((LinearLayout) findViewById(R.id.badges_container)).addView(noBadgesTextView, badgesContainerLayoutParameters);
                    }

                    // Output statistics
                    OutputStats(JsonHelper.GetStatistics(jsonResult));

                    // Output details
                    OutputName(user);
                } else {
                    // Error retrieving badges
                    // Set error layout
                    setContentView(R.layout.activity_connection_error);
                }
            }
        }.execute();
    }

    // Method that displays friend name details
    private void OutputName(User user) {
        ((TextView) findViewById(R.id.name)).setText(user.GetName().toUpperCase());
        ((TextView) findViewById(R.id.date_friends_since)).setText(getString(R.string.profile_activity_user_since_text) + MiscHelper.FormatDateForDisplay(user.GetDateRegistered()));
    }

    // Method that displays the users' statistics
    private void OutputStats(double[] statisticsArray) {
        ((TextView) findViewById(R.id.score)).setText(Integer.toString((int) statisticsArray[0]));
        ((TextView) findViewById(R.id.stats_total_runs)).setText(Integer.toString((int) statisticsArray[1]));
        ((TextView) findViewById(R.id.stats_total_challenges)).setText(Integer.toString((int) statisticsArray[2]));
        ((TextView) findViewById(R.id.stats_total_time)).setText(MiscHelper.FormatSecondsToHoursMinutesSeconds((int) statisticsArray[4]));

        if (Preferences.GetSettingDistanceUnit(ProfileActivity.this).equals(getString(R.string.run_activity_run_complete_activity_distance_unit_kilometres))) {
            // Kilometres
            ((TextView) findViewById(R.id.stats_total_distance)).setText(statisticsArray[3] + getString(R.string.run_activity_run_complete_activity_distance_unit_kilometres));
        } else {
            // Miles
            ((TextView) findViewById(R.id.stats_total_distance)).setText(statisticsArray[3] + getString(R.string.run_activity_run_complete_activity_distance_unit_miles));
        }
    }
}