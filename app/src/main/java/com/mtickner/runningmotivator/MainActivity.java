package com.mtickner.runningmotivator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends Activity {
    // Called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set privacy policy hyperlink
        TextView privacyPolicy = (TextView) findViewById(R.id.privacy_policy);
        privacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());
        privacyPolicy.setText(Html.fromHtml(privacyPolicy.getText().toString()));
    }

    // Called when the activity starts interacting with the user
    @Override
    protected void onResume() {
        User loggedInUser = Preferences.GetLoggedInUser(this);

        // Detect if user is logged in
        if (loggedInUser != null) {
            // User is logged in, direct to the home activity
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);

            finish();
        }

        super.onResume();
    }

    // Handler for the 'Register' button
    public void GoToRegister(View view) {
        Intent intent = new Intent(this, UserRegisterActivity.class);
        startActivity(intent);
    }

    // Handler for the 'Login' button
    public void GoToLogin(View view) {
        Intent intent = new Intent(this, UserLoginActivity.class);
        startActivity(intent);
    }
}