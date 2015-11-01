package ulm.university.news.app.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import ulm.university.news.app.R;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.api.UserAPI;
import ulm.university.news.app.data.LocalUser;
import ulm.university.news.app.data.enums.Platform;
import ulm.university.news.app.manager.database.UserDatabaseManager;
import ulm.university.news.app.manager.push.PushTokenGenerationService;
import ulm.university.news.app.util.Constants;
import ulm.university.news.app.util.exceptions.DatabaseException;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;
import static ulm.university.news.app.util.Constants.USER_DATA_INCOMPLETE;
import static ulm.university.news.app.util.Constants.USER_NAME_INVALID;
import static ulm.university.news.app.util.Constants.USER_PUSH_TOKEN_INVALID;

public class CreateAccountActivity extends AppCompatActivity {

    /** This classes tag for logging. */
    private static final String LOG_TAG = "CreateAccountActivity";

    /** An instance of the UserAPI class. */
    private UserAPI userAPI = new UserAPI(this);

    /** The code for the play service request. */
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /** This BroadcastReceiver listens for push token creation events. */
    private BroadcastReceiver pushTokenBR;

    // GUI elements.
    private ProgressBar pgrCreateAccount;
    private TextView tvWelcome;
    private TextView tvInfo;
    private TextView tvError;
    private Button btnCreateAccount;
    private Button btnStart;
    private EditText etUserName;
    private CheckBox chkTermsOfUse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Initialise GUI elements.
        initGUI();

        // Listens to broadcast messages by PushTokenGenerationService.
        pushTokenBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Proceed account creation with received push token.
                if (intent.getAction().equals(Constants.PUSH_TOKEN_CREATED)) {
                    // Get created push token.
                    String pushToken = intent.getExtras().getString("pushToken");
                    if (pushToken != null) {
                        // Push token successfully created. Proceed with account creation.
                        Log.d(LOG_TAG, "Push token retrieved. Proceed with account creation.");
                        createLocalUser(pushToken);
                    } else {
                        // Couldn't create push token. LocalUser should try again.
                        tvInfo.setVisibility(View.GONE);
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText(getString(R.string.activity_create_account_s_error_token));
                        btnCreateAccount.setVisibility(View.VISIBLE);
                        pgrCreateAccount.setVisibility(View.GONE);
                    }
                }
            }
        };
    }

    /**
     * Initialises all view elements of this activity.
     */
    private void initGUI() {
        pgrCreateAccount = (ProgressBar) findViewById(R.id.activity_create_account_pgr_create_account);
        tvWelcome = (TextView) findViewById(R.id.activity_create_account_tv_welcome);
        tvInfo = (TextView) findViewById(R.id.activity_create_account_tv_info);
        tvError = (TextView) findViewById(R.id.activity_create_account_tv_error);
        btnCreateAccount = (Button) findViewById(R.id.activity_create_account_btn_create_account);
        btnStart = (Button) findViewById(R.id.activity_create_account_btn_start);
        etUserName = (EditText) findViewById(R.id.activity_create_account_et_user_name);
        chkTermsOfUse = (CheckBox) findViewById(R.id.activity_create_account_chk_terms_of_use);

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvInfo.setVisibility(View.GONE);
                tvError.setVisibility(View.GONE);
                createPushToken();
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(pushTokenBR,
                new IntentFilter(Constants.PUSH_TOKEN_CREATED));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushTokenBR);
        super.onPause();
    }

    /**
     * Creates a new user object and attempts to send it to the server.
     *
     * @param pushToken The generated push access token of the new user.
     */
    private void createLocalUser(String pushToken) {
        String name = etUserName.getText().toString();
        Platform platform = Platform.ANDROID;

        LocalUser localUser = new LocalUser(name, pushToken, platform);

        // Sends POST /localUser
        userAPI.createLocalUser(localUser);
    }

    /**
     * This method is called by UserAPI after a server response.
     *
     * @param localUser The created localUser object retrieved from server.
     */
    public void createLocalUser(LocalUser localUser) {
        // Store localUser in database.
        UserDatabaseManager userDBM = new UserDatabaseManager(this);
        try {
            userDBM.storeLocalUser(localUser);
        } catch (DatabaseException e) {
            pgrCreateAccount.setVisibility(View.GONE);
            btnCreateAccount.setVisibility(View.VISIBLE);
            tvError.setText(getString(R.string.general_s_error_database));
            tvError.setVisibility(View.VISIBLE);
            return;
        }
        // Update view.
        tvWelcome.setVisibility(View.GONE);
        chkTermsOfUse.setVisibility(View.GONE);
        etUserName.setVisibility(View.GONE);
        pgrCreateAccount.setVisibility(View.GONE);
        // Account successfully created. Show button to start main application.
        btnStart.setVisibility(View.VISIBLE);
        tvInfo.setText(getString(R.string.activity_create_account_s_account_created));
    }

    /**
     * Validates user input, performs further checks and finally starts generation of a new push token for the new user.
     */
    private void createPushToken() {
        // Check if device is connected to the internet.
        if (!isOnline()) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText(getString(R.string.activity_create_account_s_error_offline));
            // Check if terms of use are accepted.
        } else if (!chkTermsOfUse.isChecked()) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText(getString(R.string.activity_create_account_s_error_checkbox));
            // Check if user name is empty.
        } else if (etUserName.getText().toString().trim().length() == 0) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText(getString(R.string.user_s_error_name_empty));
        } else {
            // Checks passed. Attempt to create user account.
            btnCreateAccount.setVisibility(View.GONE);
            pgrCreateAccount.setVisibility(View.VISIBLE);
            if (checkPlayServices()) {
                tvInfo.setVisibility(View.VISIBLE);
                tvInfo.setText(getString(R.string.activity_create_account_s_creating_account));
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(this, PushTokenGenerationService.class);
                startService(intent);
            }
        }
    }

    /**
     * Handles the server error and shows appropriate error message.
     *
     * @param se The error which occurred on the server.
     */
    public void handleServerError(ServerError se) {
        // Update view.
        pgrCreateAccount.setVisibility(View.GONE);
        btnCreateAccount.setVisibility(View.VISIBLE);
        tvInfo.setVisibility(View.GONE);
        // Show appropriate error message.
        switch (se.getErrorCode()) {
            case CONNECTION_FAILURE:
                tvError.setText(getString(R.string.general_s_error_connection));
                break;
            case USER_DATA_INCOMPLETE:
                tvError.setText(getString(R.string.user_s_error_name_empty));
                break;
            case USER_NAME_INVALID:
                tvError.setText(getString(R.string.user_s_error_name_invalid));
                break;
            case USER_PUSH_TOKEN_INVALID:
                tvError.setText(getString(R.string.general_s_error_something));
                break;
        }
        tvError.setVisibility(View.VISIBLE);
    }

    /**
     * Checks if the device has a connection to the internet.
     *
     * @return true if devices is connected to the internet.
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Checks the device to make sure it has the Google Play Services APK. If it doesn't, display a dialog that
     * allows users to download the APK from the Google Play Store or enable it in the device's system settings.
     *
     * @return true if Google Play Services are available.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.e(LOG_TAG, "This device is not supported for Google Play Services.");
                finish();
            }
            return false;
        }
        return true;
    }
}
