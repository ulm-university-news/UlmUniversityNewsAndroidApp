package ulm.university.news.app.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import ulm.university.news.app.Constants;
import ulm.university.news.app.R;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.api.UserAPI;
import ulm.university.news.app.data.User;
import ulm.university.news.app.data.enums.Platform;
import ulm.university.news.app.manager.push.RegistrationIntentService;

public class CreateAccountActivity extends Activity {

    /** This classes tag for logging. */
    private static final String LOG_TAG = "CreateAccountActivity";

    /** An instance of the UserAPI class. */
    private UserAPI userAPI = new UserAPI(this);

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
                        createUserAccount(pushToken);
                    } else {
                        // Couldn't create push token. User should try again.
                        tvInfo.setVisibility(View.GONE);
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText(getString(R.string.token_error_message));
                        btnCreateAccount.setVisibility(View.VISIBLE);
                        pgrCreateAccount.setVisibility(View.GONE);
                    }
                }
            }
        };
    }

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

    private void createUserAccount(String pushToken) {
        String name = etUserName.getText().toString();
        Platform platform = Platform.ANDROID;

        User user = new User(name, pushToken, platform);

        // Sends POST /user
        userAPI.createUser(user);
    }

    /**
     * This method is called by UserAPI after after server response.
     *
     * @param user The created user object retrieved from server.
     */
    public void createUserAccount(User user) {
        // Update view.
        chkTermsOfUse.setVisibility(View.GONE);
        etUserName.setVisibility(View.GONE);
        pgrCreateAccount.setVisibility(View.GONE);
        // Account successfully created. Show button to start main application.
        btnStart.setVisibility(View.VISIBLE);
        tvInfo.setText(getString(R.string.activity_create_account_s_account_created));

        Log.d(LOG_TAG, "User created: " + user.toString());
    }

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
            tvError.setText(getString(R.string.activity_create_account_s_error_name_empty));
        } else {
            // Checks passed. Attempt to create user account.
            btnCreateAccount.setVisibility(View.GONE);
            pgrCreateAccount.setVisibility(View.VISIBLE);
            if (checkPlayServices()) {
                tvInfo.setVisibility(View.VISIBLE);
                tvInfo.setText(getString(R.string.activity_create_account_s_creating_account));
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }
    }

    public void handleServerError(ServerError se) {
        // Update view.
        pgrCreateAccount.setVisibility(View.GONE);
        btnCreateAccount.setVisibility(View.VISIBLE);
        tvInfo.setVisibility(View.GONE);
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(se.toString());
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
}
