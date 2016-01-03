package ulm.university.news.app.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.TypedValue;

import ulm.university.news.app.R;
import ulm.university.news.app.data.LocalUser;
import ulm.university.news.app.manager.database.UserDatabaseManager;

/**
 * The Util class provides useful methods which are often use in several parts of the application. As a Singleton the
 * instance is kept in memory which allows fast access of often used fields like the users access token.
 *
 * @author Matthias Mak
 */
public class Util {
    /** This classes tag for logging. */
    private static final String LOG_TAG = "Util";

    /** The reference for the Util Singleton class. */
    private static Util _instance;

    /** The application context. */
    private Context context;

    /** The local users server access token. */
    private String userAccessToken = null;

    /** The local users id. */
    private Integer userId = null;

    /** The local users name. */
    private String userName = null;

    /** The local moderators server access token. */
    private String moderatorAccessToken = null;

    /**
     * Get the instance of the Util class.
     *
     * @return Instance of Util.
     */
    public static synchronized Util getInstance(Context context) {
        if (_instance == null) {
            _instance = new Util(context);
        }
        return _instance;
    }

    private Util(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Checks if the device has a connection to the internet.
     *
     * @return true if devices is connected to the internet.
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public String getUserAccessToken() {
        if (userAccessToken == null) {
            Log.d(LOG_TAG, "userAccessToken is null.");
            LocalUser localUser = new UserDatabaseManager(context).getLocalUser();
            if (localUser != null) {
                userAccessToken = localUser.getServerAccessToken();
            }
        }
        return userAccessToken;
    }

    public Integer getUserId() {
        if (userId == null) {
            Log.d(LOG_TAG, "userId is null.");
            LocalUser localUser = new UserDatabaseManager(context).getLocalUser();
            if (localUser != null) {
                userId = localUser.getId();
            }
        }
        return userId;
    }

    public String getUserName() {
        if (userName == null) {
            Log.d(LOG_TAG, "userName is null.");
            LocalUser localUser = new UserDatabaseManager(context).getLocalUser();
            if (localUser != null) {
                userName = localUser.getName();
            }
        }
        return userName;
    }

    public String getModeratorAccessToken() {
        // TODO
        return moderatorAccessToken;
    }


    public int fetchAccentColor() {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }
}
