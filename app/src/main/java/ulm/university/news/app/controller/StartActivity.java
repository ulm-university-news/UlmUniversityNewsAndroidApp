package ulm.university.news.app.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import ulm.university.news.app.R;
import ulm.university.news.app.util.Util;


public class StartActivity extends Activity {
    /** This classes tag for logging. */
    private static final String TAG = "StartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                startAppropriateActivity();
            }
        }, 1500);
    }

    private void startAppropriateActivity() {
        // Show no activity change animation.
        overridePendingTransition(0, 0);
        Intent intent;

        // Check if a local user account already exists.
        if (Util.getInstance(this).getUserAccessToken() != null) {
            // Push token was already created and sent to the server.
            intent = new Intent(this, MainActivity.class);
        } else {
            // Create a new user account with push token.
            intent = new Intent(this, CreateAccountActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
