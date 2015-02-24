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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserRegisterActivity extends ActionBarActivity {

    private boolean formIsValid = true;
    private ProgressDialog registeringProgressDialog;

    // Called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        // Client-side validation
        SetupFormValidation();
    }

    // Method which is called when the 'Register' button is pressed
    public void RegisterUser(View view) {
        try {
            // Hide the soft keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (NullPointerException e) {
            // Keyboard not visible
            e.printStackTrace();
        }

        // Get user data
        final EditText name = ((EditText) findViewById(R.id.name));
        final EditText email = ((EditText) findViewById(R.id.email));
        final EditText password = ((EditText) findViewById(R.id.password));

        // Validate form
        ValidateName(name);
        ValidateEmail(email);
        ValidatePassword(password);

        // Check if form is valid
        if (formIsValid) {
            // Form is valid
            // Display progress dialog to user
            progressHandler.postDelayed(progressRunnable, 500);

            // Register user
            new HttpHelper.RegisterUser(name.getText().toString(), email.getText().toString(), password.getText().toString()) {
                // Called after the background task finishes
                @Override
                protected void onPostExecute(String jsonResult) {
                    // Dismiss progress dialog
                    progressHandler.removeCallbacks(progressRunnable);
                    if (registeringProgressDialog != null) {
                        registeringProgressDialog.dismiss();
                    }

                    // Check server connection was successful
                    if (jsonResult != null) {
                        // Registration sent successfully
                        try {
                            // Create JSON object from server response
                            JSONObject resultObject = new JSONObject(jsonResult);

                            // Get 'Details' array from JSON object
                            JSONArray resultDetailsArray = resultObject.getJSONArray("Details");

                            // Get 'OutputType' from JSON object
                            if (resultObject.getString("OutputType").equals("Success")) {
                                // Registered successfully
                                // Get user ID
                                loggedInUser.SetId(resultDetailsArray.getInt(0));

                                // Login user
                                Preferences.SetLoggedInUser(UserRegisterActivity.this, loggedInUser);

                                // Direct to the main activity
                                Intent intent = new Intent(UserRegisterActivity.this, MainActivity.class);
                                startActivity(intent);

                                finish();
                            } else {
                                // Check if email is already registered
                                for (int i = 0; i < resultDetailsArray.length(); i++) {
                                    if (resultDetailsArray.getString(i).equals("307")) {
                                        // Email is already registered
                                        ((EditText) findViewById(R.id.email)).setError(getString(R.string.user_register_activity_validation_email_already_registered));
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            // Catches exceptions including JSONException when creating JSON objects
                            e.printStackTrace();
                        }


                    } else {
                        // Error sending registration
                        // Display error toast to user
                        Toast.makeText(UserRegisterActivity.this, getString(R.string.internet_connection_error_message_toast), Toast.LENGTH_LONG).show();
                    }
                }
            }.execute();
        }
    }

    // Method that sets up validation of the form
    private void SetupFormValidation() {
        final EditText name = ((EditText) findViewById(R.id.name));
        name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            // Called when the focus state changes
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    // Validate name
                    ValidateName(name);
                }
            }
        });

        final EditText email = ((EditText) findViewById(R.id.email));
        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            // Called when the focus state changes
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    // Validate email
                    ValidateEmail(email);
                }
            }
        });

        final EditText password = ((EditText) findViewById(R.id.password));
        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            // Called when the focus state changes
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    // Validate password
                    ValidatePassword(password);
                }
            }
        });
    }

    // Method that validates the name
    private void ValidateName(EditText name) {
        if (name.getText().toString().matches("^\\s*$")) {
            formIsValid = false;
            name.setError(getString(R.string.user_register_activity_validation_name_not_entered));
        } else {
            formIsValid = true;
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

    // Method that validates the password
    private void ValidatePassword(EditText password) {
        if (password.getText().toString().matches("^$|\\s+")) {
            formIsValid = false;
            password.setError(getString(R.string.user_register_activity_login_activity_validation_password_not_entered));
        } else if (password.length() < 8) {
            formIsValid = false;
            password.setError(getString(R.string.user_register_activity_validation_password_too_short));
        } else {
            formIsValid = true;
        }
    }

    // Display a progress dialog after a 500ms delay, so it does not show if there is a quick connection. Source: http://stackoverflow.com/a/10947069/1164058
    private final Handler progressHandler = new Handler();
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            registeringProgressDialog = ProgressDialog.show(UserRegisterActivity.this, null, getString(R.string.user_register_activity_creating_account_dialog_text));
        }
    };
}