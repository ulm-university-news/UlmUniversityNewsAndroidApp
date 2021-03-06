package ulm.university.news.app.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEventChannels;
import ulm.university.news.app.api.ChannelAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.api.UserAPI;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.LocalUser;
import ulm.university.news.app.data.User;
import ulm.university.news.app.data.enums.Platform;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.manager.database.SettingsDatabaseManager;
import ulm.university.news.app.manager.database.UserDatabaseManager;
import ulm.university.news.app.manager.push.PushTokenGenerationService;
import ulm.university.news.app.util.Constants;
import ulm.university.news.app.util.TextInputLabels;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;
import static ulm.university.news.app.util.Constants.USER_DATA_INCOMPLETE;
import static ulm.university.news.app.util.Constants.USER_NAME_INVALID;
import static ulm.university.news.app.util.Constants.USER_PUSH_TOKEN_INVALID;

public class CreateAccountActivity extends AppCompatActivity {
    /** This classes tag for logging. */
    private static final String TAG = "CreateAccountActivity";
    /** The code for the play service request. */
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    /** The broadcast receiver for this activity. */
    private BroadcastReceiver receiver;
    /** This filter accepts broadcasts about push tokens. */
    IntentFilter pushTokenFilter;

    // GUI elements.
    private ProgressBar pgrCreateAccount;
    private TextView tvWelcome;
    private TextView tvInfo;
    private TextView tvError;
    private Button btnCreateAccount;
    private Button btnStart;
    private Button btnRetry;
    private TextInputLabels tilUserName;
    private CheckBox chkTermsOfUse;

    ChannelDatabaseManager channelDBM;
    LocalUser localUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_create_account_toolbar);
        setSupportActionBar(toolbar);

        channelDBM = new ChannelDatabaseManager(this);
        localUser = null;

        // Initialise GUI elements.
        initGUI();
        // Initialise the broadcast receiver.
        initReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, pushTokenFilter);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        // Invalidate local user if creation wasn't completed successfully.
        localUser = null;
        Util.getInstance(this).setLocalUser(null);
        super.onBackPressed();
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
        btnRetry = (Button) findViewById(R.id.activity_create_account_btn_retry);
        tilUserName = (TextInputLabels) findViewById(R.id.activity_create_account_til_name);
        chkTermsOfUse = (CheckBox) findViewById(R.id.activity_create_account_chk_terms_of_use);

        tilUserName.setNameAndHint(getString(R.string.activity_create_account_name_hint));
        tilUserName.setLength(3, 35);
        tilUserName.setPattern(Constants.ACCOUNT_NAME_PATTERN);

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvError.setVisibility(View.GONE);
                createAccount();
            }
        });

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvError.setVisibility(View.GONE);
                btnRetry.setVisibility(View.GONE);
                pgrCreateAccount.setVisibility(View.VISIBLE);
                loadChannelData();
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

    private void initReceiver() {
        pushTokenFilter = new IntentFilter(Constants.PUSH_TOKEN_CREATED);

        // Listens to broadcast messages by PushTokenGenerationService.
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // Proceed account creation with received push token.
                if (Constants.PUSH_TOKEN_CREATED.equals(action)) {
                    // Get created push token.
                    String pushToken = intent.getExtras().getString("pushToken");
                    if (pushToken != null) {
                        // Push token successfully created. Proceed with account creation.
                        Log.d(TAG, "Push token retrieved. Proceed with account creation.");
                        createLocalUser(pushToken);
                    } else {
                        // Couldn't create push token. LocalUser should try again.
                        tvInfo.setVisibility(View.GONE);
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText(getString(R.string.activity_create_account_error_token));
                        btnCreateAccount.setVisibility(View.VISIBLE);
                        pgrCreateAccount.setVisibility(View.GONE);
                    }
                }
            }
        };
    }

    /**
     * This method will be called when a server error is posted to the EventBus.
     *
     * @param serverError The error which occurred on the server.
     */
    public void onEventMainThread(ServerError serverError) {
        handleServerError(serverError);
    }

    /**
     * This method will be called when a local user is posted to the EventBus.
     *
     * @param localUser The created localUser object retrieved from server.
     */
    public void onEventMainThread(LocalUser localUser) {
        this.localUser = localUser;
        // Set the users server access token.
        Util.getInstance(this).setLocalUser(localUser);
        Util.getInstance(this).setCurrentAccessToken();
        // User created successfully. Start loading of channel data.
        loadChannelData();
        // Update view.
        showCreatedView();
    }

    /**
     * This method will be called when a list of channels is posted to the EventBus.
     *
     * @param event The bus event containing a list of channel objects.
     */
    public void onEventMainThread(BusEventChannels event) {
        Log.d(TAG, event.toString());
        // Channels loaded successfully. Save local user in database now.
        new UserDatabaseManager(this).storeLocalUser(localUser);
        // Save local user in normal user database as well so that groups can be joined.
        User user = new User(localUser.getId(), localUser.getName());
        new UserDatabaseManager(this).storeUser(user);
        // Create and store the default settings.
        new SettingsDatabaseManager(this).createDefaultSettings();
        // Store loaded channels in database.
        List<Channel> channels = event.getChannels();
        for (Channel channel : channels) {
            channelDBM.storeChannel(channel);
        }
        // Account successfully created. Show button to start main application.
        btnRetry.setVisibility(View.GONE);
        pgrCreateAccount.setVisibility(View.GONE);
        btnStart.setVisibility(View.VISIBLE);
    }

    private void showCreatedView() {
        tvWelcome.setVisibility(View.GONE);
        chkTermsOfUse.setVisibility(View.GONE);
        tilUserName.setVisibility(View.GONE);
        btnCreateAccount.setVisibility(View.GONE);
        tvInfo.setText(getString(R.string.activity_create_account_account_created));
        tvInfo.setVisibility(View.VISIBLE);
        pgrCreateAccount.setVisibility(View.VISIBLE);
    }

    /**
     * Creates a new user object and attempts to send it to the server.
     *
     * @param pushToken The generated push access token of the new user.
     */
    private void createLocalUser(String pushToken) {
        String name = tilUserName.getText();
        Platform platform = Platform.ANDROID;
        LocalUser localUser = new LocalUser(name, pushToken, platform);
        // Sends the created local user to the server.
        UserAPI.getInstance(this).createLocalUser(localUser);
    }

    /**
     * Validates user input, performs further checks and finally starts generation of a new push token for the new user.
     */
    private void createAccount() {
        boolean valid = true;
        // Check if user name is valid.
        if (!tilUserName.isValid()) {
            valid = false;
        }
        // Check if device is connected to the internet.
        if (!Util.getInstance(this).isOnline()) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText(getString(R.string.general_error_no_connection));
            // Check if terms of use are accepted.
        } else if (!chkTermsOfUse.isChecked()) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText(getString(R.string.activity_create_account_error_checkbox));
        } else if (valid) {
            // Checks passed. Attempt to create user account.
            btnCreateAccount.setVisibility(View.GONE);
            pgrCreateAccount.setVisibility(View.VISIBLE);
            if (checkPlayServices()) {
                tvInfo.setVisibility(View.VISIBLE);
                tvInfo.setText(getString(R.string.activity_create_account_creating_account));
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
        // Check if user account was created already. Display correct view elements.
        if (Util.getInstance(this).getLocalUser() != null) {
            showCreatedView();
            btnRetry.setVisibility(View.VISIBLE);
        } else {
            btnCreateAccount.setVisibility(View.VISIBLE);
        }
        // Show appropriate error message.
        switch (se.getErrorCode()) {
            case CONNECTION_FAILURE:
                tvError.setText(getString(R.string.general_error_connection_failed));
                break;
            case USER_DATA_INCOMPLETE:
                tvError.setText(getString(R.string.activity_create_account_error_name_empty));
                break;
            case USER_NAME_INVALID:
                tvError.setText(getString(R.string.activity_create_account_error_name_invalid));
                break;
            case USER_PUSH_TOKEN_INVALID:
                tvError.setText(getString(R.string.general_error_something));
                break;
            default:
                tvError.setText(getString(R.string.general_error_something));
        }
        tvError.setVisibility(View.VISIBLE);
        pgrCreateAccount.setVisibility(View.GONE);
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
                Log.e(TAG, "This device is not supported for Google Play Services.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Sends an initial request to the server to get all channel data.
     */
    private void loadChannelData() {
        // Channel refresh is only possible if there is an internet connection.
        if (Util.getInstance(this).isOnline()) {
            // Update channels when activity is created. Request all channel data.
            ChannelAPI.getInstance(this).getChannels(null, null);
        } else {
            String errorMessage = getString(R.string.general_error_connection_failed);
            errorMessage += getString(R.string.general_error_refresh);
            tvError.setText(errorMessage);
            tvError.setVisibility(View.VISIBLE);
            btnRetry.setVisibility(View.VISIBLE);
        }
    }
}
