package com.mtickner.runningmotivator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
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

public class RunListActivity extends ActionBarActivity {

    private ArrayList<Run> runArrayList = new ArrayList<>();
    private final RunListViewAdaptor runListViewAdaptor = new RunListViewAdaptor();
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

        // Load runs
        runListViewAdaptor.refresh();
    }

    // Custom adaptor for the list view
    public class RunListViewAdaptor extends BaseAdapter {
        // Returns the count of data set items
        @Override
        public int getCount() {
            return runArrayList.size();
        }

        // Get the data item associated with the specified position in the data set
        @Override
        public Run getItem(int position) {
            return runArrayList.get(position);
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
                LayoutInflater inflater = (LayoutInflater) RunListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.activity_run_list_item, parent, false);
            }

            Run run = runArrayList.get(position);

            ImageView image = (ImageView) convertView.findViewById(R.id.run_image);
            TextView distanceTime = (TextView) convertView.findViewById(R.id.distance_time);
            TextView pace = (TextView) convertView.findViewById(R.id.pace);
            TextView date = (TextView) convertView.findViewById(R.id.date);

            //todo Image source: https://www.iconfinder.com/icons/296209/action_avatar_go_human_male_man_men_people_person_profile_run_running_running_man_user_users_icon#size=512
            image.setImageResource(R.drawable.ic_run);

            // Output pace in preferred unit
            double paceInMinutesPerKilometre = MiscHelper.CalculatePaceInMinutesPerKilometre(run.GetTotalTime(), run.GetDistanceTotal());

            // Output time and output distance and pace in preferred unit
            if (Preferences.GetSettingDistanceUnit(RunListActivity.this).equals(getString(R.string.run_activity_run_complete_activity_distance_unit_kilometres_placeholder))) {
                // Kilometres
                distanceTime.setText(MiscHelper.FormatDouble(run.GetDistanceTotal()) + " " + getString(R.string.run_activity_run_complete_activity_distance_unit_kilometres_placeholder) + " " + getString(R.string.run_list_activity_list_item_title_separator) + " " + MiscHelper.FormatSecondsToHoursMinutesSeconds(run.GetTotalTime()));

                // Minutes per kilometre
                pace.setText(getString(R.string.run_activity_run_complete_activity_pace_title) + " " + MiscHelper.FormatDouble(paceInMinutesPerKilometre) + " " + getString(R.string.run_activity_run_complete_activity_pace_distance_unit_kilometres_placeholder));
            } else {
                // Miles
                distanceTime.setText(MiscHelper.FormatDouble(MiscHelper.ConvertKilometresToMiles(run.GetDistanceTotal())) + " " + getString(R.string.run_activity_run_complete_activity_distance_unit_miles_placeholder) + " " + getString(R.string.run_list_activity_list_item_title_separator) + " " + MiscHelper.FormatSecondsToHoursMinutesSeconds(run.GetTotalTime()));

                // Minutes per mile
                pace.setText(getString(R.string.run_activity_run_complete_activity_pace_title) + " " + MiscHelper.FormatDouble(MiscHelper.ConvertMinutesPerKilometreToMinutesPerMile(paceInMinutesPerKilometre)) + " " + getString(R.string.run_activity_run_complete_activity_pace_distance_unit_miles_placeholder));
            }

            date.setText(MiscHelper.FormatDateForDisplay(run.GetDateRun()));

            return convertView;
        }

        // Clear the contents of the list view
        private void clear() {
            runArrayList.clear();

            notifyDataSetChanged();
        }

        // Refresh the contents of the list view
        public void refresh() {
            // Get runs
            new HttpHelper.GetRuns((Preferences.GetLoggedInUser(RunListActivity.this)).GetId()) {
                // Called after the background task finishes
                @Override
                protected void onPostExecute(String jsonResult) {
                    // Clear the list view
                    clear();

                    runArrayList = JsonHelper.GetRuns(jsonResult);

                    // Determine if this is the first time the method has been called
                    if (firstDisplay) {
                        // Set main layout
                        setContentView(R.layout.activity_run_list);

                        // Add refresh listener to swipe layout. Source: http://antonioleiva.com/swiperefreshlayout/
                        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.run_list_swipe_container);
                        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                            // Handler for when a swipe triggers a refresh
                            @Override
                            public void onRefresh() {
                                refresh();
                            }
                        });

                        // Populate the list view with run list items. Source: http://www.codelearn.org/android-tutorial/android-listview
                        final ListView listView = (ListView) findViewById(R.id.run_list_view);
                        listView.setEmptyView(findViewById(R.id.empty_list_item));
                        listView.setAdapter(runListViewAdaptor);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            // Handler for when an item in the list view is pressed
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                // Get pressed run, compensating for header
                                Run run = getItem(position - 1);

                                // Open challenge friend
                                Intent intent = new Intent(RunListActivity.this, ChallengeFriendActivity.class);
                                intent.putExtra(Run.RUN_GSON, new Gson().toJson(run));
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
                        listView.addHeaderView(MiscHelper.CreateListViewHeader(RunListActivity.this, getString(R.string.run_list_activity_header)), null, false);

                        firstDisplay = false;
                    } else {
                        notifyDataSetChanged();

                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }.execute();
        }
    }
}