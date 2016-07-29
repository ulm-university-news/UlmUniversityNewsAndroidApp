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

import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.R;
import ulm.university.news.app.api.BusEventAnnouncements;
import ulm.university.news.app.api.BusEventBallots;
import ulm.university.news.app.api.BusEventConversationMessages;
import ulm.university.news.app.api.BusEventConversations;
import ulm.university.news.app.api.BusEventGroupMembers;
import ulm.university.news.app.api.BusEventModerators;
import ulm.university.news.app.api.BusEventOptions;
import ulm.university.news.app.api.ChannelAPI;
import ulm.university.news.app.api.GroupAPI;
import ulm.university.news.app.controller.ChannelActivity;
import ulm.university.news.app.controller.ChannelController;
import ulm.university.news.app.controller.GroupActivity;
import ulm.university.news.app.controller.GroupController;
import ulm.university.news.app.controller.ModeratorChannelActivity;
import ulm.university.news.app.controller.ModeratorController;
import ulm.university.news.app.controller.ModeratorMainActivity;
import ulm.university.news.app.data.Announcement;
import ulm.university.news.app.data.Ballot;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Conversation;
import ulm.university.news.app.data.ConversationMessage;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.data.Option;
import ulm.university.news.app.data.PushMessage;
import ulm.university.news.app.data.User;
import ulm.university.news.app.data.enums.NotificationSettings;
import ulm.university.news.app.data.enums.Priority;
import ulm.university.news.app.data.enums.PushType;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.manager.database.GroupDatabaseManager;
import ulm.university.news.app.manager.database.SettingsDatabaseManager;
import ulm.university.news.app.manager.database.UserDatabaseManager;
import ulm.university.news.app.util.Util;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class PushGcmListenerService extends GcmListenerService {

    private static final String TAG = "PushGcmListenerService";

    private SettingsDatabaseManager settingsDBM;
    private PushMessage pushMessage;

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
        int messageNumber;
        NotificationSettings notificationSettings;
        switch (pushMessage.getPushType()) {
            case ANNOUNCEMENT_NEW:
                // Load new announcements from server.
                messageNumber = new ChannelDatabaseManager(getApplicationContext()).getMaxMessageNumberAnnouncement
                        (pushMessage.getId1());
                ChannelAPI.getInstance(getApplicationContext()).getAnnouncements(pushMessage.getId1(), messageNumber);
                break;
            case CHANNEL_DELETED:
                // Mark local channel as deleted.
                int channelId = pushMessage.getId1();
                ChannelController.deleteChannel(getApplicationContext(), channelId);
                // Notify user as if this push message had the priority HIGH.
                notificationSettings = settingsDBM.getChannelNotificationSettings(channelId);
                if (notificationSettings == NotificationSettings.GENERAL) {
                    notificationSettings = settingsDBM.getSettings().getNotificationSettings();
                }
                if (!notificationSettings.equals(NotificationSettings.NONE)) {
                    sendChannelDeletedNotification(channelId);
                }
                break;
            case CHANNEL_CHANGED:
                // Load updated channel data from server.
                ChannelAPI.getInstance(getApplicationContext()).getChannel(pushMessage.getId1());
                break;
            case MODERATOR_ADDED:
            case MODERATOR_CHANGED:
            case MODERATOR_REMOVED:
                // Refresh responsible moderator data.
                ChannelAPI.getInstance(this).getResponsibleModerators(pushMessage.getId1());
                break;
            case CONVERSATION_CHANGED:
            case CONVERSATION_CLOSED:
                GroupAPI.getInstance(getApplicationContext()).getConversation(
                        pushMessage.getId1(), pushMessage.getId2());
                break;
            case CONVERSATION_CHANGED_ALL:
            case CONVERSATION_NEW:
                GroupAPI.getInstance(getApplicationContext()).getConversations(pushMessage.getId1());
                break;
            case CONVERSATION_MESSAGE_NEW:
                // Get conversation message data. Request new messages only.
                messageNumber = new GroupDatabaseManager(getApplicationContext())
                        .getMaxMessageNumberConversationMessage(pushMessage.getId2());
                GroupAPI.getInstance(this).getConversationMessages(pushMessage.getId1(), pushMessage.getId2(),
                        messageNumber);
                break;
            case CONVERSATION_DELETED:
                notificationSettings = settingsDBM.getGroupNotificationSettings(pushMessage.getId1());
                if (notificationSettings == NotificationSettings.GENERAL) {
                    notificationSettings = settingsDBM.getSettings().getNotificationSettings();
                }
                if (!notificationSettings.equals(NotificationSettings.NONE)) {
                    sendConversationNotification(pushMessage.getId1());
                }
                new GroupDatabaseManager(getApplicationContext()).deleteConversation(pushMessage.getId2());
                new GroupDatabaseManager(getApplicationContext()).setGroupNewEvents(pushMessage.getId1(), true);
                break;
            case PARTICIPANT_NEW:
            case PARTICIPANT_CHANGED:
            case PARTICIPANT_LEFT:
            case PARTICIPANT_REMOVED:
                GroupAPI.getInstance(this).getGroupMembers(pushMessage.getId1());
                break;
            case BALLOT_NEW:
            case BALLOT_CHANGED:
            case BALLOT_CHANGED_ALL:
                GroupAPI.getInstance(this).getBallots(pushMessage.getId1());
                break;
            case BALLOT_OPTION_DELETED:
            case BALLOT_OPTION_ALL:
                GroupAPI.getInstance(this).getOptions(pushMessage.getId1(), pushMessage.getId2(), true);
                break;
            case BALLOT_OPTION_VOTE:
            case BALLOT_OPTION_VOTE_ALL:
                // Do nothing. Load new votes when user enters ballot screen.
                break;
            case GROUP_CHANGED:
                GroupAPI.getInstance(this).getGroup(pushMessage.getId1());
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
        notificationManager.notify(1, notificationBuilder.build());
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
        notificationManager.notify(2, notificationBuilder.build());
    }

    /**
     * This method will be called when a list of ballots is posted to the EventBus.
     *
     * @param event The bus event containing a list of ballot objects.
     */
    public void onEventMainThread(BusEventBallots event) {
        Log.d(TAG, event.toString());
        List<Ballot> ballots = event.getBallots();
        boolean newBallots = GroupController.storeBallots(getApplicationContext(), ballots, pushMessage.getId1());
        if (newBallots) {
            NotificationSettings notificationSettings = settingsDBM.getGroupNotificationSettings(pushMessage.getId1());
            if (notificationSettings == NotificationSettings.GENERAL) {
                notificationSettings = settingsDBM.getSettings().getNotificationSettings();
            }
            if (notificationSettings.equals(NotificationSettings.ALL)) {
                sendBallotNotification(pushMessage.getId1());
            } else if (notificationSettings.equals(NotificationSettings.PRIORITY)) {
                if (pushMessage.getPushType().equals(PushType.BALLOT_NEW)) {
                    sendBallotNotification(pushMessage.getId1());
                }
            }
            new GroupDatabaseManager(getApplicationContext()).setGroupNewEvents(pushMessage.getId1(), true);
        }
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
            int channelId = event.getAnnouncements().get(0).getChannelId();
            // Defines whether the user should be notified or not.
            boolean showNotification = false;
            NotificationSettings notificationSettings = settingsDBM.getChannelNotificationSettings(channelId);
            if (notificationSettings == NotificationSettings.GENERAL) {
                notificationSettings = settingsDBM.getSettings().getNotificationSettings();
            }
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

            if (pushMessage == null) {
                Log.d(TAG, "Push message is null.");
                showNotification = false;
            }

            if (showNotification) {
                // Show a separate notification for each channel.
                sendAnnouncementNotification(channelId);
            }
        }
    }

    /**
     * Create and show a notification containing the groups name where the new conversation was posted.
     */
    private void sendConversationNotification(int groupId) {
        Intent intent;
        boolean loggedIn = Util.getInstance(this).getLoggedInModerator() != null;
        if (loggedIn) {
            // User is logged in as local moderator.
            intent = new Intent(this, ModeratorMainActivity.class);
        } else {
            // User isn't logged in.
            intent = new Intent(this, GroupActivity.class);
        }
        intent.putExtra("groupId", groupId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Create a PendingIntent containing the entire back stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        if (loggedIn) {
            stackBuilder.addParentStack(ModeratorMainActivity.class);
        } else {
            stackBuilder.addParentStack(GroupActivity.class);
        }
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(groupId, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set group icon as large icon.
        Group group = new GroupDatabaseManager(getApplicationContext()).getGroup(groupId);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), GroupController.getGroupIcon(group,
                getApplicationContext()));
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notification_icon_1)
                .setColor(ContextCompat.getColor(this, R.color.uni_main_primary))
                .setLargeIcon(bitmap)
                .setContentTitle(String.format(getString(R.string.push_message_group), group.getName()))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        notificationBuilder.setContentText(getString(R.string.push_message_conversation_changed));

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(3, notificationBuilder.build());
    }

    /**
     * Create and show a notification containing the groups name where the new conversation was posted.
     */
    private void sendBallotNotification(int groupId) {
        Intent intent;
        boolean loggedIn = Util.getInstance(this).getLoggedInModerator() != null;
        if (loggedIn) {
            // User is logged in as local moderator.
            intent = new Intent(this, ModeratorMainActivity.class);
        } else {
            // User isn't logged in.
            intent = new Intent(this, GroupActivity.class);
        }
        intent.putExtra("groupId", groupId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Create a PendingIntent containing the entire back stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        if (loggedIn) {
            stackBuilder.addParentStack(ModeratorMainActivity.class);
        } else {
            stackBuilder.addParentStack(GroupActivity.class);
        }
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(groupId, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set group icon as large icon.
        Group group = new GroupDatabaseManager(getApplicationContext()).getGroup(groupId);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), GroupController.getGroupIcon(group,
                getApplicationContext()));
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notification_icon_1)
                .setColor(ContextCompat.getColor(this, R.color.uni_main_primary))
                .setLargeIcon(bitmap)
                .setContentTitle(String.format(getString(R.string.push_message_group), group.getName()))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        notificationBuilder.setContentText(getString(R.string.push_message_ballot_changed));

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(4, notificationBuilder.build());
    }

    /**
     * Create and show a notification containing the conversations name where the new conversation message was posted.
     */
    private void sendNewConversationMessageNotification(int groupId, int conversationId) {
        Intent intent;
        boolean loggedIn = Util.getInstance(this).getLoggedInModerator() != null;
        if (loggedIn) {
            // User is logged in as local moderator.
            intent = new Intent(this, ModeratorMainActivity.class);
        } else {
            // User isn't logged in.
            intent = new Intent(this, GroupActivity.class);
        }
        intent.putExtra("groupId", groupId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Create a PendingIntent containing the entire back stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        if (loggedIn) {
            stackBuilder.addParentStack(ModeratorMainActivity.class);
        } else {
            stackBuilder.addParentStack(GroupActivity.class);
        }
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(groupId, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set group icon as large icon.
        Conversation conversation = new GroupDatabaseManager(getApplicationContext()).getConversation(conversationId);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), GroupController.getConversationIcon(conversation,
                getApplicationContext()));
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notification_icon_1)
                .setColor(ContextCompat.getColor(this, R.color.uni_main_primary))
                .setLargeIcon(bitmap)
                .setContentTitle(String.format(getString(R.string.push_message_conversation), conversation.getTitle()))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        if (conversation.getNumberOfUnreadConversationMessages() > 1) {
            notificationBuilder.setContentText(getString(R.string.push_message_new_conversation_messages));
            notificationBuilder.setNumber(conversation.getNumberOfUnreadConversationMessages());
        } else {
            notificationBuilder.setContentText(getString(R.string.push_message_new_conversation_message));
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(5, notificationBuilder.build());
    }

    /**
     * This method will be called when a channel is posted to the EventBus.
     *
     * @param channel The bus event containing a channel object.
     */
    public void onEvent(Channel channel) {
        // Unregister this instance. For new push messages the new instance will be registered in onCreate().
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "EventBus:" + channel.toString());
        new ChannelDatabaseManager(this).updateChannel(channel);
    }

    /**
     * This method will be called when conversations are posted to the EventBus.
     *
     * @param event The bus event containing conversation objects.
     */
    public void onEvent(BusEventConversations event) {
        // Unregister this instance. For new push messages the new instance will be registered in onCreate().
        EventBus.getDefault().unregister(this);
        Log.d(TAG, event.toString());
        boolean newConversationsStored = GroupController.storeConversations(
                getApplicationContext(), event.getConversations(), pushMessage.getId1());
        if (newConversationsStored || (event.getConversations() != null && !event.getConversations().isEmpty() &&
                pushMessage.getPushType().equals(PushType.CONVERSATION_CHANGED_ALL))) {
            new GroupDatabaseManager(getApplicationContext()).setGroupNewEvents(pushMessage.getId1(), true);
            NotificationSettings notificationSettings = settingsDBM.getGroupNotificationSettings(pushMessage.getId1());
            if (notificationSettings == NotificationSettings.GENERAL) {
                notificationSettings = settingsDBM.getSettings().getNotificationSettings();
            }
            if (notificationSettings.equals(NotificationSettings.ALL)) {
                sendConversationNotification(pushMessage.getId1());
            } else if (notificationSettings.equals(NotificationSettings.PRIORITY)) {
                if (pushMessage.getPushType().equals(PushType.CONVERSATION_NEW)) {
                    sendConversationNotification(pushMessage.getId1());
                }
            }
        }
    }

    /**
     * This method will be called when a conversation is posted to the EventBus.
     *
     * @param conversation The bus event containing a conversation object.
     */
    public void onEvent(Conversation conversation) {
        // Unregister this instance. For new push messages the new instance will be registered in onCreate().
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "EventBus:" + conversation.toString());
        GroupDatabaseManager groupDBM = new GroupDatabaseManager(getApplicationContext());
        groupDBM.updateConversation(conversation);
        groupDBM.setGroupNewEvents(pushMessage.getId1(), true);
        NotificationSettings notificationSettings = settingsDBM.getGroupNotificationSettings(pushMessage.getId1());
        if (notificationSettings == NotificationSettings.GENERAL) {
            notificationSettings = settingsDBM.getSettings().getNotificationSettings();
        }
        if (notificationSettings.equals(NotificationSettings.ALL)) {
            sendConversationNotification(pushMessage.getId1());
        } else if (notificationSettings.equals(NotificationSettings.PRIORITY)) {
            if (pushMessage.getPushType().equals(PushType.CONVERSATION_NEW)) {
                sendConversationNotification(pushMessage.getId1());
            }
        }
    }

    /**
     * This method will be called when conversation messages are posted to the EventBus.
     *
     * @param event The bus event containing conversation message objects.
     */
    public void onEvent(BusEventConversationMessages event) {
        // Unregister this instance. For new push messages the new instance will be registered in onCreate().
        EventBus.getDefault().unregister(this);
        Log.d(TAG, event.toString());
        List<ConversationMessage> conversationMessages = event.getConversationMessages();
        // Store new conversation messages in database.
        GroupDatabaseManager groupDBM = new GroupDatabaseManager(getApplicationContext());
        for (ConversationMessage cm : conversationMessages) {
            groupDBM.storeConversationMessage(cm);
        }
        if (!conversationMessages.isEmpty()) {
            groupDBM.setGroupNewEvents(pushMessage.getId1(), true);
            NotificationSettings notificationSettings = settingsDBM.getGroupNotificationSettings(pushMessage.getId1());
            if (notificationSettings == NotificationSettings.GENERAL) {
                notificationSettings = settingsDBM.getSettings().getNotificationSettings();
            }
            if (!notificationSettings.equals(NotificationSettings.NONE)) {
                sendNewConversationMessageNotification(pushMessage.getId1(), pushMessage.getId2());
            }
        }
    }

    /**
     * This method will be called when a list of options is posted to the EventBus.
     *
     * @param event The bus event containing a list of ballot options objects.
     */
    public void onEventMainThread(BusEventOptions event) {
        Log.d(TAG, event.toString());
        List<Option> options = event.getOptions();

        boolean newOptions = GroupController.storeOptions(getApplicationContext(), options, pushMessage.getId2());
        boolean newVotes = GroupController.storeVoters(getApplicationContext(), options);

        if (newVotes || newOptions) {
            NotificationSettings notificationSettings = settingsDBM.getGroupNotificationSettings(pushMessage.getId1());
            if (notificationSettings == NotificationSettings.GENERAL) {
                notificationSettings = settingsDBM.getSettings().getNotificationSettings();
            }
            if (notificationSettings.equals(NotificationSettings.ALL)) {
                sendOptionNotification(pushMessage.getId1(), pushMessage.getId2());
            }
            new GroupDatabaseManager(getApplicationContext()).setGroupNewEvents(pushMessage.getId1(), true);
        }
    }

    /**
     * Create and show a notification containing the groups name where the new conversation was posted.
     */
    private void sendOptionNotification(int groupId, int ballotId) {
        Intent intent;
        boolean loggedIn = Util.getInstance(this).getLoggedInModerator() != null;
        if (loggedIn) {
            // User is logged in as local moderator.
            intent = new Intent(this, ModeratorMainActivity.class);
        } else {
            // User isn't logged in.
            intent = new Intent(this, GroupActivity.class);
        }
        intent.putExtra("groupId", groupId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Create a PendingIntent containing the entire back stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        if (loggedIn) {
            stackBuilder.addParentStack(ModeratorMainActivity.class);
        } else {
            stackBuilder.addParentStack(GroupActivity.class);
        }
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(groupId, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set group icon as large icon.
        Ballot ballot = new GroupDatabaseManager(getApplicationContext()).getBallot(ballotId);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), GroupController.getBallotIcon(ballot,
                getApplicationContext()));
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notification_icon_1)
                .setColor(ContextCompat.getColor(this, R.color.uni_main_primary))
                .setLargeIcon(bitmap)
                .setContentTitle(String.format(getString(R.string.push_message_ballot), ballot.getTitle()))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        notificationBuilder.setContentText(getString(R.string.push_message_options_changed));

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(6, notificationBuilder.build());
    }

    /**
     * Create and show a notification containing the groups name where the new conversation was posted.
     */
    private void sendGroupMemberNotification(int groupId) {
        Intent intent;
        boolean loggedIn = Util.getInstance(this).getLoggedInModerator() != null;
        if (loggedIn) {
            // User is logged in as local moderator.
            intent = new Intent(this, ModeratorMainActivity.class);
        } else {
            // User isn't logged in.
            intent = new Intent(this, GroupActivity.class);
        }
        intent.putExtra("groupId", groupId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Create a PendingIntent containing the entire back stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        if (loggedIn) {
            stackBuilder.addParentStack(ModeratorMainActivity.class);
        } else {
            stackBuilder.addParentStack(GroupActivity.class);
        }
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(groupId, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set group icon as large icon.
        Group group = new GroupDatabaseManager(getApplicationContext()).getGroup(groupId);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), GroupController.getGroupIcon(group,
                getApplicationContext()));
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notification_icon_1)
                .setColor(ContextCompat.getColor(this, R.color.uni_main_primary))
                .setLargeIcon(bitmap)
                .setContentTitle(String.format(getString(R.string.push_message_group), group.getName()))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        notificationBuilder.setContentText(getString(R.string.push_message_group_members_changed));

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(7, notificationBuilder.build());
    }

    /**
     * This method will be called when a list of users is posted to the EventBus.
     *
     * @param event The bus event containing a list of user objects.
     */
    public void onEvent(BusEventGroupMembers event) {
        Log.d(TAG, event.toString());
        List<User> users = event.getUsers();

        // Store users in database an add them as group members to the group.
        UserDatabaseManager userDBM = new UserDatabaseManager(this);
        GroupDatabaseManager groupDBM = new GroupDatabaseManager(this);
        for (User u : users) {
            userDBM.storeUser(u);
            // Update users to make name changes visible.
            userDBM.updateUser(u);
            if (u.getActive() != null && u.getActive()) {
                groupDBM.addUserToGroup(pushMessage.getId1(), u.getId());
            } else {
                groupDBM.removeUserFromGroup(pushMessage.getId1(), u.getId());
            }
        }
        NotificationSettings notificationSettings = settingsDBM.getGroupNotificationSettings(pushMessage.getId1());
        if (notificationSettings == NotificationSettings.GENERAL) {
            notificationSettings = settingsDBM.getSettings().getNotificationSettings();
        }
        if (notificationSettings.equals(NotificationSettings.ALL)) {
            sendGroupMemberNotification(pushMessage.getId1());
        }
        groupDBM.setGroupNewEvents(pushMessage.getId1(), true);
    }

    /**
     * This method will be called when a list of moderators is posted to the EventBus.
     *
     * @param event The bus event containing a list of moderator objects.
     */
    public void onEvent(BusEventModerators event) {
        Log.d(TAG, event.toString());
        ModeratorController.storeModerators(this, event.getModerators(), pushMessage.getId1());
    }

    /**
     * This method will be called when a group is posted to the EventBus.
     *
     * @param group The group object sent to the EventBus.
     */
    public void onEvent(Group group) {
        Log.d(TAG, group.toString());
        new GroupDatabaseManager(this).updateGroup(group);
    }
}
