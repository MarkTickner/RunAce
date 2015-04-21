package com.mtickner.runningmotivator;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;

public class HttpHelper {
    // Asynchronous methods. Source: http://www.vogella.com/tutorials/AndroidBackgroundProcessing/article.html

    private static final String urlPrefix = "http://www.mtickner.co.uk/runace-online/services/";

    // Method that POSTs data to the specified URI and returns an encoded JSON string
    public static String DoPost(String postUri, ArrayList<NameValuePair> postData) {
        String result;

        try {
            // Create objects to execute POST over HTTPS
            HttpClient httpClient = CustomSSLSocketFactory.CreateCustomHttpClient();
            HttpPost httpPost = new HttpPost(postUri);

            // Set POST data
            httpPost.setEntity(new UrlEncodedFormEntity(postData));

            // Execute post
            HttpResponse httpResponse = httpClient.execute(httpPost);

            // Get server response
            result = EntityUtils.toString(httpResponse.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
            result = null;
        }

        return result;
    }


    // Asynchronous inner class that registers a user online and returns the user ID
    public static class RegisterUser extends AsyncTask<Void, Void, String> {

        User loggedInUser;

        // Constructor to instantiate object
        public RegisterUser(String name, String email, String password) {
            loggedInUser = new User(name, email, password);
        }

        // Method which executes the background task
        @Override
        protected String doInBackground(Void... params) {
            // Create POST data
            ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>() {{
                add(new BasicNameValuePair("requestFromApplication", "true"));
                add(new BasicNameValuePair("name", loggedInUser.GetName()));
                add(new BasicNameValuePair("email", loggedInUser.GetEmail()));
                add(new BasicNameValuePair("password", loggedInUser.GetPassword()));
            }};

            return DoPost(urlPrefix + "user-register.php", postData);
        }
    }

    // Asynchronous inner class that authenticates a user online and returns the user details
    public static class LoginUser extends AsyncTask<Void, Void, String> {

        User loggedInUser;

        // Constructor to instantiate object
        public LoginUser(String email, String password) {
            loggedInUser = new User(email, password);
        }

        // Method which executes the background task
        @Override
        protected String doInBackground(Void... params) {
            // Create POST data
            ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>() {{
                add(new BasicNameValuePair("requestFromApplication", "true"));
                add(new BasicNameValuePair("email", loggedInUser.GetEmail()));
                add(new BasicNameValuePair("password", loggedInUser.GetPassword()));
            }};

            return DoPost(urlPrefix + "user-login.php", postData);
        }
    }

    // Asynchronous inner class that sends a password reset request
    public static class ResetPassword extends AsyncTask<Void, Void, String> {

        String email;

        // Constructor to instantiate object
        public ResetPassword(String email) {
            this.email = email;
        }

        // Method which executes the background task
        @Override
        protected String doInBackground(Void... params) {
            // Create POST data
            ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>() {{
                add(new BasicNameValuePair("requestFromApplication", "true"));
                add(new BasicNameValuePair("email", email));
            }};

            return DoPost(urlPrefix + "user-password-reset.php", postData);
        }
    }


    // Asynchronous inner class that returns a user's friends
    public static class GetFriends extends AsyncTask<Void, Void, String> {

        int userId;

        // Constructor to instantiate object
        public GetFriends(int userId) {
            this.userId = userId;
        }

        // Method which executes the background task
        @Override
        protected String doInBackground(Void... params) {
            // Create POST data
            ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>() {{
                add(new BasicNameValuePair("requestFromApplication", "true"));
                add(new BasicNameValuePair("userId", Integer.toString(userId)));
            }};

            return DoPost(urlPrefix + "friends-get.php", postData);
        }
    }

    // Asynchronous inner class that sends a friend request
    public static class AddFriend extends AsyncTask<Void, Void, String> {

        int userId;
        String friendEmail;

        // Constructor to instantiate object
        public AddFriend(int userId, String friendEmail) {
            this.userId = userId;
            this.friendEmail = friendEmail;
        }

        // Method which executes the background task
        @Override
        protected String doInBackground(Void... params) {
            // Generate verification string
            final String verificationString = MiscHelper.GenerateRandomString(20);

            // Create POST data
            ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>() {{
                add(new BasicNameValuePair("requestFromApplication", "true"));
                add(new BasicNameValuePair("userId", Integer.toString(userId)));
                add(new BasicNameValuePair("friendEmail", friendEmail));
                add(new BasicNameValuePair("verificationString", verificationString));
            }};

            return DoPost(urlPrefix + "friend-add.php", postData);
        }
    }

    // Asynchronous inner class that unfriends the specified users
    public static class Unfriend extends AsyncTask<Void, Void, String> {

        int user1Id, user2Id;

        // Constructor to instantiate object
        public Unfriend(int user1Id, int user2Id) {
            this.user1Id = user1Id;
            this.user2Id = user2Id;
        }

        // Method which executes the background task
        @Override
        protected String doInBackground(Void... params) {
            // Create POST data
            ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>() {{
                add(new BasicNameValuePair("requestFromApplication", "true"));
                add(new BasicNameValuePair("user1Id", Integer.toString(user1Id)));
                add(new BasicNameValuePair("user2Id", Integer.toString(user2Id)));
            }};

            return DoPost(urlPrefix + "friend-remove.php", postData);
        }
    }


    // Asynchronous inner class that returns a user's runs
    public static class GetRuns extends AsyncTask<Void, Void, String> {

        int userId;

        // Constructor to instantiate object
        public GetRuns(int userId) {
            this.userId = userId;
        }

        // Method which executes the background task
        @Override
        protected String doInBackground(Void... params) {
            // Create POST data
            ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>() {{
                add(new BasicNameValuePair("requestFromApplication", "true"));
                add(new BasicNameValuePair("userId", Integer.toString(userId)));
            }};

            return DoPost(urlPrefix + "runs-get.php", postData);
        }
    }

    // Asynchronous inner class that saves a run online
    public static class SaveRun extends AsyncTask<Void, Void, String> {

        int userId, totalTime, challengeId;
        double distanceTotal;
        boolean challengeSuccess;

        // Constructor to instantiate object
        public SaveRun(int userId, double distanceTotal, int totalTime, int challengeId, boolean challengeSuccess) {
            this.userId = userId;
            this.distanceTotal = distanceTotal;
            this.totalTime = totalTime;
            this.challengeId = challengeId;
            this.challengeSuccess = challengeSuccess;
        }

        // Method which executes the background task
        @Override
        protected String doInBackground(Void... params) {
            // Create POST data
            ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>() {{
                add(new BasicNameValuePair("requestFromApplication", "true"));
                add(new BasicNameValuePair("userId", Integer.toString(userId)));
                add(new BasicNameValuePair("distanceTotal", MiscHelper.FormatDouble(distanceTotal)));
                add(new BasicNameValuePair("totalTime", Integer.toString(totalTime)));
                add(new BasicNameValuePair("challengeId", Integer.toString(challengeId)));
                add(new BasicNameValuePair("challengeSuccess", Boolean.toString(challengeSuccess)));
            }};

            return DoPost(urlPrefix + "run-save.php", postData);
        }
    }


    // Asynchronous inner class that returns a challenge
    public static class GetChallenge extends AsyncTask<Void, Void, String> {

        int challengeId;
        boolean setRead;

        // Constructor
        public GetChallenge(int challengeId, boolean setRead) {
            this.challengeId = challengeId;
            this.setRead = setRead;
        }

        // Method which executes the background task
        @Override
        protected String doInBackground(Void... params) {
            // Create POST data
            ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>() {{
                add(new BasicNameValuePair("requestFromApplication", "true"));
                add(new BasicNameValuePair("challengeId", Integer.toString(challengeId)));
                add(new BasicNameValuePair("setRead", Boolean.toString(setRead)));
            }};

            return DoPost(urlPrefix + "challenge-get.php", postData);
        }
    }

    // Asynchronous inner class that returns a user's challenges
    public static class GetChallenges extends AsyncTask<Void, Void, String> {

        int userId;
        boolean requestFromService;

        // Constructor to instantiate object
        public GetChallenges(int userId, boolean requestFromService) {
            this.userId = userId;
            this.requestFromService = requestFromService;
        }

        // Method which executes the background task
        @Override
        protected String doInBackground(Void... params) {
            // Create POST data
            ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>() {{
                add(new BasicNameValuePair("requestFromApplication", "true"));
                add(new BasicNameValuePair("userId", Integer.toString(userId)));
                add(new BasicNameValuePair("requestFromService", Boolean.toString(requestFromService)));
            }};

            return DoPost(urlPrefix + "challenges-get.php", postData);
        }
    }

    // Asynchronous inner class that saves a challenge
    public static class SaveChallenge extends AsyncTask<Void, Void, String> {

        int userId, friendUserId, runId;
        String message;

        // Constructor to instantiate object
        public SaveChallenge(int userId, int friendUserId, int runId, String message) {
            this.userId = userId;
            this.friendUserId = friendUserId;
            this.runId = runId;
            this.message = message;
        }

        // Method which executes the background task
        @Override
        protected String doInBackground(Void... params) {
            // Create POST data
            ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>() {{
                add(new BasicNameValuePair("requestFromApplication", "true"));
                add(new BasicNameValuePair("userId", Integer.toString(userId)));
                add(new BasicNameValuePair("friendUserId", Integer.toString(friendUserId)));
                add(new BasicNameValuePair("runId", Integer.toString(runId)));
                add(new BasicNameValuePair("message", message));
            }};

            return DoPost(urlPrefix + "challenge-save.php", postData);
        }
    }


    // Asynchronous inner class that returns a user's badges
    public static class GetBadges extends AsyncTask<Void, Void, String> {

        int userId;

        // Constructor to instantiate object
        public GetBadges(int userId) {
            this.userId = userId;
        }

        // Method which executes the background task
        @Override
        protected String doInBackground(Void... params) {
            // Create POST data
            ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>() {{
                add(new BasicNameValuePair("requestFromApplication", "true"));
                add(new BasicNameValuePair("userId", Integer.toString(userId)));
            }};

            return DoPost(urlPrefix + "profile-get.php", postData);
        }
    }
}