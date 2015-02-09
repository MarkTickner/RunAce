package com.mtickner.runningmotivator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

//todo
public class ProfileFriendActivity extends ActionBarActivity {

    private ArrayList<Badge> badgeArrayList = new ArrayList<>();


    // Called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display loading layout
        setContentView(R.layout.activity_loading);

        //todo
        CreateBadgesTable();
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
                //todo
                Toast.makeText(this, "Are you sure you want to unfriend [name], etc", Toast.LENGTH_SHORT).show();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //todo Method that creates a table of badges
    private void CreateBadgesTable() {
        // Get badges todo id should be of friend not me
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

                    // Get friend
                    Intent intent = getIntent();
                    Friend friend = new Gson().fromJson(intent.getStringExtra(Friend.FRIEND_GSON), Friend.class);

                    // Display friend details
                    ((TextView) findViewById(R.id.name)).setText(friend.GetUser().GetName().toUpperCase());
                    ((TextView) findViewById(R.id.date_friends_since)).setText("Friends since: " + MiscHelper.FormatDateForDisplay(friend.GetStatusDate()));

                    //
                    TableLayout tableLayout = new TableLayout(ProfileFriendActivity.this);
                    tableLayout.setStretchAllColumns(true);

                    LinearLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                    TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                    tableRowParams.setMargins(0, 0, 0, MiscHelper.ConvertDpToPx(ProfileFriendActivity.this, 15));

                    TableRow.LayoutParams badgeLayoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
                    badgeLayoutParams.setMargins(MiscHelper.ConvertDpToPx(ProfileFriendActivity.this, 15), 0, MiscHelper.ConvertDpToPx(ProfileFriendActivity.this, 15), 0);
                    badgeLayoutParams.gravity = Gravity.CENTER_VERTICAL;

                    LinearLayout.LayoutParams levelTextViewLayoutParams = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    levelTextViewLayoutParams.setMargins(0, MiscHelper.ConvertDpToPx(ProfileFriendActivity.this, -5), 0, 0);

                    LinearLayout.LayoutParams typeTextViewLayoutParams = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    typeTextViewLayoutParams.setMargins(0, MiscHelper.ConvertDpToPx(ProfileFriendActivity.this, -10), 0, 0);

                    //
                    for (int rowNo = 0; rowNo < badgeArrayList.size(); rowNo = rowNo + 3) {
                        // Create row
                        TableRow tableRow = new TableRow(ProfileFriendActivity.this);

                        for (int columnNo = 0; (columnNo < 3) && ((rowNo + columnNo + 1) <= badgeArrayList.size()); columnNo++) {
                            // Create column
                            //
                            LinearLayout badgeLayout = new LinearLayout(ProfileFriendActivity.this);
                            badgeLayout.setBackground(getResources().getDrawable(R.drawable.border_box));
                            badgeLayout.setGravity(Gravity.CENTER);
                            badgeLayout.setOrientation(LinearLayout.VERTICAL);

                            //
                            Badge currentBadge = badgeArrayList.get(rowNo + columnNo);

                            //
                            TextView levelTextView = new TextView(ProfileFriendActivity.this);
                            levelTextView.setText(Integer.toString(currentBadge.GetLevel()));//todo width for single/double digit
                            levelTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
                            levelTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                            badgeLayout.addView(levelTextView, levelTextViewLayoutParams);

                            TextView typeTextView = new TextView(ProfileFriendActivity.this);
                            typeTextView.setText(currentBadge.GetType().toString());//todo format single/plural
                            typeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                            typeTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                            badgeLayout.addView(typeTextView, typeTextViewLayoutParams);

                            // Add column to row
                            tableRow.addView(badgeLayout, badgeLayoutParams);
                        }

                        // Add row to table
                        tableLayout.addView(tableRow, tableRowParams);
                    }

                    // Add table to linear layout container
                    ((LinearLayout) findViewById(R.id.badges_container)).addView(tableLayout, tableLayoutParams);
                } else {
                    // Error retrieving badges
                    // Set error layout
                    setContentView(R.layout.activity_connection_error);
                }
            }
        }.execute();
    }
}