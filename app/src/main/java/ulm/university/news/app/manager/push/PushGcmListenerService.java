package ulm.university.news.app.manager.push;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEventAnnouncements;
import ulm.university.news.app.api.ChannelAPI;
import ulm.university.news.app.controller.ChannelActivity;
import ulm.university.news.app.controller.ChannelController;
import ulm.university.news.app.controller.ModeratorChannelActivity;
import ulm.university.news.app.controller.ModeratorMainActivity;
import ulm.university.news.app.data.Announcement;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.PushMessage;
import ulm.university.news.app.data.enums.NotificationSettings;
import ulm.university.news.app.data.enums.Priority;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.manager.database.SettingsDatabaseManager;
import ulm.university.news.app.util.Util;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class PushGcmListenerService extends GcmListenerService {

    private static final String TAG = "PushGcmListenerService";

    private SettingsDatabaseManager settingsDBM;
    PushMessage pushMessage;

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        settingsDBM = new SettingsDatabaseManager(this);
        pushMessage = null;
    }

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     * For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Gson gson = new Gson();
        pushMessage = gson.fromJson(message, PushMessage.class);
        Log.d(TAG, pushMessage.toString());

        // Process the push message.
        if (pushMessage != null) {
            handlePushMessage();
        }
    }

    /**
     * Load data described by push message.
     */
    private void handlePushMessage() {
        // TODO Group stuff.
        switch (pushMessage.getPushType()) {
            case ANNOUNCEMENT_NEW:
                // Load new announcements from server.
                int messageNumber = new ChannelDatabaseManager(getApplicationContext()).getMaxMessageNumberAnnouncement
                        (pushMessage.getId1());
                ChannelAPI.getInstance(getApplicationContext()).getAnnouncements(pushMessage.getId1(), messageNumber);
                break;
            case CHANNEL_DELETED:
                // Mark local channel as deleted.
                int channelId = pushMessage.getId1();
                ChannelController.deleteChannel(getApplicationContext(), channelId);
                // Notify user as if this push message had the priority HIGH.
                NotificationSettings notificationSettings = settingsDBM.getSettings().getNotificationSettings();
                if (!notificationSettings.equals(NotificationSettings.NONE)) {
                    sendChannelDeletedNotification(channelId);
                }
                break;
            case CHANNEL_CHANGED:
                break;
            case MODERATOR_ADDED:
                break;
            case MODERATOR_CHANGED:
                break;
            case MODERATOR_REMOVED:
                break;
        }
    }

    /**
     * Create and show a simple notification containing the channels name where the new announcement was posted.
     */
    private void sendAnnouncementNotification(int channelId) {
        ChannelDatabaseManager channelDBM = new ChannelDatabaseManager(this);
        Channel channel = channelDBM.getChannel(channelId);
        int numberOfAnnouncements = channelDBM.getNumberOfUnreadAnnouncements(channelId);

        Intent intent;
        boolean loggedIn = Util.getInstance(this).getLoggedInModerator() != null;
        if (loggedIn) {
            // User is logged in as local moderator.
            intent = new Intent(this, ModeratorChannelActivity.class);
        } else {
            // User isn't logged in.
            intent = new Intent(this, ChannelActivity.class);
        }
        intent.putExtra("channelId", channelId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Create a PendingIntent containing the entire back stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        if (loggedIn) {
            stackBuilder.addParentStack(ModeratorChannelActivity.class);
        } else {
            stackBuilder.addParentStack(ChannelActivity.class);
        }
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(channelId, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set channel icon as large icon.
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), ChannelController.getChannelIcon(channel));
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notification_icon_1)
                .setColor(ContextCompat.getColor(this, R.color.uni_main_primary))
                .setLargeIcon(bitmap)
                .setContentTitle(channel.getName())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        if (numberOfAnnouncements > 1) {
            notificationBuilder.setContentText(getString(R.string.push_message_new_announcements));
            notificationBuilder.setNumber(numberOfAnnouncements);
        } else {
            notificationBuilder.setContentText(getString(R.string.push_message_new_announcement));
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(channelId, notificationBuilder.build());
    }

    /**
     * Create and show a simple notification containing the channels name where the new announcement was posted.
     */
    private void sendChannelDeletedNotification(int channelId) {
        ChannelDatabaseManager channelDBM = new ChannelDatabaseManager(this);
        Channel channel = channelDBM.getChannel(channelId);

        Intent intent;
        boolean loggedIn = Util.getInstance(this).getLoggedInModerator() != null;
        if (loggedIn) {
            // User is logged in as local moderator.
            intent = new Intent(this, ModeratorMainActivity.class);
        } else {
            // User isn't logged in.
            intent = new Intent(this, ChannelActivity.class);
        }
        intent.putExtra("channelId", channelId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Create a PendingIntent containing the entire back stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        if (loggedIn) {
            stackBuilder.addParentStack(ModeratorMainActivity.class);
        } else {
            stackBuilder.addParentStack(ChannelActivity.class);
        }
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(channelId, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set channel icon as large icon.
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), ChannelController.getChannelIcon(channel));
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notification_icon_1)
                .setColor(ContextCompat.getColor(this, R.color.uni_main_primary))
                .setLargeIcon(bitmap)
                .setContentTitle(channel.getName())
                .setContentText(getString(R.string.push_message_channel_deleted))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(channelId, notificationBuilder.build());
    }

    /**
     * Create and show a simple notification containing the groups name where the new message was posted.
     */
    private void sendGroupNotification(int groupId) {
    }

    /**
     * This method will be called when a list of announcements is posted to the EventBus.
     *
     * @param event The bus event containing a list of announcement objects.
     */
    public void onEvent(BusEventAnnouncements event) {
        // Unregister this instance. For new push messages the new instance will be registered in onCreate().
        EventBus.getDefault().unregister(this);
        Log.d(TAG, event.toString());
        ChannelController.storeAnnouncements(getApplicationContext(), event.getAnnouncements());

        // Check if new announcements were loaded.
        if (!event.getAnnouncements().isEmpty()) {
            // Defines whether the user should be notified or not.
            boolean showNotification = false;
            NotificationSettings notificationSettings = settingsDBM.getSettings().getNotificationSettings();
            switch (notificationSettings) {
                case ALL:
                    showNotification = true;
                    break;
                case PRIORITY:
                    for (Announcement announcement : event.getAnnouncements()) {
                        if (announcement.getPriority() == Priority.HIGH) {
                            showNotification = true;
                            break;
                        }
                    }
                    break;
                case NONE:
                    showNotification = false;
                    break;
            }

            // TODO Do not notify if message was send from logged in moderator?
            // TODO Do not notify moderator stuff at all?

            if (pushMessage == null) {
                Log.d(TAG, "Push message is null.");
                showNotification = false;
            }

            if (showNotification) {
                // Show a separate notification for each channel.
                sendAnnouncementNotification(event.getAnnouncements().get(0).getChannelId());
            }
        }
    }
}
