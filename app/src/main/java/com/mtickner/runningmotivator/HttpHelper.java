package com.mtickner.runningmotivator;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;

public class HttpHelper {
    // Asynchronous methods. Source: http://www.vogella.com/tutorials/AndroidBackgroundProcessing/article.html

    static final String urlPrefix = "http://www.mtickner.co.uk/RunAceOnline/services/";

    // Method that POSTs data to the specified URI and returns an encoded JSON string
    public static String DoPost(String postUri, ArrayList<NameValuePair> postData) {
        String result;

        try {
            // Create objects to execute POST
            HttpClient httpClient = new DefaultHttpClient();
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