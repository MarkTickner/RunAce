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

//todo
public class ProfileFriendActivity extends ActionBarActivity {

    // Called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_friend);

        // Get friend
        Intent intent = getIntent();
        Friend friend = new Gson().fromJson(intent.getStringExtra(Friend.FRIEND_GSON), Friend.class);

        // Display friend details
        ((TextView) findViewById(R.id.name)).setText(friend.GetUser().GetName().toUpperCase());
        ((TextView) findViewById(R.id.date_friends_since)).setText("Friends since: " + MiscHelper.FormatDateForDisplay(friend.GetStatusDate()));


        //todo get badges
        int badgesCount = 10;

        // Add table to linear layout container
        ((LinearLayout) findViewById(R.id.badges_container)).addView(CreateBadgesTable(badgesCount));
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
    private TableLayout CreateBadgesTable(int badgesCount) {
        TableLayout tableLayout = new TableLayout(this);
        tableLayout.setStretchAllColumns(true);

        LinearLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableRowParams.setMargins(0, 0, 0, MiscHelper.ConvertDpToPx(this, 15));

        TableRow.LayoutParams badgeLayoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        badgeLayoutParams.setMargins(MiscHelper.ConvertDpToPx(this, 15), 0, MiscHelper.ConvertDpToPx(this, 15), 0);
        badgeLayoutParams.gravity = Gravity.CENTER_VERTICAL;

        LinearLayout.LayoutParams tv1LayoutParams = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tv1LayoutParams.setMargins(0, MiscHelper.ConvertDpToPx(this, -5), 0, 0);

        LinearLayout.LayoutParams tv2LayoutParams = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tv2LayoutParams.setMargins(0, MiscHelper.ConvertDpToPx(this, -10), 0, 0);


        for (int rowNo = 0; rowNo < badgesCount; rowNo = rowNo + 3) {
            // Create row
            TableRow tableRow = new TableRow(this);

            for (int columnNo = 0; (columnNo < 3) && ((rowNo + columnNo + 1) <= badgesCount); columnNo++) {
                // Create column


                LinearLayout badgeLayout = new LinearLayout(this);
                badgeLayout.setBackground(getResources().getDrawable(R.drawable.border_box));
                badgeLayout.setGravity(Gravity.CENTER);
                badgeLayout.setOrientation(LinearLayout.VERTICAL);

                TextView tv1 = new TextView(this);
                tv1.setText(Integer.toString(rowNo + columnNo + 1));
                tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
                tv1.setGravity(Gravity.CENTER_HORIZONTAL);
                badgeLayout.addView(tv1, tv1LayoutParams);

                TextView tv2 = new TextView(this);
                tv2.setText("BADGE");
                tv2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                tv2.setGravity(Gravity.CENTER_HORIZONTAL);
                badgeLayout.addView(tv2, tv2LayoutParams);

                // Add column to row
                tableRow.addView(badgeLayout, badgeLayoutParams);
            }

            // Add row to table
            tableLayout.addView(tableRow, tableRowParams);
        }

        tableLayout.setLayoutParams(tableLayoutParams);

        return tableLayout;
    }
}