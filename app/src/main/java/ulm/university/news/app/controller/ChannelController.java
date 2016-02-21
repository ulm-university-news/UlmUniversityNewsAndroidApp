package ulm.university.news.app.controller;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashSet;
import java.util.List;

import ulm.university.news.app.R;
import ulm.university.news.app.api.ChannelAPI;
import ulm.university.news.app.data.Announcement;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Lecture;
import ulm.university.news.app.data.Moderator;
import ulm.university.news.app.data.Reminder;
import ulm.university.news.app.data.enums.ChannelType;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.manager.database.ModeratorDatabaseManager;

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
        if (channel.isDeleted()) {
            // Set special color for channels which are marked as deleted.
            activity.setTheme(R.style.UlmUniversity_Deleted);
        } else if (ChannelType.LECTURE.equals(channel.getType())) {
            Lecture lecture = (Lecture) channel;
            // Set appropriate faculty color.
            switch (lecture.getFaculty()) {
                case ENGINEERING_COMPUTER_SCIENCE_PSYCHOLOGY:
                    activity.setTheme(R.style.UlmUniversity_Informatics);
                    break;
                case MATHEMATICS_ECONOMICS:
                    activity.setTheme(R.style.UlmUniversity_Mathematics);
                    break;
                case MEDICINES:
                    activity.setTheme(R.style.UlmUniversity_Medicines);
                    break;
                case NATURAL_SCIENCES:
                    activity.setTheme(R.style.UlmUniversity_Science);
                    break;
            }
        } else {
            // Use main color for other channels.
            activity.setTheme(R.style.UlmUniversity_Main);
        }
    }

    public static void storeAnnouncements(Context context, List<Announcement> announcements) {
        // Check if there are new announcements.
        if (!announcements.isEmpty()) {
            ChannelDatabaseManager channelDBM = new ChannelDatabaseManager(context);
            HashSet<Integer> authorIds = new HashSet<>();

            // Store new announcements.
            for (Announcement announcement : announcements) {
                channelDBM.storeAnnouncement(announcement);
                authorIds.add(announcement.getAuthorModerator());
            }

            // Check if a new moderator has written any of the announcements.
            List<Moderator> moderatorsDB = new ModeratorDatabaseManager(context).getModerators();
            boolean moderatorExists = false;
            for (Integer authorId : authorIds) {
                moderatorExists = false;
                for (Moderator moderator : moderatorsDB) {
                    if (moderator.getId() == authorId) {
                        moderatorExists = true;
                        break;
                    }
                }
                if (!moderatorExists) {
                    break;
                }
            }
            // If a moderator who is not stored yet is found, request new moderator data from server.
            if (!moderatorExists) {
                ChannelAPI.getInstance(context).getResponsibleModerators(announcements.get(0).getChannelId());
            }

            // TODO How to receive the responsible moderators in PushGcmListenerService?
        }
    }

    public static boolean storeReminders(Context context, List<Reminder> reminders) {
        ChannelDatabaseManager channelDBM = new ChannelDatabaseManager(context);
        HashSet<Integer> authorIds = new HashSet<>();

        boolean newReminders = false;

        if (!reminders.isEmpty()) {
            List<Reminder> remindersDB = channelDBM.getReminders(reminders.get(0).getChannelId());

            boolean alreadyStored;

            // Store new reminders and update existing ones.
            for (Reminder reminder : reminders) {
                alreadyStored = false;
                for (Reminder reminderDB : remindersDB) {
                    if (reminderDB.getId() == reminder.getId()) {
                        channelDBM.updateReminder(reminder);
                        alreadyStored = true;
                        break;
                    }
                }
                if (!alreadyStored) {
                    channelDBM.storeReminder(reminder);
                    authorIds.add(reminder.getAuthorModerator());
                    newReminders = true;
                }
            }

            // Load and store new moderators.
            for (Integer authorId : authorIds) {
                Log.d(TAG, "authorId:" + authorId);
                // TODO Check moderator existence, load and store if necessary.
            }
        }

        return newReminders;
    }

    public static void deleteChannel(Context context, int channelId) {
        ChannelDatabaseManager channelDBM = new ChannelDatabaseManager(context);

        // Check if deleted channel is subscribed by local user.
        List<Channel> channels = channelDBM.getSubscribedChannels();
        boolean isSubscribed = false;
        for (Channel channel : channels) {
            if (channel.getId() == channelId) {
                isSubscribed = true;
                break;
            }
        }

        if (isSubscribed) {
            // If deleted channel is subscribed, just mark channel as deleted and keep in in local database.
            channelDBM.setChannelToDeleted(channelId);
            // TODO Also show a dialog which explains that the channel was delete from the server.
        } else {
            // If deleted channel isn't subscribed, delete it from local database.
            channelDBM.deleteChannel(channelId);
        }
    }

    public static String getFormattedDateShort(DateTime date) {
        // Format the date for output.
        // TODO Language dependency.
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("HH:mm MM/dd/yy");
        return dtfOut.print(date);
    }

    public static String getFormattedDateLong(DateTime date) {
        // Format the date for output.
        // TODO Language dependency.
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm");
        return dtfOut.print(date);
    }
}
