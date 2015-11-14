package ulm.university.news.app.manager.push;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class PushInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "PushListenerService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of the previous token had been
     * compromised. This call is initiated by the InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes.
        Log.i(TAG, "Update of GCM push token has been requested.");
        Intent intent = new Intent(this, PushTokenGenerationService.class);
        startService(intent);
    }
}