package com.mtickner.runningmotivator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.mtickner.runningmotivator.HttpHelper.urlPrefix;

public class ChallengeService extends Service {

    private static final String TAG = "ChallengeService";

    // Background-running service. Source: http://karanbalkar.com/2013/07/tutorial-41-using-alarmmanager-and-broadcastreceiver-in-android/

    private ArrayList<Challenge> challengeArrayList = new ArrayList<>();

    // Return the communication channel to the service
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Called by the system every time a client explicitly starts the service
    @Override
    public int onStartCommand(final Intent intent, int flags, final int startId) {
        // Check if user is logged in
        if (Preferences.GetLoggedInUser(ChallengeService.this) != null) {
            // User is logged in

            final String postUri = urlPrefix + "challenges-get.php";

            // Get challenges
            RequestQueue queue = Volley.newRequestQueue(ChallengeService.this);
            queue.add(new StringRequest(Method.POST, postUri, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, response);

                    // Check server connection was successful
                    if (response != null) {
                        // Challenges retrieved successfully
                        challengeArrayList = JsonHelper.GetChallenges(response, false);

                        if (challengeArrayList.size() > 0) {
                            // There are challenges
                            // Get array list of unnotified challenges
                            ArrayList<Challenge> unnotifiedChallengeArrayList = MiscHelper.GetUnnotifiedChallenges(challengeArrayList);

                            // Get notification preference
                            if (Preferences.GetSettingNotificationsEnabled(ChallengeService.this)) {
                                // Get unnotified challenges array list size
                                int unnotifiedChallengeCount = unnotifiedChallengeArrayList.size();

                                if (unnotifiedChallengeCount > 0) {
                                    for (int i = 0; i < unnotifiedChallengeCount; i++) {
                                        // Instantiate current challenge
                                        Challenge challenge = unnotifiedChallengeArrayList.get(i);

                                        // Define the notification action
                                        Intent intent = new Intent(ChallengeService.this, ChallengeViewActivity.class);
                                        intent.putExtra(Challenge.CHALLENGE_GSON, new Gson().toJson(challenge));

                                        // Create notification
                                        MiscHelper.CreateNotification(ChallengeService.this, intent, challenge.GetId(), getString(R.string.challenge_list_activity_challenge_service_notification_content_name_prefix_text) + challenge.GetRun().GetUser().GetName());
                                    }
                                }
                            }
                        }
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
                    params.put("userId", Integer.toString((Preferences.GetLoggedInUser(ChallengeService.this)).GetId()));
                    params.put("requestFromService", Boolean.toString(true));

                    return params;
                }
            });
        }

        // Return sticky to continue running this service until it is explicitly stopped
        return START_STICKY;
    }
}