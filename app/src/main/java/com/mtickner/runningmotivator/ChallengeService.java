package com.mtickner.runningmotivator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.gson.Gson;

import java.util.ArrayList;

public class ChallengeService extends Service {
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
            // Get challenges
            new HttpHelper.GetChallenges((Preferences.GetLoggedInUser(ChallengeService.this)).GetId(), true) {
                // Called after the background task finishes
                @Override
                protected void onPostExecute(String jsonResult) {
                    // Check server connection was successful
                    if (jsonResult != null) {
                        // Challenges retrieved successfully
                        challengeArrayList = JsonHelper.GetChallenges(jsonResult);

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
            }.execute();


            //todo get friend requests?
            //MiscHelper.CreateNotification(ChallengeService.this, intent, startId + 1, "test" + (startId + 1));


        }

        // Return sticky to continue running this service until it is explicitly stopped
        return START_STICKY;
    }
}