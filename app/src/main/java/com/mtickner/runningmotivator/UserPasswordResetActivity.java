package com.mtickner.runningmotivator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
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

public class UserPasswordResetActivity extends ActionBarActivity {

    private static final String TAG = "PasswordResetActivity";
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

            final String postUri = urlPrefix + "user-password-reset.php";

            RequestQueue queue = Volley.newRequestQueue(UserPasswordResetActivity.this);
            queue.add(new StringRequest(Method.POST, postUri, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, response);

                    // Check server connection was successful
                    if (JsonHelper.ResultSuccess(response)) {
                        // Password reset request sent successfully
                        // Display success toast to user
                        Toast.makeText(UserPasswordResetActivity.this, getString(R.string.user_password_reset_toast_text) + email.getText(), Toast.LENGTH_LONG).show();

                        // Direct to the login activity
                        Intent intent = new Intent(UserPasswordResetActivity.this, UserLoginActivity.class);
                        startActivity(intent);

                        finish();
                    } else {
                        // Error sending password reset request
                        // Display error toast to user
                        Toast.makeText(UserPasswordResetActivity.this, ErrorCodes.GetErrorMessage(UserPasswordResetActivity.this, 102), Toast.LENGTH_LONG).show();
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

                    return params;
                }
            });
        }
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
}
