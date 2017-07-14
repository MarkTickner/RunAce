package com.mtickner.runningmotivator;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import static com.mtickner.runningmotivator.HttpHelper.urlPrefix;

public class ChallengeFriendActivity extends ActionBarActivity {

    private static final String TAG = "ChallengeFriendActivity";
    private Run run;
    private User friendUser;
    private ProgressDialog sendingChallengeProgressDialog;

    // Called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_friend);

        // Get run to set as challenge
        Intent intent = getIntent();
        String runGson = intent.getStringExtra(Run.RUN_GSON);
        run = new Gson().fromJson(runGson, Run.class);

        // Output run details
        // Output time
        ((TextView) findViewById(R.id.time)).setText(MiscHelper.FormatSecondsToHoursMinutesSeconds(run.GetTotalTime()));

        // Output distance to preferred unit
        String distanceUnit = Preferences.GetSettingDistanceUnit(ChallengeFriendActivity.this);

        if (distanceUnit.equals(getString(R.string.run_activity_run_complete_activity_distance_unit_kilometres))) {
            // Kilometres
            ((TextView) findViewById(R.id.distance)).setText(MiscHelper.FormatDouble(run.GetDistanceTotal()));
        } else {
            // Miles
            ((TextView) findViewById(R.id.distance)).setText(MiscHelper.FormatDouble(MiscHelper.ConvertKilometresToMiles(run.GetDistanceTotal())));
        }

        // Output distance to preferred unit
        ((TextView) findViewById(R.id.distance_unit)).setText(distanceUnit);

        // Change send challenge button colour to blue
        Button sendBtn = (Button) findViewById(R.id.send_button);
        sendBtn.getBackground().setColorFilter(getResources().getColor(R.color.runace_blue_primary), PorterDuff.Mode.SRC_ATOP);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            sendBtn.setTextColor(getResources().getColor(R.color.white));
        }
    }

    // Called when a launched activity exits, in this case the contacts picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Identify request
        if (requestCode == 1) {
            // Confirm request was successful
            if (resultCode == RESULT_OK) {
                // Get selected friend user
                String userGson = data.getStringExtra(User.USER_GSON);
                friendUser = new Gson().fromJson(userGson, User.class);

                // Show friend details
                findViewById(R.id.friend_container).setVisibility(View.VISIBLE);

                // Display name
                ((TextView) findViewById(R.id.friend_name)).setText(friendUser.GetName().toUpperCase());

                // Update button text
                ((Button) findViewById(R.id.choose_friend_button)).setText(getString(R.string.challenge_friend_activity_change_friend_button_text));
            }
        }
    }

    // Handler for the 'Choose Friend' button
    public void ChooseFriend(View view) {
        // Launch the friend picker activity
        Intent intent = new Intent(this, FriendListPickerActivity.class);
        startActivityForResult(intent, 1);
    }

    // Handler for the 'Send Challenge' button
    public void SendChallenge(View view) {
        try {
            // Hide the soft keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (NullPointerException e) {
            // Keyboard not visible
            e.printStackTrace();
        }

        // Get message
        final EditText message = (EditText) findViewById(R.id.message);

        // Display progress dialog to user
        progressHandler.postDelayed(progressRunnable, 500);

        final String postUri = urlPrefix + "challenge-save.php";

        // Save challenge
        RequestQueue queue = Volley.newRequestQueue(ChallengeFriendActivity.this);
        queue.add(new StringRequest(Method.POST, postUri, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);

                // Dismiss progress dialog
                progressHandler.removeCallbacks(progressRunnable);
                if (sendingChallengeProgressDialog != null) {
                    sendingChallengeProgressDialog.dismiss();
                }

                // Check server connection was successful
                if (JsonHelper.ResultSuccess(response)) {
                    // Challenge saved successfully
                    // Display success toast to user
                    Toast.makeText(ChallengeFriendActivity.this, getString(R.string.challenge_friend_activity_challenge_sent_toast_text), Toast.LENGTH_SHORT).show();

                    // Direct to the home activity
                    Intent intent = new Intent(ChallengeFriendActivity.this, HomeActivity.class);
                    startActivity(intent);

                    finish();
                } else {
                    // Error saving challenge
                    // Display error toast to user
                    Toast.makeText(ChallengeFriendActivity.this, ErrorCodes.GetErrorMessage(ChallengeFriendActivity.this, 102), Toast.LENGTH_LONG).show();
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
                params.put("userId", Integer.toString((Preferences.GetLoggedInUser(ChallengeFriendActivity.this)).GetId()));
                params.put("friendUserId", Integer.toString(friendUser.GetId()));
                params.put("runId", Integer.toString(run.GetId()));
                params.put("message", message.getText().toString());

                return params;
            }
        });
    }

    // Display a progress dialog after a 500ms delay, so it does not show if there is a quick connection. Source: http://stackoverflow.com/a/10947069/1164058
    private final Handler progressHandler = new Handler();
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            sendingChallengeProgressDialog = ProgressDialog.show(ChallengeFriendActivity.this, null, getString(R.string.challenge_friend_activity_sending_challenge_dialog_text));
        }
    };
}