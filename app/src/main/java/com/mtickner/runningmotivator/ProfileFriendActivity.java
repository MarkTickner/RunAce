package com.mtickner.runningmotivator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

public class ProfileFriendActivity extends ActionBarActivity {

    private ArrayList<Badge> badgeArrayList = new ArrayList<>();

    // Called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display loading layout
        setContentView(R.layout.activity_loading);

        // Get friend
        Intent intent = getIntent();
        Friend friend = new Gson().fromJson(intent.getStringExtra(Friend.FRIEND_GSON), Friend.class);

        // Create and output table of user's badges
        CreateBadgesTable(friend);
    }

    // Initialise the contents of the Activity's standard options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.menu_profile_friend, menu);

        return true;
    }

    // Called whenever an item in the options menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_unfriend:
                //todo unfriend
                Toast.makeText(this, "Are you sure you want to unfriend [name], etc", Toast.LENGTH_SHORT).show();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Method that creates a table of badges
    private void CreateBadgesTable(final Friend friend) {
        // Get badges
        //new HttpHelper.GetBadges(friend.GetUser().GetId()) { todo id should be of friend not me
        new HttpHelper.GetBadges((Preferences.GetLoggedInUser(ProfileFriendActivity.this)).GetId()) {
            // Called after the background task finishes
            @Override
            protected void onPostExecute(String jsonResult) {
                // Check server connection was successful
                if (jsonResult != null) {
                    // Badges retrieved successfully
                    badgeArrayList = JsonHelper.GetBadges(jsonResult);

                    // Set main layout
                    setContentView(R.layout.activity_profile_friend);

                    // Display friend details
                    ((TextView) findViewById(R.id.name)).setText(friend.GetUser().GetName().toUpperCase());
                    ((TextView) findViewById(R.id.date_friends_since)).setText("Friends since: " + MiscHelper.FormatDateForDisplay(friend.GetStatusDate()));

                    // Badges container linear layout parameters (for its child elements)
                    LinearLayout.LayoutParams badgesContainerLayoutParameters = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                    // Check if friend has any badges
                    if (badgeArrayList.size() > 0) {
                        // User has badges
                        // Badges table layout
                        TableLayout badgesTableLayout = new TableLayout(ProfileFriendActivity.this);
                        badgesTableLayout.setStretchAllColumns(true);

                        // Badges table layout parameters (for its child elements)
                        TableLayout.LayoutParams badgesTableLayoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                        badgesTableLayoutParams.setMargins(0, 0, 0, MiscHelper.ConvertDpToPx(ProfileFriendActivity.this, 15));

                        // Badges table row layout parameters (for its child elements)
                        TableRow.LayoutParams badgesTableRowLayoutParams = new TableRow.LayoutParams(MiscHelper.ConvertDpToPx(ProfileFriendActivity.this, 75), MiscHelper.ConvertDpToPx(ProfileFriendActivity.this, 75));
                        badgesTableRowLayoutParams.setMargins(MiscHelper.ConvertDpToPx(ProfileFriendActivity.this, 15), 0, MiscHelper.ConvertDpToPx(ProfileFriendActivity.this, 15), 0);
                        badgesTableRowLayoutParams.gravity = Gravity.CENTER_VERTICAL;

                        // Badge layout parameters (for the text view child elements)
                        LinearLayout.LayoutParams badgeLayoutTextViewLayoutParams = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        badgeLayoutTextViewLayoutParams.setMargins(0, MiscHelper.ConvertDpToPx(ProfileFriendActivity.this, -5), 0, 0);

                        // Loop badges array list, with 3 badges per row
                        for (int rowNo = 0; rowNo < badgeArrayList.size(); rowNo = rowNo + 3) {
                            // Badges table row
                            TableRow badgesTableRow = new TableRow(ProfileFriendActivity.this);

                            // Loop badges array list
                            for (int columnNo = 0; (columnNo < 3) && ((rowNo + columnNo + 1) <= badgeArrayList.size()); columnNo++) {
                                // Create column
                                // Badge layout
                                LinearLayout badgeLayout = new LinearLayout(ProfileFriendActivity.this);
                                badgeLayout.setBackground(getResources().getDrawable(R.drawable.bg_border_box));
                                badgeLayout.setGravity(Gravity.CENTER);
                                badgeLayout.setOrientation(LinearLayout.VERTICAL);

                                // Get current badge
                                final Badge currentBadge = badgeArrayList.get(rowNo + columnNo);

                                // Output badge level in a text view
                                TextView levelTextView = new TextView(ProfileFriendActivity.this);
                                levelTextView.setText(Integer.toString(currentBadge.GetLevel()));
                                levelTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
                                levelTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                                badgeLayout.addView(levelTextView, badgeLayoutTextViewLayoutParams);

                                // Output badge type in a text view
                                TextView typeTextView = new TextView(ProfileFriendActivity.this);
                                if (currentBadge.GetLevel() == 1) {
                                    // Single badge type
                                    typeTextView.setText(currentBadge.GetType().toString());
                                } else {
                                    // Plural badge type
                                    typeTextView.setText(currentBadge.GetType() + "S");
                                }
                                typeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                                typeTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                                badgeLayout.addView(typeTextView, badgeLayoutTextViewLayoutParams);

                                // Set badge click handler
                                badgeLayout.setOnClickListener(new View.OnClickListener() {
                                    // Handler for when the badge is pressed
                                    @Override
                                    public void onClick(View view) {
                                        // Display toast to user
                                        Toast.makeText(ProfileFriendActivity.this, "Awarded on " + MiscHelper.FormatDateForDisplay(currentBadge.GetDateAwarded()), Toast.LENGTH_SHORT).show();
                                    }
                                });

                                // Add column to row
                                badgesTableRow.addView(badgeLayout, badgesTableRowLayoutParams);
                            }

                            // Add row to table
                            badgesTableLayout.addView(badgesTableRow, badgesTableLayoutParams);
                        }

                        // Add table to badges container linear layout
                        ((LinearLayout) findViewById(R.id.badges_container)).addView(badgesTableLayout, badgesContainerLayoutParameters);
                    } else {
                        // User has no badges
                        // Create text view to display message
                        TextView noBadgesTextView = new TextView(ProfileFriendActivity.this);
                        noBadgesTextView.setText(getString(R.string.profile_friend_activity_badges_none_text));//todo different text for own profile and friend profile
                        noBadgesTextView.setGravity(Gravity.CENTER_HORIZONTAL);

                        // Set margin of text view
                        badgesContainerLayoutParameters.setMargins(0, 0, 0, MiscHelper.ConvertDpToPx(ProfileFriendActivity.this, 15));

                        // Add text view to badges container linear layout
                        ((LinearLayout) findViewById(R.id.badges_container)).addView(noBadgesTextView, badgesContainerLayoutParameters);
                    }
                } else {
                    // Error retrieving badges
                    // Set error layout
                    setContentView(R.layout.activity_connection_error);
                }
            }
        }.execute();
    }
}