package ulm.university.news.app.controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Settings;
import ulm.university.news.app.data.enums.OrderSettings;
import ulm.university.news.app.manager.database.SettingsDatabaseManager;

public class SettingsListActivity extends AppCompatActivity {

    Toast toast;
    Settings settings;
    SettingsDatabaseManager settingsDBM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_settings_list_toolbar);
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

    @SuppressWarnings("all")
    private void initView() {
        toast = Toast.makeText(this, getString(R.string.general_settings_updated), Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);

        RadioGroup rgSettingsMessages = (RadioGroup) findViewById(R.id.activity_settings_list_rg_settings_messages);
        RadioButton rbMessagesAsc = (RadioButton) findViewById(R.id.activity_settings_list_rb_messages_asc);
        RadioButton rbMessagesDesc = (RadioButton) findViewById(R.id.activity_settings_list_rb_messages_desc);

        switch (settings.getMessageSettings()) {
            case ASCENDING:
                rbMessagesAsc.setChecked(true);
                break;
            case DESCENDING:
                rbMessagesDesc.setChecked(true);
                break;
        }

        rgSettingsMessages.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onRbMessagesClicked(checkedId);
            }
        });

        RadioGroup rgSettingsChannel = (RadioGroup) findViewById(R.id.activity_settings_list_rg_settings_channel);
        RadioButton rbChannelAlphabetical = (RadioButton) findViewById(R.id
                .activity_settings_list_rb_channel_alphabetical);
        RadioButton rbChannelType = (RadioButton) findViewById(R.id.activity_settings_list_rb_channel_type);
        RadioButton rbChannelMessages = (RadioButton) findViewById(R.id.activity_settings_list_rb_channel_messages);

        // Disable messages setting. Not supported anymore.
        rbChannelMessages.setVisibility(View.GONE);

        switch (settings.getChannelSettings()) {
            case ALPHABETICAL:
                rbChannelAlphabetical.setChecked(true);
                break;
            case TYPE_AND_FACULTY:
                rbChannelType.setChecked(true);
                break;
            case NEW_MESSAGES:
                rbChannelMessages.setChecked(true);
        }

        rgSettingsChannel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onRbChannelClicked(checkedId);
            }
        });

        RadioGroup rgSettingsGroup = (RadioGroup) findViewById(R.id.activity_settings_list_rg_settings_group);
        RadioButton rbGroupAlphabetical = (RadioButton) findViewById(R.id
                .activity_settings_list_rb_group_alphabetical);
        RadioButton rbGroupType = (RadioButton) findViewById(R.id.activity_settings_list_rb_group_type);
        RadioButton rbGroupMessages = (RadioButton) findViewById(R.id.activity_settings_list_rb_group_messages);

        // Disable messages setting. Not supported anymore.
        rbGroupMessages.setVisibility(View.GONE);

        switch (settings.getGroupSettings()) {
            case ALPHABETICAL:
                rbGroupAlphabetical.setChecked(true);
                break;
            case TYPE:
                rbGroupType.setChecked(true);
                break;
            case NEW_MESSAGES:
                rbGroupMessages.setChecked(true);
        }

        rgSettingsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onRbGroupClicked(checkedId);
            }
        });
    }

    private void onRbChannelClicked(int checkedId) {
        // Check which radio button is selected.
        switch (checkedId) {
            case R.id.activity_settings_list_rb_channel_alphabetical:
                settings.setChannelSettings(OrderSettings.ALPHABETICAL);
                break;
            case R.id.activity_settings_list_rb_channel_type:
                settings.setChannelSettings(OrderSettings.TYPE_AND_FACULTY);
                break;
            case R.id.activity_settings_list_rb_channel_messages:
                settings.setChannelSettings(OrderSettings.NEW_MESSAGES);
                break;
        }
        // Update settings in the database.
        settingsDBM.updateSettings(settings);

        // Show updated message.
        toast.show();
    }

    private void onRbGroupClicked(int checkedId) {
        // Check which radio button is selected.
        switch (checkedId) {
            case R.id.activity_settings_list_rb_group_alphabetical:
                settings.setGroupSettings(OrderSettings.ALPHABETICAL);
                break;
            case R.id.activity_settings_list_rb_group_type:
                settings.setGroupSettings(OrderSettings.TYPE);
                break;
            case R.id.activity_settings_list_rb_group_messages:
                settings.setGroupSettings(OrderSettings.NEW_MESSAGES);
                break;
        }
        // Update settings in the database.
        settingsDBM.updateSettings(settings);

        // Show updated message.
        toast.show();
    }

    private void onRbMessagesClicked(int checkedId) {
        // Check which radio button is selected.
        switch (checkedId) {
            case R.id.activity_settings_list_rb_messages_asc:
                settings.setMessageSettings(OrderSettings.ASCENDING);
                break;
            case R.id.activity_settings_list_rb_messages_desc:
                settings.setMessageSettings(OrderSettings.DESCENDING);
                break;
        }
        // Update settings in the database.
        settingsDBM.updateSettings(settings);

        // Show updated message.
        toast.show();
    }
}
