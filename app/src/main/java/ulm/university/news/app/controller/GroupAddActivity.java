package ulm.university.news.app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import ulm.university.news.app.R;
import ulm.university.news.app.util.TextInputLabels;

public class GroupAddActivity extends AppCompatActivity {

    private TextInputLabels tilName;
    private TextInputLabels tilDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_add);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_group_add_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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
        tilName = (TextInputLabels) findViewById(R.id.activity_group_add_til_name);
        tilDescription = (TextInputLabels) findViewById(R.id.activity_group_add_til_description);

        tilName.setNameAndHint("Name");
        tilDescription.setNameAndHint("Description");
        tilDescription.showError("Enter text!");
    }
}
