package com.mtickner.runningmotivator;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import static com.mtickner.runningmotivator.HttpHelper.urlPrefix;

public class UserLoginActivity extends ActionBarActivity {

    private static final String TAG = "UserLoginActivity";
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

    // Called when the activity has detected the user's press of the back key
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // Method which is called when the 'Login' button is pressed
    public void LoginUser(View view) {
        // TODO crash when logging in with no internet connection

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

            final String postUri = urlPrefix + "user-login.php";

            // Login user
            RequestQueue queue = Volley.newRequestQueue(UserLoginActivity.this);
            queue.add(new StringRequest(Method.POST, postUri, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, response);

                    // Dismiss progress dialog
                    progressHandler.removeCallbacks(progressRunnable);
                    if (loggingInProgressDialog != null) {
                        loggingInProgressDialog.dismiss();
                    }

                    // Check server connection was successful
                    if (response != null) {
                        // Authentication check was successful
                        User loggedInUser;
                        if ((loggedInUser = JsonHelper.GetUserAfterLogin(response)) != null) {
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
                            ((EditText) findViewById(R.id.password)).setError(ErrorCodes.GetErrorMessage(UserLoginActivity.this, 304));
                        }
                    } else {
                        // Error sending user login
                        // Display error toast to user
                        Toast.makeText(UserLoginActivity.this, ErrorCodes.GetErrorMessage(UserLoginActivity.this, 102), Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, error.getLocalizedMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("requestFromApplication", "true");
                    params.put("email", email.getText().toString());
                    params.put("password", password.getText().toString());

                    return params;
                }
            });
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
            email.setError(ErrorCodes.GetErrorMessage(this, 301));
        } else if (!email.getText().toString().matches("^([\\w\\.\\-])+@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$")) {
            formIsValid = false;
            email.setError(ErrorCodes.GetErrorMessage(this, 302));
        } else {
            formIsValid = true;
        }
    }

    // Method that validates the password
    private void ValidatePassword(EditText password) {
        if (password.getText().toString().matches("^$|\\s+")) {
            formIsValid = false;
            password.setError(ErrorCodes.GetErrorMessage(this, 303));
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