package com.mtickner.runningmotivator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class UserPasswordResetActivity extends ActionBarActivity {

    private boolean formIsValid = true;

    // Called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_password_reset);
    }

    // Called whenever an item in the options menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Respond to the action bar up button
                Intent intent = new Intent(this, UserLoginActivity.class);
                startActivity(intent);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Called when the activity has detected the user's press of the back key
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, UserLoginActivity.class);
        startActivity(intent);
    }

    // Method which is called when the 'Reset Password' button is pressed
    public void ResetPassword(View view) {
        try {
            // Hide the soft keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (NullPointerException e) {
            // Keyboard not visible
            e.printStackTrace();
        }

        // Get user email
        final EditText email = ((EditText) findViewById(R.id.email));

        // Validate email
        ValidateEmail(email);

        // Check if form is valid
        if (formIsValid) {
            // Form is valid
            // Send password reset request
            new HttpHelper.ResetPassword(email.getText().toString()) {
                // Called after the background task finishes
                @Override
                protected void onPostExecute(String jsonResult) {
                    // Check server connection was successful
                    if (JsonHelper.ResultSuccess(jsonResult)) {
                        // Password reset request sent successfully
                        // Display success toast to user
                        Toast.makeText(UserPasswordResetActivity.this, "An email has been sent to " + email, Toast.LENGTH_LONG).show();

                        // Direct to the login activity
                        Intent intent = new Intent(UserPasswordResetActivity.this, UserLoginActivity.class);
                        startActivity(intent);

                        finish();
                    } else {
                        // Error sending password reset request
                        // Display error toast to user
                        Toast.makeText(UserPasswordResetActivity.this, getString(R.string.internet_connection_error_message_toast), Toast.LENGTH_LONG).show();
                    }
                }
            }.execute();
        }
    }

    // Method that validates the email
    private void ValidateEmail(EditText email) {
        if (email.getText().toString().matches("^$|\\s+")) {
            formIsValid = false;
            email.setError(getString(R.string.user_register_activity_login_activity_validation_email_not_entered));
        } else if (!email.getText().toString().matches("^([\\w\\.\\-])+@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$")) {
            formIsValid = false;
            email.setError(getString(R.string.user_register_activity_login_activity_validation_email_not_valid));
        } else {
            formIsValid = true;
        }
    }
}
