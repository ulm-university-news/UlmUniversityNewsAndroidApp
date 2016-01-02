package ulm.university.news.app.controller;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import ulm.university.news.app.R;
import ulm.university.news.app.api.GroupAPI;
import ulm.university.news.app.data.enums.GroupType;
import ulm.university.news.app.util.Constants;
import ulm.university.news.app.util.Util;

public class GroupSearchActivity extends AppCompatActivity {
    /** This classes tag for logging. */
    private static final String TAG = "GroupSearchActivity";

    private TextView tvError;
    private EditText etSearch;
    private Button btnSearch;
    private ImageView ivSearch;
    private RadioGroup rgSearch;
    private RadioButton rbId;
    private RadioButton rbName;
    private CheckBox chkTutorial;
    private CheckBox chkWork;

    private boolean isInputValid;
    private int groupId;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_group_search_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        isInputValid = false;
        initView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initView() {
        tvError = (TextView) findViewById(R.id.activity_group_search_tv_error);
        etSearch = (EditText) findViewById(R.id.activity_group_search_et_search);
        btnSearch = (Button) findViewById(R.id.activity_group_search_btn_search);
        ivSearch = (ImageView) findViewById(R.id.activity_group_search_ib_search);
        rbId = (RadioButton) findViewById(R.id.activity_group_search_rb_id);
        rbName = (RadioButton) findViewById(R.id.activity_group_search_rb_name);
        rgSearch = (RadioGroup) findViewById(R.id.activity_group_search_rg_search);
        chkTutorial = (CheckBox) findViewById(R.id.activity_group_search_chk_tutorial);
        chkWork = (CheckBox) findViewById(R.id.activity_group_search_chk_work);

        ivSearch.setColorFilter(Color.parseColor("#888888"));
        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchGroup();
            }
        });

        rgSearch.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onRadioButtonClicked(checkedId);
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchGroup();
            }
        });
    }

    private void validateTextInput(String input) {
        groupName = input;
        if (input.length() == 0) {
            isInputValid = false;
            tvError.setText(getString(R.string.activity_group_search_error_search_empty));
        } else if (rbId.isChecked()) {
            // Validate group id.
            try {
                groupId = Integer.valueOf(input);
                isInputValid = true;
            } catch (NumberFormatException e) {
                // Invalid group id.
                tvError.setText(getString(R.string.activity_group_search_error_search_numbers));
                isInputValid = false;
            }
        } else {
            // Validate group name.
            if (input.matches(Constants.NAME_PATTERN)) {
                isInputValid = true;
            } else {
                // Invalid group name.
                tvError.setText(getString(R.string.activity_group_search_error_search_name));
                isInputValid = false;
            }
        }
        // Enable or disable button and show or hide error message.
        if (isInputValid) {
            tvError.setVisibility(View.GONE);
        } else {
            tvError.setVisibility(View.VISIBLE);
        }
    }

    private void onRadioButtonClicked(int checkedId) {
        // Check which radio button is selected.
        switch (checkedId) {
            case R.id.activity_group_search_rb_id:
                etSearch.setHint(R.string.activity_group_search_et_hint_id);
                chkTutorial.setVisibility(View.GONE);
                chkWork.setVisibility(View.GONE);
                break;
            case R.id.activity_group_search_rb_name:
                etSearch.setHint(R.string.activity_group_search_et_hint_name);
                chkTutorial.setVisibility(View.VISIBLE);
                chkWork.setVisibility(View.VISIBLE);
                break;
        }
        // Update input validation.
        validateTextInput(etSearch.getText().toString());
    }

    private void searchGroup() {
        // Validate input.
        validateTextInput(etSearch.getText().toString());
        // Check if device is connected to the internet.
        if (!Util.isOnline(this)) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText(getString(R.string.general_error_no_connection));
            // Check if input is valid.
        } else if (isInputValid) {
            if (rbId.isChecked()) {
                GroupAPI.getInstance(this).getGroup(groupId);
            } else {
                // Search for group name.
                if (chkTutorial.isChecked() && chkWork.isChecked()) {
                    GroupAPI.getInstance(this).getGroups(groupName, null);
                }
                if (chkTutorial.isChecked() && !chkWork.isChecked()) {
                    GroupAPI.getInstance(this).getGroups(groupName, GroupType.TUTORIAL.toString());
                }
                if (!chkTutorial.isChecked() && chkWork.isChecked()) {
                    GroupAPI.getInstance(this).getGroups(groupName, GroupType.WORKING.toString());
                }
            }
        }
    }
}
