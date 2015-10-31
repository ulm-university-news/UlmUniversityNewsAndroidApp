package ulm.university.news.app.controller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import ulm.university.news.app.R;
import ulm.university.news.app.util.Constants;


public class StartActivity extends Activity {

    /** This classes tag for logging. */
    private static final String LOG_TAG = "StartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                startAppropriateActivity();
            }
        }, 2000);
    }

    private void startAppropriateActivity() {
        // TODO Replace shared preferences with SQLite database.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean sentToken = sharedPreferences.getBoolean(Constants.SENT_TOKEN_TO_SERVER, false);

        overridePendingTransition(0, 0);
        Intent intent;

        Log.d(LOG_TAG, "sentToken: " + sentToken);

        if (sentToken) {
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
