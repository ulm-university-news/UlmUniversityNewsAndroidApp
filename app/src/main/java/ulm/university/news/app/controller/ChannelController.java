package ulm.university.news.app.controller;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.util.Log;

import java.util.HashSet;
import java.util.List;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Announcement;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Lecture;
import ulm.university.news.app.data.enums.ChannelType;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;

/**
 * This class provides static util methods which are often used across different activities.
 *
 * @author Matthias Mak
 */
public class ChannelController {
    /** This classes tag for logging. */
    private static final String TAG = "ChannelController";

    /**
     * Changes the apps color theme which defines the toolbar colors and others. Must be called before super.onCreate()
     *
     * @param activity The activity of which the color theme should be set.
     * @param channel The channel which defines the colors.
     */
    public static void setColorTheme(Activity activity, Channel channel) {
        if (ChannelType.LECTURE.equals(channel.getType())) {
            Lecture lecture = (Lecture) channel;
            // Set appropriate faculty color.
            switch (lecture.getFaculty()) {
                case ENGINEERING_COMPUTER_SCIENCE_PSYCHOLOGY:
                    activity.setTheme(R.style.Theme_UlmUniversityInformatics);
                    break;
                case MATHEMATICS_ECONOMICS:
                    activity.setTheme(R.style.Theme_UlmUniversityMathematics);
                    break;
                case MEDICINES:
                    activity.setTheme(R.style.Theme_UlmUniversityMedicines);
                    break;
                case NATURAL_SCIENCES:
                    activity.setTheme(R.style.Theme_UlmUniversityScience);
                    break;
            }
        } else {
            // Use main color for other channels.
            activity.setTheme(R.style.Theme_UlmUniversityMain);
        }
    }

    /**
     * Returns a text which consists of the channels symbol letter, a separator and the channels name.
     *
     * @param activity The activity from which this method is called.
     * @param channel The channel from which the header text should be created.
     * @return The header text.
     */
    public static String getHeaderText(Activity activity, Channel channel) {
        String headerText = "";
        switch (channel.getType()) {
            case LECTURE:
                headerText = activity.getString(R.string.lecture_symbol);
                break;
            case EVENT:
                headerText = activity.getString(R.string.event_symbol);
                break;
            case SPORTS:
                headerText = activity.getString(R.string.sports_symbol);
                break;
            case STUDENT_GROUP:
                headerText = activity.getString(R.string.student_group_symbol);
                break;
            case OTHER:
                headerText = activity.getString(R.string.other_symbol);
                break;
        }
        headerText += " " + Html.fromHtml("&#448; ") + channel.getName();
        return headerText;
    }

    public static void storeAnnouncements(Context context, List<Announcement> announcements) {
        ChannelDatabaseManager channelDBM = new ChannelDatabaseManager(context);
        HashSet<Integer> authorIds = new HashSet<>();

        // Store new announcements.
        for (Announcement announcement : announcements) {
            channelDBM.storeAnnouncement(announcement);
            authorIds.add(announcement.getAuthorModerator());
        }

        // Load and store new moderators.
        for (Integer authorId : authorIds) {
            Log.d(TAG, "authorId:" + authorId);
            // TODO Check moderator existence, load and store if necessary.
        }
    }
}
