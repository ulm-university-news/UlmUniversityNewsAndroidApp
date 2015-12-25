package ulm.university.news.app.manager.push;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;

import ulm.university.news.app.R;
import ulm.university.news.app.controller.ChannelActivity;
import ulm.university.news.app.controller.MainActivity;
import ulm.university.news.app.data.PushMessage;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class PushGcmListenerService extends GcmListenerService {

    private static final String TAG = "PushGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     * For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        Gson gson = new Gson();
        PushMessage pushMessage = gson.fromJson(message, PushMessage.class);
        Log.d(TAG, pushMessage.toString());

        // Process the push message.
        handlePushMessage(pushMessage);

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(pushMessage);
    }

    /**
     * Load data described by push message.
     * @param pushMessage The received push message.
     */
    private void handlePushMessage(PushMessage pushMessage) {
        // TODO
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param pushMessage The received push message.
     */
    private void sendNotification(PushMessage pushMessage) {
        Intent intent;
        switch (pushMessage.getPushType()){
            case ANNOUNCEMENT_NEW:
                intent = new Intent(this, ChannelActivity.class);
                intent.putExtra("channelId", pushMessage.getId1());
                break;
            default:
                intent = new Intent(this, MainActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle("GCM Message")
                .setContentText(pushMessage.getPushType().toString() + " " + pushMessage.getId1())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
