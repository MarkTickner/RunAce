package com.mtickner.runningmotivator;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.webkit.WebView;

public class PrivacyPolicyActivity extends ActionBarActivity {

    // Called when the activity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        // Load privacy policy into the web view
        WebView webView = (WebView) findViewById(R.id.webview);
        webView.loadUrl("http://www.mtickner.co.uk/runace-online/privacy-policy.php?inApp=true");

        // Disable vibration and text selection so the user is not aware that it is a web page
        webView.setHapticFeedbackEnabled(false);
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
    }
}