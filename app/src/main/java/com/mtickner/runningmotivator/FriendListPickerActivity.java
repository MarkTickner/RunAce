package com.mtickner.runningmotivator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

public class FriendListPickerActivity extends ActionBarActivity {

    private ArrayList<Friend> friendArrayList = new ArrayList<>();
    private final FriendListViewAdaptor friendListViewAdaptor = new FriendListViewAdaptor();

    // Called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display loading layout
        setContentView(R.layout.activity_loading);

        // Load friends
        friendListViewAdaptor.refresh();
    }

    // Custom adaptor for the list view
    private class FriendListViewAdaptor extends BaseAdapter {
        // Returns the count of data set items
        @Override
        public int getCount() {
            return friendArrayList.size();
        }

        // Get the data item associated with the specified position in the data set
        @Override
        public Friend getItem(int position) {
            return friendArrayList.get(position);
        }

        // Get the row ID associated with the specified position in the list
        @Override
        public long getItemId(int position) {
            return position;
        }

        // Get a View that displays the data at the specified position in the data set
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) FriendListPickerActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.activity_friend_list_item, parent, false);
            }

            Friend friend = friendArrayList.get(position);

            TextView friendName = (TextView) convertView.findViewById(R.id.friend_name);
            friendName.setText(friend.GetUser().GetName());

            // Display pending text if friend request has not yet been accepted
            if (friend.GetStatus().equals(Friend.Status.PENDING)) {
                TextView friendStatus = (TextView) convertView.findViewById(R.id.friend_pending);
                friendStatus.setVisibility(View.VISIBLE);
                friendStatus.setText(getString(R.string.friend_list_activity_friend_pending_request));
            }

            return convertView;
        }

        // Refresh the contents of the list view
        public void refresh() {
            // Get friends
            new HttpHelper.GetFriends((Preferences.GetLoggedInUser(FriendListPickerActivity.this)).GetId()) {
                // Called after the background task finishes
                @Override
                protected void onPostExecute(String jsonResult) {
                    // Check server connection was successful
                    if (jsonResult != null) {
                        // Friends retrieved successfully
                        friendArrayList = JsonHelper.GetFriends(jsonResult, Friend.Status.ACCEPTED);

                        // Set main layout
                        setContentView(R.layout.activity_friend_list_picker);

                        // Populate the list view with friend list items. Source: http://www.codelearn.org/android-tutorial/android-listview
                        final ListView listView = (ListView) findViewById(R.id.friend_list_view);
                        listView.setEmptyView(findViewById(R.id.empty_list_item));
                        listView.setAdapter(friendListViewAdaptor);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            // Handler for when an item in the list view is pressed
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                // Get pressed friend, compensating for header
                                Friend friend = getItem(position);

                                // Return user
                                Intent data = new Intent();
                                data.putExtra(User.USER_GSON, new Gson().toJson(friend.GetUser()));
                                setResult(RESULT_OK, data);

                                finish();
                            }
                        });
                    } else {
                        // Error retrieving friends
                        // Set error layout
                        setContentView(R.layout.activity_connection_error);
                    }
                }
            }.execute();
        }
    }
}