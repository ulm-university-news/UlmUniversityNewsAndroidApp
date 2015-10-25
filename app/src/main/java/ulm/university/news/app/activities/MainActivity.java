package ulm.university.news.app.activities;

import android.app.Activity;
import android.os.Bundle;

import ulm.university.news.app.R;

public class MainActivity extends Activity {

    /** This classes tag for logging. */
    private static final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

}
