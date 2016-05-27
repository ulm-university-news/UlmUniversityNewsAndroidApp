package ulm.university.news.app.controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.api.UserAPI;
import ulm.university.news.app.data.LocalUser;
import ulm.university.news.app.data.User;
import ulm.university.news.app.manager.database.UserDatabaseManager;
import ulm.university.news.app.util.Constants;
import ulm.university.news.app.util.TextInputLabels;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;
import static ulm.university.news.app.util.Constants.USER_DATA_INCOMPLETE;
import static ulm.university.news.app.util.Constants.USER_NAME_INVALID;
import static ulm.university.news.app.util.Constants.USER_PUSH_TOKEN_INVALID;

public class SettingsUserActivity extends AppCompatActivity {

    // GUI elements.
    private ProgressBar pgrEditName;
    private TextView tvError;
    private TextView tvInfo;
    private Button btnEdit;
    private TextInputLabels tilUserName;

    LocalUser localUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_user);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_settings_user_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        localUser = Util.getInstance(this).getLocalUser();
        initGUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    /**
     * Initialises all view elements of this activity.
     */
    private void initGUI() {
        pgrEditName = (ProgressBar) findViewById(R.id.activity_settings_user_pgr_edit_name);
        tvError = (TextView) findViewById(R.id.activity_settings_user_tv_error);
        tvInfo = (TextView) findViewById(R.id.activity_settings_user_tv_info);
        btnEdit = (Button) findViewById(R.id.activity_settings_user_btn_edit);
        tilUserName = (TextInputLabels) findViewById(R.id.activity_settings_user_til_name);

        tilUserName.setNameAndHint(getString(R.string.activity_create_account_name_hint));
        tilUserName.setLength(3, 35);
        tilUserName.setPattern(Constants.ACCOUNT_NAME_PATTERN);
        tilUserName.setText(localUser.getName());

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvError.setVisibility(View.GONE);
                editName();
            }
        });

        String info = String.format(getString(R.string.activity_settings_user_info), localUser.getName());
        tvInfo.setText(info);
    }

    /**
     * Validates user input, performs further checks and finally attempts to change the user name.
     */
    private void editName() {
        boolean valid = true;
        // Check if user name is valid.
        if (!tilUserName.isValid()) {
            valid = false;
        }
        // Check if device is connected to the internet.
        if (!Util.getInstance(this).isOnline()) {
            tvError.setText(getString(R.string.general_error_no_connection));
            tvError.setVisibility(View.VISIBLE);
            // Check if user name has changed.
        } else if (tilUserName.getText().equals(localUser.getName())) {
            tvError.setText(getString(R.string.user_name_not_changed));
            tvError.setVisibility(View.VISIBLE);
        } else if (valid) {
            // Checks passed. Attempt to update local user account on the server.
            btnEdit.setVisibility(View.GONE);
            pgrEditName.setVisibility(View.VISIBLE);
            localUser.setName(tilUserName.getText());
            // Send new local user name to the server.
            UserAPI.getInstance(this).updateLocalUser(localUser);
        }
    }

    /**
     * This method will be called when a local user is posted to the EventBus.
     *
     * @param localUser The created localUser object retrieved from server.
     */
    public void onEventMainThread(LocalUser localUser) {
        this.localUser = localUser;
        pgrEditName.setVisibility(View.GONE);
        btnEdit.setVisibility(View.VISIBLE);
        // Update local user object in database.
        UserDatabaseManager userDBM = new UserDatabaseManager(this);
        userDBM.updateLocalUser(localUser);
        // Update the cached local user.
        Util.getInstance(this).setLocalUser(localUser);
        // TODO Update local user as normal user in database.
        User user = userDBM.getUser(localUser.getId());
        user.setOldName(user.getName());
        user.setName(localUser.getName());
        userDBM.updateUser(user);
        // Update new user name in info text.
        String info = String.format(getString(R.string.activity_settings_user_info), localUser.getName());
        tvInfo.setText(info);
        // Show updated message.
        Toast toast = Toast.makeText(this, getString(R.string.user_edited), Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);
        toast.show();
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
        // Update view.
        pgrEditName.setVisibility(View.GONE);
        btnEdit.setVisibility(View.VISIBLE);
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
        }
        tvError.setVisibility(View.VISIBLE);
    }
}
