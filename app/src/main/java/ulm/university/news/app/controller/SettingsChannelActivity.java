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
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.enums.NotificationSettings;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.manager.database.SettingsDatabaseManager;

public class SettingsChannelActivity extends AppCompatActivity {

    NotificationSettings notificationSettings;
    SettingsDatabaseManager settingsDBM;
    int channelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set color theme according to channel and lecture type.
        channelId = getIntent().getIntExtra("channelId", 0);
        Channel channel = new ChannelDatabaseManager(this).getChannel(channelId);
        ChannelController.setColorTheme(this, channel);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_channel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_settings_channel_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(channel.getName());
        }

        settingsDBM = new SettingsDatabaseManager(this);
        notificationSettings = settingsDBM.getChannelNotificationSettings(channelId);
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
        RadioGroup rgSettings = (RadioGroup) findViewById(R.id.activity_settings_channel_rg_settings);
        RadioButton rbAll = (RadioButton) findViewById(R.id.activity_settings_channel_rb_all);
        RadioButton rbPriority = (RadioButton) findViewById(R.id.activity_settings_channel_rb_priority);
        RadioButton rbNone = (RadioButton) findViewById(R.id.activity_settings_channel_rb_none);
        RadioButton rbGeneral = (RadioButton) findViewById(R.id.activity_settings_channel_rb_general);

        switch (notificationSettings) {
            case ALL:
                rbAll.setChecked(true);
                break;
            case PRIORITY:
                rbPriority.setChecked(true);
                break;
            case NONE:
                rbNone.setChecked(true);
            default:
                rbGeneral.setChecked(true);
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
            case R.id.activity_settings_channel_rb_all:
                notificationSettings = NotificationSettings.ALL;
                break;
            case R.id.activity_settings_channel_rb_priority:
                notificationSettings = NotificationSettings.PRIORITY;
                break;
            case R.id.activity_settings_channel_rb_none:
                notificationSettings = NotificationSettings.NONE;
                break;
            case R.id.activity_settings_channel_rb_general:
                notificationSettings = NotificationSettings.GENERAL;
                break;
        }
        // Update settings in the database.
        settingsDBM.updateChannelNotificationSettings(channelId, notificationSettings);

        // Show updated message.
        Toast toast = Toast.makeText(this, getString(R.string.general_settings_updated), Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);
        toast.show();
    }
}

