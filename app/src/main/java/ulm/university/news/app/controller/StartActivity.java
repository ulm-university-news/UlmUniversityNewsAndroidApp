package ulm.university.news.app.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Moderator;
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

        // TODO Remove this code later. Used for development to auto login as moderator.
        Moderator loggedInModerator = new Moderator();
        loggedInModerator.setServerAccessToken("510e4f3dafa2568c59d94787030292f81a37e5a4baf6a727cd5274db79d0b17d");
        loggedInModerator.setId(1);
        loggedInModerator.setName("mmak");
        loggedInModerator.setFirstName("Matthias");
        loggedInModerator.setLastName("Mak");
        // Util.getInstance(this).setLoggedInModerator(loggedInModerator);
        // TODO Remove this code later. Used for development to auto login as moderator.

        if (Util.getInstance(this).getLoggedInModerator() != null) {
            // User is logged in as local moderator.
            intent = new Intent(this, ModeratorMainActivity.class);
        } else if (Util.getInstance(this).getLocalUser() != null) {
            // A local user account already exists.
            intent = new Intent(this, MainActivity.class);
        } else {
            // No account exists. Create a new user account with push token.
            intent = new Intent(this, CreateAccountActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
