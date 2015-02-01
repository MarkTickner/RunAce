package com.mtickner.runningmotivator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.google.gson.Gson;

//todo
public class ProfileActivity extends ActionBarActivity {

    // Called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Get user
        Intent intent = getIntent();
        User user = new Gson().fromJson(intent.getStringExtra(User.USER_GSON), User.class);

        // Display user details
        //((TextView) findViewById(R.id.name)).setText(user.GetName());
        //((TextView) findViewById(R.id.message)).setText("Member since: " + MiscHelper.FormatDateForDisplay(user.GetDateRegistered()));
    }
}