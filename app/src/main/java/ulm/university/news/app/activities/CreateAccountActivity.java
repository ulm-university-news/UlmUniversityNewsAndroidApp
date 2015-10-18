package ulm.university.news.app.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import ulm.university.news.app.Constants;
import ulm.university.news.app.R;
import ulm.university.news.app.data.enums.Platform;
import ulm.university.news.app.manager.push.RegistrationIntentService;

public class CreateAccountActivity extends Activity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String LOG_TAG = "CreateAccountActivity";

    private BroadcastReceiver registrationBroadcastReceiver;

    // GUI elements.
    private ProgressBar pgrCreateAccount;
    private TextView tvInformation;
    private Button btnCreateAccount;
    private EditText etUserName;
    private CheckBox chkTermsOfUse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Initialise GUI elements.
        initGUI();

        registrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Proceed account creation with received push token.
                if (intent.getAction().equals(Constants.PUSH_TOKEN_CREATED)) {
                    // Get created push token.
                    String pushToken = intent.getExtras().getString("pushToken");
                    if (pushToken != null) {
                        // Push token successfully created. Proceed with account creation.
                        tvInformation.setText(getString(R.string.gcm_send_message));
                        createUserAccount(pushToken);
                    } else {
                        // Couldn't create push token. Try again.
                        tvInformation.setText(getString(R.string.token_error_message));
                        btnCreateAccount.setVisibility(View.VISIBLE);
                        pgrCreateAccount.setVisibility(View.GONE);
                    }
                }
            }
        };
    }

    private void initGUI() {
        pgrCreateAccount = (ProgressBar) findViewById(R.id.activity_create_account_pgr_create_account);
        tvInformation = (TextView) findViewById(R.id.activity_create_account_tv_information);
        btnCreateAccount = (Button) findViewById(R.id.activity_create_account_btn_create_account);
        etUserName = (EditText) findViewById(R.id.activity_create_account_et_user_name);
        chkTermsOfUse = (CheckBox) findViewById(R.id.activity_create_account_chk_terms_of_use);

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPushToken();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(registrationBroadcastReceiver,
                new IntentFilter(Constants.PUSH_TOKEN_CREATED));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(registrationBroadcastReceiver);
        super.onPause();
    }

    /**
     * Checks the device to make sure it has the Google Play Services APK. If it doesn't, display a dialog that
     * allows users to download the APK from the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LOG_TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void createUserAccount(String pushToken) {
        String name = etUserName.getText().toString();
        Platform platform = Platform.ANDROID;
        // TODO Create User object with name, platform and pushToken.
        // TODO Send POST /user via API.
        /*
        Make view elements invisible if account was created successfully. Only if info text should be shown here and
        main activity should be started after button press.
        chkTermsOfUse.setVisibility(View.GONE);
        etUserName.setVisibility(View.GONE);
        pgrCreateAccount.setVisibility(View.GONE);
        */

        // Account successfully created. Start main application.
        Toast tCreated = Toast.makeText(this, LOG_TAG, Toast.LENGTH_LONG);
        tCreated.setText(getString(R.string.activity_create_account_s_account_created));
        tCreated.show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void createPushToken() {
        Toast tError = Toast.makeText(this, LOG_TAG, Toast.LENGTH_SHORT);

        // Check if device is connected to the internet.
        if (!isOnline()) {
            tError.setText(getString(R.string.activity_create_account_s_error_offline));
            tError.show();
            // Check if terms of use are accepted.
        } else if (!chkTermsOfUse.isChecked()) {
            tError.setText(getString(R.string.activity_create_account_s_error_checkbox));
            tError.show();
            // Check if user name is empty.
        } else if (etUserName.getText().toString().trim().length() == 0) {
            tError.setText(getString(R.string.activity_create_account_s_error_name_empty));
            tError.show();
        } else {
            // Checks passed. Attempt to create user account.
            btnCreateAccount.setVisibility(View.GONE);
            pgrCreateAccount.setVisibility(View.VISIBLE);
            if (checkPlayServices()) {
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }
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
