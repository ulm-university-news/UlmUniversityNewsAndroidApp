package ulm.university.news.app.manager.push;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import ulm.university.news.app.R;
import ulm.university.news.app.util.Constants;
import ulm.university.news.app.util.Util;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class PushTokenGenerationService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    public PushTokenGenerationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String token;

        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                // Retrieve push token from GCM server via internet.
                InstanceID instanceID = InstanceID.getInstance(this);
                token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                Log.i(TAG, "GCM Registration Token: " + token);

                // Subscribe to topic channels
                subscribeTopics(token);
            }
        } catch (Exception e) {
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            Log.d(TAG, "Failed to complete token refresh.", e);
            token = null;
        }

        // TODO Check if user account already exists. Do check via local database.
        // TODO If account exists just update push token in local and server database.
        // TODO Otherwise proceed like below: Create user account.
        if (Util.getInstance(this.getApplicationContext()).getUserAccessToken() != null) {
            updatePushToken(token);
        } else {
            // Notify UI that registration has completed, so the progress indicator can be hidden.
            Intent registrationComplete = new Intent(Constants.PUSH_TOKEN_CREATED);
            registrationComplete.putExtra("pushToken", token);
            LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
        }
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    private void subscribeTopics(String token) throws IOException {
        // [START subscribe_topics]
        for (String topic : TOPICS) {
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }

    private void updatePushToken(String token) {
        // TODO Update push token on server.
        // TODO Update user (push token) in local database.
    }
}
