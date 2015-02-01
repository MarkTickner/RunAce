package com.mtickner.runningmotivator;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
                //todo not on own profile
                Toast.makeText(this, "Are you sure you want to unfriend [name], etc", Toast.LENGTH_SHORT).show();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}