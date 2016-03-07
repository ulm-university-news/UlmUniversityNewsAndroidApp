package ulm.university.news.app.controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Settings;
import ulm.university.news.app.data.enums.NotificationSettings;
import ulm.university.news.app.manager.database.SettingsDatabaseManager;

public class SettingsNotificationActivity extends AppCompatActivity {

    Settings settings;
    SettingsDatabaseManager settingsDBM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_notification);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_settings_notification_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        settingsDBM = new SettingsDatabaseManager(this);
        settings = settingsDBM.getSettings();
        initView();
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

    private void initView() {
        RadioGroup rgSettings = (RadioGroup) findViewById(R.id.activity_settings_notification_rg_settings);
        RadioButton rbAll = (RadioButton) findViewById(R.id.activity_settings_notification_rb_all);
        RadioButton rbPriority = (RadioButton) findViewById(R.id.activity_settings_notification_rb_priority);
        RadioButton rbNone = (RadioButton) findViewById(R.id.activity_settings_notification_rb_none);

        switch (settings.getNotificationSettings()) {
            case ALL:
                rbAll.setChecked(true);
                break;
            case PRIORITY:
                rbPriority.setChecked(true);
                break;
            default:
                rbNone.setChecked(true);
        }

        rgSettings.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onRadioButtonClicked(checkedId);
            }
        });
    }

    private void onRadioButtonClicked(int checkedId) {
        // Check which radio button is selected.
        switch (checkedId) {
            case R.id.activity_settings_notification_rb_all:
                settings.setNotificationSettings(NotificationSettings.ALL);
                break;
            case R.id.activity_settings_notification_rb_priority:
                settings.setNotificationSettings(NotificationSettings.PRIORITY);
                break;
            case R.id.activity_settings_notification_rb_none:
                settings.setNotificationSettings(NotificationSettings.NONE);
                break;
        }
        // Update settings in the database.
        settingsDBM.updateSettings(settings);

        // Show updated message.
        Toast toast = Toast.makeText(this, getString(R.string.general_settings_updated), Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);
        toast.show();
    }
}
