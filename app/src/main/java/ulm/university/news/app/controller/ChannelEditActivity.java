package ulm.university.news.app.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.ChannelAPI;
import ulm.university.news.app.api.ServerError;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Event;
import ulm.university.news.app.data.Lecture;
import ulm.university.news.app.data.Sports;
import ulm.university.news.app.data.enums.ChannelType;
import ulm.university.news.app.data.enums.Faculty;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.util.Constants;
import ulm.university.news.app.util.TextInputLabels;
import ulm.university.news.app.util.Util;

import static ulm.university.news.app.util.Constants.CONNECTION_FAILURE;

public class ChannelEditActivity extends AppCompatActivity {
    /** This classes tag for logging. */
    private static final String TAG = "ChannelEditActivity";

    private TextInputLabels tilName;
    private TextInputLabels tilDescription;
    private TextInputLabels tilLecturer;
    private TextInputLabels tilAssistant;
    private TextInputLabels tilStartDate;
    private TextInputLabels tilEndDate;
    private TextInputLabels tilDates;
    private TextInputLabels tilLocations;
    private TextInputLabels tilWebsite;
    private TextInputLabels tilContacts;
    private TextInputLabels tilCost;
    private TextInputLabels tilParticipants;
    private TextInputLabels tilOrganizer;
    private Spinner spTerm;
    private Spinner spFaculty;
    private EditText etYear;
    private TextView tvError;
    private ProgressBar pgrAdding;
    private Button btnCreate;

    private Toast toast;
    private ChannelDatabaseManager channelDBM;
    private Channel channelDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set color theme according to channel and lecture type.
        int channelId = getIntent().getIntExtra("channelId", 0);
        channelDBM = new ChannelDatabaseManager(this);
        channelDB = channelDBM.getChannel(channelId);
        ChannelController.setColorTheme(this, channelDB);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_add);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_channel_add_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initView();
        initFields();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateUp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void navigateUp() {
        Intent intent = NavUtils.getParentActivityIntent(this);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        NavUtils.navigateUpTo(this, intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void initView() {
        tilName = (TextInputLabels) findViewById(R.id.activity_channel_add_til_name);
        tilDescription = (TextInputLabels) findViewById(R.id.activity_channel_add_til_description);
        tilDates = (TextInputLabels) findViewById(R.id.activity_channel_add_til_dates);
        tilLocations = (TextInputLabels) findViewById(R.id.activity_channel_add_til_locations);
        tilWebsite = (TextInputLabels) findViewById(R.id.activity_channel_add_til_website);
        tilContacts = (TextInputLabels) findViewById(R.id.activity_channel_add_til_contacts);
        tilLecturer = (TextInputLabels) findViewById(R.id.activity_channel_add_til_lecturer);
        tilAssistant = (TextInputLabels) findViewById(R.id.activity_channel_add_til_assistant);
        tilStartDate = (TextInputLabels) findViewById(R.id.activity_channel_add_til_start_date);
        tilEndDate = (TextInputLabels) findViewById(R.id.activity_channel_add_til_end_date);
        tilCost = (TextInputLabels) findViewById(R.id.activity_channel_add_til_cost);
        tilParticipants = (TextInputLabels) findViewById(R.id.activity_channel_add_til_participants);
        tilOrganizer = (TextInputLabels) findViewById(R.id.activity_channel_add_til_organizer);
        Spinner spType = (Spinner) findViewById(R.id.activity_channel_add_sp_channel_type);
        spTerm = (Spinner) findViewById(R.id.activity_channel_add_sp_term);
        spFaculty = (Spinner) findViewById(R.id.activity_channel_add_sp_channel_faculty);
        etYear = (EditText) findViewById(R.id.activity_channel_add_et_year);
        pgrAdding = (ProgressBar) findViewById(R.id.activity_channel_add_pgr_adding);
        TextView tvType = (TextView) findViewById(R.id.activity_channel_add_tv_channel_type);
        TextView tvFaculty = (TextView) findViewById(R.id.activity_channel_add_tv_channel_faculty);
        TextView tvInfo = (TextView) findViewById(R.id.activity_channel_add_tv_info);
        tvError = (TextView) findViewById(R.id.activity_channel_add_tv_error);
        btnCreate = (Button) findViewById(R.id.activity_channel_add_btn_create);

        tilName.setNameAndHint(getString(R.string.channel_name));
        tilName.setPattern(Constants.NAME_PATTERN);
        tilName.setLength(3, 45);

        // Field may be empty.
        tilDescription.setNameAndHint(getString(R.string.channel_description) + " *");
        tilDescription.setLength(0, Constants.DESCRIPTION_MAX_LENGTH);

        // Field may be empty.
        tilDates.setNameAndHint(getString(R.string.channel_dates) + " *");
        tilDates.setLength(0, Constants.CHANNEL_DATES_MAX_LENGTH);

        // Field may be empty.
        tilLocations.setNameAndHint(getString(R.string.channel_locations) + " *");
        tilLocations.setLength(0, Constants.CHANNEL_LOCATIONS_MAX_LENGTH);

        // Field may be empty.
        tilWebsite.setNameAndHint(getString(R.string.channel_website) + " *");
        tilWebsite.setLength(0, Constants.CHANNEL_WEBSITE_MAX_LENGTH);

        tilContacts.setNameAndHint(getString(R.string.channel_contacts));
        tilContacts.setLength(1, Constants.CHANNEL_CONTACTS_MAX_LENGTH);

        tilLecturer.setNameAndHint(getString(R.string.lecture_lecturer));
        tilLecturer.setLength(1, Constants.CHANNEL_CONTACTS_MAX_LENGTH);

        // Field may be empty.
        tilAssistant.setNameAndHint(getString(R.string.lecture_assistant) + " *");
        tilAssistant.setLength(0, Constants.CHANNEL_CONTACTS_MAX_LENGTH);

        // Field may be empty.
        tilStartDate.setNameAndHint(getString(R.string.lecture_start_date) + " *");
        tilStartDate.setLength(0, Constants.CHANNEL_DATES_MAX_LENGTH);

        // Field may be empty.
        tilEndDate.setNameAndHint(getString(R.string.lecture_end_date) + " *");
        tilEndDate.setLength(0, Constants.CHANNEL_DATES_MAX_LENGTH);

        // Field may be empty.
        tilCost.setNameAndHint(getString(R.string.event_cost) + " *");
        tilCost.setLength(0, Constants.CHANNEL_COST_MAX_LENGTH);

        // Field may be empty.
        tilParticipants.setNameAndHint(getString(R.string.sports_participants) + " *");
        tilParticipants.setLength(0, Constants.CHANNEL_PARTICIPANTS_MAX_LENGTH);

        // Field may be empty.
        tilOrganizer.setNameAndHint(getString(R.string.event_organizer) + " *");
        tilOrganizer.setLength(0, Constants.CHANNEL_CONTACTS_MAX_LENGTH);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.terms, R.layout
                .spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTerm.setAdapter(adapter);
        spTerm.setSelection(0);

        etYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                tvError.setVisibility(View.GONE);
            }
        });

        adapter = ArrayAdapter.createFromResource(this, R.array.faculties, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFaculty.setAdapter(adapter);
        spFaculty.setSelection(0);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editChannel();
            }
        });

        // Show channel type specific fields.
        tilLecturer.setVisibility(View.GONE);
        tilAssistant.setVisibility(View.GONE);
        tilStartDate.setVisibility(View.GONE);
        tilEndDate.setVisibility(View.GONE);
        tilCost.setVisibility(View.GONE);
        tilParticipants.setVisibility(View.GONE);
        tilOrganizer.setVisibility(View.GONE);
        tvFaculty.setVisibility(View.GONE);
        spFaculty.setVisibility(View.GONE);
        switch (channelDB.getType()) {
            case LECTURE:
                tilLecturer.setVisibility(View.VISIBLE);
                tilAssistant.setVisibility(View.VISIBLE);
                tilStartDate.setVisibility(View.VISIBLE);
                tilEndDate.setVisibility(View.VISIBLE);
                tvFaculty.setVisibility(View.VISIBLE);
                spFaculty.setVisibility(View.VISIBLE);
                break;
            case EVENT:
                tilCost.setVisibility(View.VISIBLE);
                tilOrganizer.setVisibility(View.VISIBLE);
                break;
            case SPORTS:
                tilCost.setVisibility(View.VISIBLE);
                tilParticipants.setVisibility(View.VISIBLE);
                break;
        }

        tvType.setVisibility(View.GONE);
        spType.setVisibility(View.GONE);
        tvInfo.setText(getString(R.string.activity_channel_edit_info));
        btnCreate.setText(getString(R.string.general_edit));

        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);
    }

    private void initFields() {
        // Fill fields with stored channel data.
        tilName.setText(channelDB.getName());
        tilDescription.setText(channelDB.getDescription());
        tilDates.setText(channelDB.getDates());
        tilLocations.setText(channelDB.getLocations());
        tilWebsite.setText(channelDB.getWebsite());
        tilContacts.setText(channelDB.getContacts());

        String termShort = channelDB.getTerm().substring(0, 0);
        if (termShort.equals(getString(R.string.channel_term_summer_short))) {
            spTerm.setSelection(0);
        } else {
            spTerm.setSelection(1);
        }

        String year = channelDB.getTerm().substring(1);
        etYear.setText(year);

        switch (channelDB.getType()) {
            case LECTURE:
                tilLecturer.setText(((Lecture) channelDB).getLecturer());
                tilAssistant.setText(((Lecture) channelDB).getAssistant());
                tilStartDate.setText(((Lecture) channelDB).getStartDate());
                tilEndDate.setText(((Lecture) channelDB).getEndDate());
                // TODO Change of faculty not designated on server side.
                spFaculty.setSelection(((Lecture) channelDB).getFaculty().ordinal());
                break;
            case EVENT:
                tilCost.setText(((Event) channelDB).getCost());
                tilOrganizer.setText(((Event) channelDB).getOrganizer());
                break;
            case SPORTS:
                tilCost.setText(((Sports) channelDB).getCost());
                tilParticipants.setText(((Sports) channelDB).getNumberOfParticipants());
                break;
        }
    }

    private void editChannel() {
        boolean valid = true;
        String year = etYear.getText().toString().trim();

        if (!tilName.isValid()) {
            valid = false;
        }
        if (!tilDescription.isValid()) {
            valid = false;
        }
        if (!tilDates.isValid()) {
            valid = false;
        }
        if (!tilLocations.isValid()) {
            valid = false;
        }
        if (!tilWebsite.isValid()) {
            valid = false;
        }
        if (!tilContacts.isValid()) {
            valid = false;
        }

        switch (channelDB.getType()) {
            case LECTURE:
                if (!tilLecturer.isValid()) {
                    valid = false;
                }
                if (!tilAssistant.isValid()) {
                    valid = false;
                }
                if (!tilStartDate.isValid()) {
                    valid = false;
                }
                if (!tilEndDate.isValid()) {
                    valid = false;
                }
                break;
            case EVENT:
                if (!tilCost.isValid()) {
                    valid = false;
                }
                if (!tilOrganizer.isValid()) {
                    valid = false;
                }
                break;
            case SPORTS:
                if (!tilCost.isValid()) {
                    valid = false;
                }
                if (!tilParticipants.isValid()) {
                    valid = false;
                }
                break;
        }

        try {
            int y = Integer.parseInt(year);
            if (y < 2016) {
                tvError.setText(getString(R.string.channel_term_year_error));
                tvError.setVisibility(View.VISIBLE);
                valid = false;
            }
        } catch (NumberFormatException e) {
            tvError.setText(getString(R.string.channel_term_year_error));
            tvError.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (!Util.getInstance(this).isOnline()) {
            String message = getString(R.string.general_error_no_connection)
                    + " " + getString(R.string.general_error_create);
            toast.setText(message);
            toast.show();
            valid = false;
        }

        if (valid) {
            // All checks passed. Create new group.
            tvError.setVisibility(View.GONE);
            btnCreate.setVisibility(View.GONE);
            pgrAdding.setVisibility(View.VISIBLE);

            String term;
            if (spTerm.getSelectedItemPosition() == 0) {
                term = getString(R.string.channel_term_summer_short);
            } else {
                term = getString(R.string.channel_term_winter_short);
            }
            term += year;

            Channel channel;
            switch (channelDB.getType()) {
                case LECTURE:
                    channel = new Lecture();
                    ((Lecture) channel).setLecturer(tilLecturer.getText());
                    if (!tilAssistant.getText().isEmpty()) {
                        ((Lecture) channel).setAssistant(tilAssistant.getText());
                    }
                    if (!tilStartDate.getText().isEmpty()) {
                        ((Lecture) channel).setStartDate(tilStartDate.getText());
                    }
                    if (!tilEndDate.getText().isEmpty()) {
                        ((Lecture) channel).setEndDate(tilEndDate.getText());
                    }
                    Faculty faculty;
                    switch (spFaculty.getSelectedItemPosition()) {
                        case 0:
                            faculty = Faculty.ENGINEERING_COMPUTER_SCIENCE_PSYCHOLOGY;
                            break;
                        case 1:
                            faculty = Faculty.MATHEMATICS_ECONOMICS;
                            break;
                        case 2:
                            faculty = Faculty.MEDICINES;
                            break;
                        default:
                            faculty = Faculty.NATURAL_SCIENCES;
                    }
                    ((Lecture) channel).setFaculty(faculty);
                    break;
                case SPORTS:
                    channel = new Sports();
                    if (!tilCost.getText().isEmpty()) {
                        ((Sports) channel).setCost(tilCost.getText());
                    }
                    if (!tilParticipants.getText().isEmpty()) {
                        ((Sports) channel).setNumberOfParticipants(tilParticipants.getText());
                    }
                    break;
                case EVENT:
                    channel = new Event();
                    if (!tilCost.getText().isEmpty()) {
                        ((Event) channel).setCost(tilCost.getText());
                    }
                    if (!tilOrganizer.getText().isEmpty()) {
                        ((Event) channel).setOrganizer(tilOrganizer.getText());
                    }
                    break;
                default:
                    channel = new Channel();
            }
            channel.setId(channelDB.getId());
            channel.setName(tilName.getText());
            channel.setTerm(term);
            channel.setType(channelDB.getType());
            channel.setContacts(tilContacts.getText());
            if (!tilDescription.getText().isEmpty()) {
                channel.setDescription(tilDescription.getText());
            }
            if (!tilDates.getText().isEmpty()) {
                channel.setDates(tilDates.getText());
            }
            if (!tilLocations.getText().isEmpty()) {
                channel.setLocations(tilLocations.getText());
            }
            if (!tilWebsite.getText().isEmpty()) {
                channel.setWebsite(tilWebsite.getText());
            }

            // Send channel data to the server.
            ChannelAPI.getInstance(this).changeChannel(channel);
        }
    }

    /**
     * This method will be called when a channel is posted to the EventBus.
     *
     * @param channel The channel object.
     */
    public void onEventMainThread(Channel channel) {
        Log.d(TAG, "EventBus: Channel");
        Log.d(TAG, channel.toString());

        // Store channel in database and show created message.
        channelDBM.updateChannel(channel);
        toast.setText(getString(R.string.channel_edited));
        toast.show();

        // If the channel is a lecture.
        if (channelDB.getType().equals(ChannelType.LECTURE)) {
            // And if the faculty has changed, go to ModeratorMainActivity due to color theme change.
            if (!((Lecture) channelDB).getFaculty().equals(((Lecture) channel).getFaculty())) {
                Intent intent = new Intent(this, ModeratorMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return;
            }
        }
        // Otherwise, go back to moderator channel view.
        navigateUp();
    }

    /**
     * This method will be called when a server error is posted to the EventBus.
     *
     * @param serverError The error which occurred on the server.
     */
    public void onEventMainThread(ServerError serverError) {
        Log.d(TAG, "EventBus: ServerError");
        handleServerError(serverError);
    }

    /**
     * Handles the server error and shows appropriate error message.
     *
     * @param serverError The error which occurred on the server.
     */
    public void handleServerError(ServerError serverError) {
        Log.d(TAG, serverError.toString());
        // Show appropriate error message.
        pgrAdding.setVisibility(View.GONE);
        btnCreate.setVisibility(View.VISIBLE);
        switch (serverError.getErrorCode()) {
            case CONNECTION_FAILURE:
                tvError.setText(R.string.general_error_connection_failed);
                break;
        }
    }
}
