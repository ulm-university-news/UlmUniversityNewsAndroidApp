package ulm.university.news.app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.ModeratorAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Moderator;
import ulm.university.news.app.manager.database.ModeratorDatabaseManager;
import ulm.university.news.app.util.TextInputLabels;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.ACCOUNT_NAME_PATTERN;
import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;
import static ulm.university.news.app.util.Constants.MODERATOR_DELETED;
import static ulm.university.news.app.util.Constants.MODERATOR_LOCKED;
import static ulm.university.news.app.util.Constants.MODERATOR_NOT_FOUND;
import static ulm.university.news.app.util.Constants.MODERATOR_UNAUTHORIZED;

public class LoginActivity extends AppCompatActivity {
    /** This classes tag for logging. */
    private static final String TAG = "LoginActivity";

    private TextInputLabels tilName;
    private TextInputLabels tilPassword;
    private TextView tvInfo;
    private TextView tvError;
    private ProgressBar pgrLogin;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_login_toolbar);
        setSupportActionBar(toolbar);

        initView();
    }

    private void initView() {
        tilName = (TextInputLabels) findViewById(R.id.activity_login_til_name);
        tilPassword = (TextInputLabels) findViewById(R.id.activity_login_til_password);
        tvInfo = (TextView) findViewById(R.id.activity_login_tv_info);
        tvError = (TextView) findViewById(R.id.activity_login_tv_error);
        pgrLogin = (ProgressBar) findViewById(R.id.activity_login_pgr_login);
        btnLogin = (Button) findViewById(R.id.activity_login_btn_login);

        tilName.setNameAndHint(getString(R.string.activity_login_til_name_hint));
        tilName.setLength(3, 35);
        tilName.setPattern(ACCOUNT_NAME_PATTERN);
        tilPassword.setNameAndHint(getString(R.string.activity_login_til_password_hint));
        tilPassword.setLength(1,20);
        tilPassword.setToPasswordField();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptToLogin();
            }
        });
    }

    private void attemptToLogin() {
        boolean valid = true;
        if (!Util.getInstance(this).isOnline()) {
            tvError.setText(getString(R.string.general_error_no_connection));
            tvError.setVisibility(View.VISIBLE);
            valid = false;
        }
        if (!tilName.isValid()) {
            valid = false;
        }
        if (!tilPassword.isValid()) {
            valid = false;
        }

        if (valid) {
            // All checks passed. Attempt to attemptToLogin.
            tvError.setVisibility(View.GONE);
            pgrLogin.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.GONE);

            String password = tilPassword.getText();
            password = Util.hashPassword(password);
            ModeratorAPI.getInstance(this).login(tilName.getText(), password);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * This method will be called when a Moderator is posted to the EventBus.
     *
     * @param moderator The Moderator object.
     */
    public void onEventMainThread(Moderator moderator) {
        Log.d(TAG, "EventBus: Moderator");
        Log.d(TAG, moderator.toString());
        login(moderator);
    }

    private void login(Moderator moderator) {
        ModeratorDatabaseManager moderatorDBM = new ModeratorDatabaseManager(this);

        // Check if already stored in database.
        if (moderatorDBM.getModerator(moderator.getId()) == null) {
            moderatorDBM.storeModerator(moderator);
        } else {
            moderatorDBM.updateModerator(moderator);
        }

        // Cache the logged in moderator.
        Util.getInstance(this).setLoggedInModerator(moderator);
        Util.getInstance(this).setCurrentAccessToken();

        // Tell MainActivity to finish.
        Intent intent = new Intent("finish_activity");
        sendBroadcast(intent);

        // Go to moderator view.
        intent = new Intent(this, ModeratorMainActivity.class);
        // Prevent back navigation to logged out state.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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
     * Handles the server error and shows appropriate error message.
     *
     * @param se The error which occurred on the server.
     */
    public void handleServerError(ServerError se) {
        Log.d(TAG, se.toString());
        // Update view.
        pgrLogin.setVisibility(View.GONE);
        btnLogin.setVisibility(View.VISIBLE);
        // Show appropriate error message.
        switch (se.getErrorCode()) {
            case CONNECTION_FAILURE:
                tvError.setText(getString(R.string.general_error_connection_failed));
                break;
            case MODERATOR_LOCKED:
                tvError.setText(getString(R.string.activity_login_error_locked));
                break;
            case MODERATOR_DELETED:
                tvError.setText(getString(R.string.activity_login_error_deleted));
                break;
            case MODERATOR_NOT_FOUND:
                // If moderator wasn't found, just treat like unauthorized for security reasons.
            case MODERATOR_UNAUTHORIZED:
                tvError.setText(getString(R.string.activity_login_error_unauthorized));
                break;
        }
        tvError.setVisibility(View.VISIBLE);
    }
}
