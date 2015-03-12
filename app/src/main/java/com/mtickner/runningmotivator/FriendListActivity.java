package com.mtickner.runningmotivator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

public class FriendListActivity extends ActionBarActivity {

    private ArrayList<Friend> friendArrayList = new ArrayList<>();
    private Friend.Status friendsCurrentlyDisplayed = Friend.Status.ACCEPTED;
    private final FriendListViewAdaptor friendListViewAdaptor = new FriendListViewAdaptor();
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean firstDisplay;

    // Called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display loading layout
        setContentView(R.layout.activity_loading);

        firstDisplay = true;
    }

    // Called when the activity starts interacting with the user
    @Override
    protected void onResume() {
        super.onResume();

        // Load only accepted friends by default
        friendListViewAdaptor.refresh(friendsCurrentlyDisplayed);
    }

    // Initialise the contents of the Activity's standard options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.menu_friend_list, menu);

        return true;
    }

    // Called whenever an item in the options menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_friend:
                // Launch the contacts picker activity
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);
                startActivityForResult(intent, 1);

                return true;
            case R.id.menu_include_pending:

                // Determine if check box is checked or not
                if (item.isChecked()) {
                    // Display only accepted friends
                    friendListViewAdaptor.refresh(Friend.Status.ACCEPTED);
                    item.setChecked(false);
                } else {
                    // Display all friends including pending requests
                    friendListViewAdaptor.refresh(Friend.Status.ALL);
                    item.setChecked(true);
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Called when the activity has detected the user's press of the back key
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    // Called when a launched activity exits, in this case the contacts picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Identify request
        if (requestCode == 1) {
            // Confirm request was successful
            if (resultCode == RESULT_OK) {

                // Source: http://stackoverflow.com/questions/18559574/android-contact-picker-with-only-phone-numbers
                Uri uri = data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                cursor.moveToFirst();

                // Get selected contact name and email address
                final String friendName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                final String friendEmail = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

                // Close cursor object
                cursor.close();

                // Confirm with user
                final AlertDialog alert = new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.friend_list_activity_add_friend_dialog_text_start) + friendName + getString(R.string.friend_list_activity_add_remove_friend_dialog_text_end))
                        .setPositiveButton(getString(R.string.friend_list_activity_add_friend_confirm_button_text).toUpperCase(), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Send friend request
                                new HttpHelper.AddFriend(Preferences.GetLoggedInUser(FriendListActivity.this).GetId(), friendEmail) {
                                    // Called after the background task finishes
                                    @Override
                                    protected void onPostExecute(String jsonResult) {
                                        String friendAddStatus;

                                        // Check server connection was successful
                                        if ((friendAddStatus = JsonHelper.GetAddFriendStatus(jsonResult)) != null) {
                                            // Get friend add status
                                            switch (friendAddStatus) {
                                                case "Sent":
                                                    // Display success toast to user
                                                    Toast.makeText(FriendListActivity.this, getString(R.string.friend_list_activity_friend_request_sent_toast_text), Toast.LENGTH_SHORT).show();

                                                    break;
                                                case "Invited":
                                                    // Display success toast to user
                                                    Toast.makeText(FriendListActivity.this, getString(R.string.friend_list_activity_friend_invited_toast_text), Toast.LENGTH_SHORT).show();

                                                    break;
                                                case "Already":
                                                    // Display success toast to user
                                                    Toast.makeText(FriendListActivity.this, getString(R.string.friend_list_activity_friend_already_added_toast_text), Toast.LENGTH_SHORT).show();

                                                    break;
                                            }

                                            friendListViewAdaptor.refresh(Friend.Status.ALL);
                                        } else {
                                            // Error adding friend
                                            // Display error toast to user
                                            Toast.makeText(FriendListActivity.this, ErrorCodes.GetErrorMessage(FriendListActivity.this, 102), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }.execute();
                            }
                        })
                        .setNegativeButton(getString(R.string.friend_list_activity_add_friend_cancel_button_text).toUpperCase(), null)
                        .create();
                // Set button colour
                alert.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.runace_red_primary));
                        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.runace_grey_primary));
                    }
                });
                alert.show();
            }
        }
    }

    // Custom adaptor for the list view
    public class FriendListViewAdaptor extends BaseAdapter {
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
                LayoutInflater inflater = (LayoutInflater) FriendListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        // Clear the contents of the list view
        private void clear() {
            friendArrayList.clear();

            notifyDataSetChanged();
        }

        // Refresh the contents of the list view
        public void refresh(final Friend.Status friendsToDisplay) {
            friendsCurrentlyDisplayed = friendsToDisplay;

            // Get friends
            new HttpHelper.GetFriends((Preferences.GetLoggedInUser(FriendListActivity.this)).GetId()) {
                // Called after the background task finishes
                @Override
                protected void onPostExecute(String jsonResult) {
                    // Clear the list view
                    clear();

                    // Check server connection was successful
                    if (jsonResult != null) {
                        // Friends retrieved successfully
                        friendArrayList = JsonHelper.GetFriends(jsonResult, friendsToDisplay);

                        // Determine if this is the first time the method has been called
                        if (firstDisplay) {
                            // Set main layout
                            setContentView(R.layout.activity_friend_list);

                            // Add refresh listener to swipe layout. Source: http://antonioleiva.com/swiperefreshlayout/
                            swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.friend_list_swipe_container);
                            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                // Handler for when a swipe triggers a refresh
                                @Override
                                public void onRefresh() {
                                    refresh(friendsCurrentlyDisplayed);
                                }
                            });

                            // Populate the list view with friend list items. Source: http://www.codelearn.org/android-tutorial/android-listview
                            final ListView listView = (ListView) findViewById(R.id.friend_list_view);
                            listView.addHeaderView(MiscHelper.CreateListViewHeader(FriendListActivity.this, getString(R.string.friend_list_activity_header)), null, false);
                            listView.setEmptyView(findViewById(R.id.empty_list_item));
                            listView.setAdapter(friendListViewAdaptor);
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                // Handler for when an item in the list view is pressed
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    // Get pressed friend, compensating for header
                                    Friend friend = getItem(position - 1);

                                    if (friend.GetStatus().equals(Friend.Status.ACCEPTED)) {
                                        // Open user if accepted
                                        Intent intent = new Intent(FriendListActivity.this, ProfileFriendActivity.class);
                                        intent.putExtra(Friend.FRIEND_GSON, new Gson().toJson(friend));
                                        startActivity(intent);
                                    } else {
                                        // Display toast if still pending
                                        Toast.makeText(FriendListActivity.this, getString(R.string.friend_list_activity_pending_friend_profile_toast_text), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                                @Override
                                public void onScrollStateChanged(AbsListView view, int scrollState) {
                                }

                                @Override
                                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                                    // Scroll fix to allow list view to scroll to top before swipe to refresh shows
                                    // Source: http://nlopez.io/swiperefreshlayout-with-listview-done-right/
                                    int topRowVerticalPosition = (listView.getChildCount() == 0) ? 0 : listView.getChildAt(0).getTop();
                                    swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
                                }
                            });

                            firstDisplay = false;
                        } else {
                            notifyDataSetChanged();

                            swipeRefreshLayout.setRefreshing(false);
                        }
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