package com.mtickner.runningmotivator;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

public class ChallengeListActivity extends ActionBarActivity {

    private ArrayList<Challenge> challengeArrayList = new ArrayList<>();
    private boolean completedCurrentlyShown = true;
    private final ChallengeListViewAdaptor challengeListViewAdaptor = new ChallengeListViewAdaptor();
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

        // Load challenges
        challengeListViewAdaptor.refresh(completedCurrentlyShown);
    }

    // Initialise the contents of the Activity's standard options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.menu_challenge_list, menu);

        return true;
    }

    // Called whenever an item in the options menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_show_completed:
                // Determine if check box is checked or not
                if (item.isChecked()) {
                    // Do not display completed challenges
                    challengeListViewAdaptor.refresh(false);
                    item.setChecked(false);
                } else {
                    // Display all challenges included completed ones
                    challengeListViewAdaptor.refresh(true);
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

    // Custom adaptor for the list view
    public class ChallengeListViewAdaptor extends BaseAdapter {
        // Returns the count of data set items
        @Override
        public int getCount() {
            return challengeArrayList.size();
        }

        // Get the data item associated with the specified position in the data set
        @Override
        public Challenge getItem(int position) {
            return challengeArrayList.get(position);
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
                LayoutInflater inflater = (LayoutInflater) ChallengeListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.activity_challenge_list_item, parent, false);
            }

            Challenge challenge = challengeArrayList.get(position);

            ImageView image = (ImageView) convertView.findViewById(R.id.challenge_image);
            TextView title = (TextView) convertView.findViewById(R.id.distance_time);
            TextView content = (TextView) convertView.findViewById(R.id.content);
            TextView date = (TextView) convertView.findViewById(R.id.date);

            // Generate content text from challenge run details
            String contentText = getString(R.string.challenge_list_activity_challenge_details_prefix);

            // Get preferred distance unit
            String distanceUnit = Preferences.GetSettingDistanceUnit(ChallengeListActivity.this);

            if (distanceUnit.equals(getString(R.string.run_activity_run_complete_activity_distance_unit_kilometres_placeholder))) {
                // Kilometres
                contentText += MiscHelper.FormatDouble(challenge.GetRun().GetDistanceTotal());
            } else {
                // Miles
                contentText += MiscHelper.FormatDouble(MiscHelper.ConvertKilometresToMiles(challenge.GetRun().GetDistanceTotal()));
            }

            // Distance preferred unit
            contentText += distanceUnit + getString(R.string.challenge_list_activity_challenge_details_separator);

            // Time
            contentText += MiscHelper.FormatSecondsToHoursMinutesSeconds(challenge.GetRun().GetTotalTime());

            // Show if challenge is completed
            if (challenge.GetDateCompleted() != null) {
                // Completed. todo Image source: https://www.iconfinder.com/icons/308461/accept_success_tick_icon
                image.setImageResource(R.drawable.ic_tick);
            } else {
                // Not completed
                image.setImageResource(R.drawable.ic_challenge);
            }

            title.setText(getString(R.string.challenge_list_activity_challenge_service_notification_content_name_prefix_text) + challenge.GetRun().GetUser().GetName());
            content.setText(contentText);
            date.setText(MiscHelper.FormatDateForDisplay(challenge.GetDateCreated()));

            // Set title to be bold on unread challenges
            if (!challenge.IsRead()) {
                title.setTypeface(null, Typeface.BOLD);
            }

            return convertView;
        }

        // Clear the contents of the list view
        private void clear() {
            challengeArrayList.clear();

            notifyDataSetChanged();
        }

        // Refresh the contents of the list view
        public void refresh(final boolean showCompleted) {
            completedCurrentlyShown = showCompleted;

            // Get challenges
            new HttpHelper.GetChallenges((Preferences.GetLoggedInUser(ChallengeListActivity.this)).GetId(), false) {
                // Called after the background task finishes
                @Override
                protected void onPostExecute(String jsonResult) {
                    // Clear the list view
                    clear();

                    // Check server connection was successful
                    if (jsonResult != null) {
                        // Challenges retrieved successfully
                        challengeArrayList = JsonHelper.GetChallenges(jsonResult, showCompleted);

                        // Determine if this is the first time the method has been called
                        if (firstDisplay) {
                            // Set main layout
                            setContentView(R.layout.activity_challenge_list);

                            // Add refresh listener to swipe layout. Source: http://antonioleiva.com/swiperefreshlayout/
                            swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.challenge_list_swipe_container);
                            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                // Handler for when a swipe triggers a refresh
                                @Override
                                public void onRefresh() {
                                    refresh(completedCurrentlyShown);
                                }
                            });

                            // Populate the list view with challenge list items. Source: http://www.codelearn.org/android-tutorial/android-listview
                            final ListView listView = (ListView) findViewById(R.id.challenge_list_view);
                            listView.addHeaderView(MiscHelper.CreateListViewHeader(ChallengeListActivity.this, getString(R.string.challenge_list_activity_header)), null, false);
                            listView.setEmptyView(findViewById(R.id.empty_list_item));
                            listView.setAdapter(challengeListViewAdaptor);
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                // Handler for when an item in the list view is pressed
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    // Get pressed challenge, compensating for header
                                    Challenge challenge = getItem(position - 1);

                                    // Open challenge
                                    Intent intent = new Intent(ChallengeListActivity.this, ChallengeViewActivity.class);
                                    intent.putExtra(Challenge.CHALLENGE_GSON, new Gson().toJson(challenge));
                                    startActivity(intent);
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
                        // Error retrieving challenges
                        // Set error layout
                        setContentView(R.layout.activity_connection_error);
                    }
                }
            }.execute();
        }
    }
}