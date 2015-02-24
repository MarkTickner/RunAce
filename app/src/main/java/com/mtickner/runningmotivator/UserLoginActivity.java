package com.mtickner.runningmotivator;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class UserLoginActivity extends ActionBarActivity {

    private boolean formIsValid = true;
    private ProgressDialog loggingInProgressDialog;

    // Called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        // Client-side validation
        SetupFormValidation();
    }

    // Method which is called when the 'Login' button is pressed
    public void LoginUser(View view) {
        try {
            // Hide the soft keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (NullPointerException e) {
            // Keyboard not visible
            e.printStackTrace();
        }

        // Get user data
        final EditText email = ((EditText) findViewById(R.id.email));
        final EditText password = ((EditText) findViewById(R.id.password));

        // Validate form
        ValidateEmail(email);
        ValidatePassword(password);

        // Check if form is valid
        if (formIsValid) {
            // Form is valid
            // Display progress dialog to user
            progressHandler.postDelayed(progressRunnable, 500);

            // Login user
            new HttpHelper.LoginUser(email.getText().toString(), password.getText().toString()) {
                // Called after the background task finishes
                @Override
                protected void onPostExecute(String jsonResult) {
                    // Dismiss progress dialog
                    progressHandler.removeCallbacks(progressRunnable);
                    if (loggingInProgressDialog != null) {
                        loggingInProgressDialog.dismiss();
                    }

                    // Check server connection was successful
                    if (jsonResult != null) {
                        // Authentication check was successful
                        if ((loggedInUser = JsonHelper.GetUserAfterLogin(jsonResult)) != null) {
                            // Authentication was successful
                            // Login user
                            Preferences.SetLoggedInUser(UserLoginActivity.this, loggedInUser);

                            // Direct to the main activity
                            Intent intent = new Intent(UserLoginActivity.this, MainActivity.class);
                            startActivity(intent);

                            finish();
                        } else {
                            // Authentication failed
                            // Clear password text
                            ((EditText) findViewById(R.id.password)).setText("");

                            // Set error text
                            ((EditText) findViewById(R.id.password)).setError(getString(R.string.user_login_activity_authentication_error_text));
                        }
                    } else {
                        // Error sending user login
                        // Display error toast to user
                        Toast.makeText(UserLoginActivity.this, getString(R.string.internet_connection_error_message_toast), Toast.LENGTH_LONG).show();
                    }
                }
            }.execute();
        }
    }

    // Method which is called when the 'Reset Password' text is pressed
    public void ResetPassword(View view) {
        // Direct to the password reset activity
        Intent intent = new Intent(this, UserPasswordResetActivity.class);
        startActivity(intent);
    }

    // Method that sets up validation of the form
    private void SetupFormValidation() {
        // Validate email
        final EditText email = ((EditText) findViewById(R.id.email));
        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            // Called when the focus state changes
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    ValidateEmail(email);
                }
            }
        });

        // Validate password
        final EditText password = ((EditText) findViewById(R.id.password));
        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            // Called when the focus state changes
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    ValidatePassword(password);
                }
            }
        });
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

    // Method that validates the password
    private void ValidatePassword(EditText password) {
        if (password.getText().toString().matches("^$|\\s+")) {
            formIsValid = false;
            password.setError(getString(R.string.user_register_activity_login_activity_validation_password_not_entered));
        } else {
            formIsValid = true;
        }
    }

    // Display a progress dialog after a 500ms delay, so it does not show if there is a quick connection. Source: http://stackoverflow.com/a/10947069/1164058
    private final Handler progressHandler = new Handler();
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            loggingInProgressDialog = ProgressDialog.show(UserLoginActivity.this, null, getString(R.string.user_login_activity_logging_in_dialog_text));
        }
    };
}