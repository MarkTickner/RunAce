package com.mtickner.runningmotivator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JsonHelper {
    // Method that returns a user after login or null if authentication failed
    public static User GetUserAfterLogin(String jsonResult) {
        User loggedInUser = null;

        // Server connection was successful
        try {
            // Create JSON object from server response
            JSONObject resultObject = new JSONObject(jsonResult);

            // Get 'OutputType'
            if (resultObject.getString("OutputType").equals("Success")) {
                // Logged in successfully, no error in PHP
                // Get 'Details' array
                JSONObject resultDetailsArray = resultObject.getJSONObject("Details");

                // Get user details
                loggedInUser = new User(
                        resultDetailsArray.getInt("ID"),
                        resultDetailsArray.getString("NAME"),
                        resultDetailsArray.getString("EMAIL"),
                        MiscHelper.FormatDateFromDatabase(resultDetailsArray.getString("DATE_REGISTERED"))
                );
            }
        } catch (Exception e) {
            // Catches exceptions including JSONException when creating JSON objects
            e.printStackTrace();
        }

        // Return the user or null if error
        return loggedInUser;
    }


    // Method that returns an array list of friends
    public static ArrayList<Friend> GetFriends(String jsonResult, Friend.Status statusFilter) {
        ArrayList<Friend> friendArrayList = null;

        // Server connection was successful
        try {
            // Instantiate array list
            friendArrayList = new ArrayList<>();

            // Create JSON object from server response
            JSONObject resultObject = new JSONObject(jsonResult);

            // Get 'OutputType'
            if (resultObject.getString("OutputType").equals("Success")) {
                // Friends retrieved successfully, no error in PHP
                // Get 'Details' array
                JSONArray resultDetailsArray = resultObject.getJSONArray("Details");

                // Add friend objects to array list
                for (int i = 0; i < resultDetailsArray.length(); i++) {
                    JSONObject friendDetailsObject = resultDetailsArray.getJSONObject(i);
                    JSONObject friendDetailsUserObject = friendDetailsObject.getJSONObject("USER_ID");

                    // Get friend status
                    Friend.Status statusFromDatabase = (friendDetailsObject.getString("STATUS").equals("A")) ? Friend.Status.ACCEPTED : Friend.Status.PENDING;

                    // Only add current friend if the status filter is set to 'all' or if the current friend status is the same as the status filter
                    if (statusFilter.equals(Friend.Status.ALL) || statusFilter.equals(statusFromDatabase)) {
                        Friend friend = new Friend(
                                friendDetailsObject.getInt("ID"),
                                new User(
                                        friendDetailsUserObject.getInt("ID"),
                                        friendDetailsUserObject.getString("NAME"),
                                        friendDetailsUserObject.getString("EMAIL"),
                                        MiscHelper.FormatDateFromDatabase(friendDetailsUserObject.getString("DATE_REGISTERED"))
                                ),
                                statusFromDatabase,
                                MiscHelper.FormatDateFromDatabase(friendDetailsObject.getString("STATUS_DATE"))
                        );

                        friendArrayList.add(friend);
                    }
                }
            }
        } catch (Exception e) {
            // Catches exceptions including JSONException when creating JSON objects
            e.printStackTrace();
        }

        // Return an array list of friends or null if error
        return friendArrayList;
    }

    // Method that returns the status of an added friend
    public static String GetAddFriendStatus(String jsonResult) {
        String friendAddStatus = null;

        // Check server connection was successful
        if (jsonResult != null) {
            // Server connection was successful
            try {
                // Create JSON object from server response
                JSONObject resultObject = new JSONObject(jsonResult);

                // Get 'OutputType'
                if (resultObject.getString("OutputType").equals("Success")) {
                    // Friend request sent successfully, no error in PHP
                    // Get 'Details' array
                    JSONArray resultDetailsArray = resultObject.getJSONArray("Details");

                    friendAddStatus = resultDetailsArray.getString(0);
                }
            } catch (JSONException e) {
                // Catches exceptions including JSONException when creating JSON objects
                e.printStackTrace();
            }
        }

        // Return true if the challenge was saved or false if error
        return friendAddStatus;
    }


    // Method that returns a run
    public static Run GetRun(String jsonResult) {
        Run run = null;

        // Check server connection was successful
        if (jsonResult != null) {
            // Server connection was successful
            try {
                // Create JSON object from server response
                JSONObject resultObject = new JSONObject(jsonResult);

                // Get 'OutputType'
                if (resultObject.getString("OutputType").equals("Success")) {
                    // Run retrieved successfully, no error in PHP
                    // Get 'Details' array
                    JSONArray resultDetailsArray = resultObject.getJSONArray("Details");

                    JSONObject runDetailsObject = resultDetailsArray.getJSONObject(0);
                    JSONObject runDetailsUserObject = runDetailsObject.getJSONObject("USER_ID");

                    // Get run details
                    run = new Run(
                            runDetailsObject.getInt("ID"),
                            new User(
                                    runDetailsUserObject.getInt("ID"),
                                    runDetailsUserObject.getString("NAME"),
                                    runDetailsUserObject.getString("EMAIL"),
                                    MiscHelper.FormatDateFromDatabase(runDetailsUserObject.getString("DATE_REGISTERED"))
                            ),
                            runDetailsObject.getDouble("DISTANCE_TOTAL"),
                            runDetailsObject.getInt("TOTAL_TIME"),
                            MiscHelper.FormatDateFromDatabase(runDetailsObject.getString("DATE_RUN"))
                    );
                }

            } catch (Exception e) {
                // Catches exceptions including JSONException when creating JSON objects
                e.printStackTrace();
            }
        }

        // Return the run or null if error
        return run;
    }

    // Method that returns an array list of runs
    public static ArrayList<Run> GetRuns(String jsonResult) {
        ArrayList<Run> runArrayList = null;

        // Check server connection was successful
        if (jsonResult != null) {
            // Server connection was successful
            try {
                // Instantiate array list
                runArrayList = new ArrayList<>();

                // Create JSON object from server response
                JSONObject resultObject = new JSONObject(jsonResult);

                // Get 'OutputType'
                if (resultObject.getString("OutputType").equals("Success")) {
                    // Runs retrieved successfully, no error in PHP
                    // Get 'Details' array
                    JSONArray resultDetailsArray = resultObject.getJSONArray("Details");

                    // Add run objects to array list
                    for (int i = 0; i < resultDetailsArray.length(); i++) {
                        JSONObject runDetailsObject = resultDetailsArray.getJSONObject(i);
                        JSONObject runDetailsUserObject = runDetailsObject.getJSONObject("USER_ID");

                        Run run = new Run(
                                runDetailsObject.getInt("ID"),
                                new User(
                                        runDetailsUserObject.getInt("ID"),
                                        runDetailsUserObject.getString("NAME"),
                                        runDetailsUserObject.getString("EMAIL"),
                                        MiscHelper.FormatDateFromDatabase(runDetailsUserObject.getString("DATE_REGISTERED"))
                                ),
                                runDetailsObject.getDouble("DISTANCE_TOTAL"),
                                runDetailsObject.getInt("TOTAL_TIME"),
                                MiscHelper.FormatDateFromDatabase(runDetailsObject.getString("DATE_RUN"))
                        );

                        runArrayList.add(run);
                    }
                }
            } catch (Exception e) {
                // Catches exceptions including JSONException when creating JSON objects
                e.printStackTrace();
            }
        }

        return runArrayList;
    }


    // Method that returns a challenge
    public static Challenge GetChallenge(String jsonResult) {
        Challenge challenge = null;

        // Server connection was successful
        try {
            // Create JSON object from server response
            JSONObject resultObject = new JSONObject(jsonResult);

            // Get 'OutputType'
            if (resultObject.getString("OutputType").equals("Success")) {
                // Challenge retrieved successfully, no error in PHP
                // Get 'Details' array
                JSONArray resultDetailsArray = resultObject.getJSONArray("Details");

                JSONObject challengeDetailsObject = resultDetailsArray.getJSONObject(0);
                JSONObject challengeDetailsRunObject = challengeDetailsObject.getJSONObject("RUN_ID");
                JSONObject challengeDetailsRunUserObject = challengeDetailsRunObject.getJSONObject("USER_ID");

                challenge = new Challenge(
                        challengeDetailsObject.getInt("ID"),
                        new Run(
                                challengeDetailsRunObject.getInt("ID"),
                                new User(
                                        challengeDetailsRunUserObject.getInt("ID"),
                                        challengeDetailsRunUserObject.getString("NAME"),
                                        challengeDetailsRunUserObject.getString("EMAIL"),
                                        MiscHelper.FormatDateFromDatabase(challengeDetailsRunUserObject.getString("DATE_REGISTERED"))
                                ),
                                challengeDetailsRunObject.getDouble("DISTANCE_TOTAL"),
                                challengeDetailsRunObject.getInt("TOTAL_TIME"),
                                MiscHelper.FormatDateFromDatabase(challengeDetailsRunObject.getString("DATE_RUN"))
                        ),
                        challengeDetailsObject.getString("MESSAGE"),
                        MiscHelper.FormatDateFromDatabase(challengeDetailsObject.getString("DATE_CREATED")),
                        (challengeDetailsObject.getInt("IS_NOTIFIED") != 0),
                        (challengeDetailsObject.getInt("IS_READ") != 0),
                        MiscHelper.FormatDateFromDatabase(challengeDetailsObject.getString("DATE_COMPLETED"))
                );
            }
        } catch (Exception e) {
            // Catches exceptions including JSONException when creating JSON objects
            e.printStackTrace();
        }

        // Return the challenge or null if error
        return challenge;
    }

    // Method that returns an array list of challenges
    public static ArrayList<Challenge> GetChallenges(String jsonResult) {
        ArrayList<Challenge> challengeArrayList = null;

        // Server connection was successful
        try {
            // Instantiate array list
            challengeArrayList = new ArrayList<>();

            // Create JSON object from server response
            JSONObject resultObject = new JSONObject(jsonResult);

            // Get 'OutputType'
            if (resultObject.getString("OutputType").equals("Success")) {
                // Challenges retrieved successfully, no error in PHP
                // Get 'Details' array
                JSONArray resultDetailsArray = resultObject.getJSONArray("Details");

                // Add challenge objects to array list
                for (int i = 0; i < resultDetailsArray.length(); i++) {
                    JSONObject challengeDetailsObject = resultDetailsArray.getJSONObject(i);
                    JSONObject challengeDetailsRunObject = challengeDetailsObject.getJSONObject("RUN_ID");
                    JSONObject challengeDetailsRunUserObject = challengeDetailsRunObject.getJSONObject("USER_ID");

                    Challenge challenge = new Challenge(
                            challengeDetailsObject.getInt("ID"),
                            new Run(
                                    challengeDetailsRunObject.getInt("ID"),
                                    new User(
                                            challengeDetailsRunUserObject.getInt("ID"),
                                            challengeDetailsRunUserObject.getString("NAME"),
                                            challengeDetailsRunUserObject.getString("EMAIL"),
                                            MiscHelper.FormatDateFromDatabase(challengeDetailsRunUserObject.getString("DATE_REGISTERED"))
                                    ),
                                    challengeDetailsRunObject.getDouble("DISTANCE_TOTAL"),
                                    challengeDetailsRunObject.getInt("TOTAL_TIME"),
                                    MiscHelper.FormatDateFromDatabase(challengeDetailsRunObject.getString("DATE_RUN"))
                            ),
                            challengeDetailsObject.getString("MESSAGE"),
                            MiscHelper.FormatDateFromDatabase(challengeDetailsObject.getString("DATE_CREATED")),
                            (challengeDetailsObject.getInt("IS_NOTIFIED") != 0),
                            (challengeDetailsObject.getInt("IS_READ") != 0),
                            MiscHelper.FormatDateFromDatabase(challengeDetailsObject.getString("DATE_COMPLETED"))
                    );

                    challengeArrayList.add(challenge);
                }
            }
        } catch (Exception e) {
            // Catches exceptions including JSONException when creating JSON objects
            e.printStackTrace();
        }

        return challengeArrayList;
    }

    // Method that saves a challenge
    public static boolean SaveChallenge(String jsonResult) {
        boolean challengeSaved = false;

        // Check server connection was successful
        if (jsonResult != null) {
            // Server connection was successful
            try {
                // Create JSON object from server response
                JSONObject resultObject = new JSONObject(jsonResult);

                // Get 'OutputType'
                if (resultObject.getString("OutputType").equals("Success")) {
                    // Challenge saved successfully, no error in PHP
                    challengeSaved = true;
                }
            } catch (JSONException e) {
                // Catches exceptions including JSONException when creating JSON objects
                e.printStackTrace();
            }
        }

        // Return true if the challenge was saved or false if error
        return challengeSaved;
    }
}