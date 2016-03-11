package ulm.university.news.app.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.TypedValue;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ulm.university.news.app.R;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Lecture;
import ulm.university.news.app.data.LocalUser;
import ulm.university.news.app.data.Moderator;
import ulm.university.news.app.data.Settings;
import ulm.university.news.app.data.enums.ChannelType;
import ulm.university.news.app.manager.database.SettingsDatabaseManager;
import ulm.university.news.app.manager.database.UserDatabaseManager;

/**
 * The Util class provides useful methods which are often use in several parts of the application. As a Singleton the
 * instance is kept in memory which allows fast access to often used fields like the users access token.
 *
 * @author Matthias Mak
 */
public class Util {
    /** This classes tag for logging. */
    private static final String TAG = "Util";

    /** The reference for the Util Singleton class. */
    private static Util _instance;

    /** The application context. */
    private Context context;

    /** The local user. */
    private LocalUser localUser = null;

    /** The logged in moderator. */
    private Moderator loggedInModerator = null;

    /** The server access token either of the local user or the logged in moderator. */
    private String accessToken;

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
        setCurrentAccessToken();
    }

    /**
     * Sets the access token of the logged in moderator if available (logged in). Otherwise, the access token of the
     * local user is set as the current access token.
     */
    public void setCurrentAccessToken() {
        String userAccessToken = null;
        String moderatorAccessToken = null;
        // Get tokens of local user and logged in moderator if existing.
        localUser = getLocalUser();
        if (localUser != null) {
            userAccessToken = localUser.getServerAccessToken();
        }
        if (loggedInModerator != null) {
            moderatorAccessToken = loggedInModerator.getServerAccessToken();
        }
        // Use the access token of the local moderator if available (logged in).
        accessToken = moderatorAccessToken;
        if (accessToken == null) {
            // Otherwise, use the access token of the local user.
            accessToken = userAccessToken;
        }
    }

    /**
     * Gets the access token of the local user or the logged in moderator.
     *
     * @return The current server access token.
     */
    public String getAccessToken() {
        return accessToken;
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

    public LocalUser getLocalUser() {
        if (localUser == null) {
            Log.d(TAG, "localUser is null.");
            this.localUser = new UserDatabaseManager(context).getLocalUser();
        }
        return localUser;
    }

    public void setLocalUser(LocalUser localUser) {
        this.localUser = localUser;
    }

    public void setLoggedInModerator(Moderator loggedInModerator) {
        this.loggedInModerator = loggedInModerator;
    }

    public Moderator getLoggedInModerator() {
        return loggedInModerator;
    }

    /**
     * Hashes the given password.
     *
     * @return The hashed password.
     */
    public static String hashPassword(String password) {
        String passwordHash = null;
        try {
            // Calculate hash on the given password.
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(password.getBytes());

            // Transform the bytes (8 bit signed) into a hexadecimal format.
            StringBuilder hashString = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                /*
                Format parameters: %[flags][width][conversion]
                Flag '0' - The result will be zero padded.
                Width '2' - The width is 2 as 1 byte is represented by two hex characters.
                Conversion 'x' - Result is formatted as hexadecimal integer, uppercase.
                 */
                hashString.append(String.format("%02x", hash[i]));
            }
            passwordHash = hashString.toString();
            Log.d(TAG, "Password hashed to " + passwordHash);
        } catch (NoSuchAlgorithmException e) {
            Log.d(TAG, "Couldn't hash password. The expected digest algorithm is not available.");
        }
        return passwordHash;
    }

    /**
     * Sorts the given channels alphabetically. Ignores channel type.
     *
     * @param channels The channel list.
     */
    private void sortChannelsName(List<Channel> channels) {
        Collections.sort(channels, new Comparator<Channel>() {
            public int compare(Channel c1, Channel c2) {
                return c1.getName().compareToIgnoreCase(c2.getName());
            }
        });
    }

    /**
     * Sorts the given channels according to their channel type. Channels of the same type will be sorted
     * alphabetically.
     *
     * @param channels The channel list.
     */
    private void sortChannelsType(List<Channel> channels) {
        Collections.sort(channels, new Comparator<Channel>() {
            public int compare(Channel c1, Channel c2) {
                int res = c1.getType().compareTo(c2.getType());
                if (res != 0) {
                    return res;
                }
                return c1.getName().compareToIgnoreCase(c2.getName());
            }
        });
    }

    /**
     * Sorts the given channels according to their channel type. Channels of the same type will be sorted
     * alphabetically. Lectures will be sorted will be sorted according to their faculty. Lectures of the same
     * faculty will be sorted alphabetically.
     *
     * @param channels The channel list.
     */
    private void sortChannelsTypeFaculty(List<Channel> channels) {
        Collections.sort(channels, new Comparator<Channel>() {
            public int compare(Channel c1, Channel c2) {
                int res = c1.getType().compareTo(c2.getType());
                if (res != 0) {
                    return res;
                }
                if (c1.getType().equals(ChannelType.LECTURE)) {
                    res = ((Lecture) c1).getFaculty().compareTo(((Lecture) c2).getFaculty());
                    if (res != 0) {
                        return res;
                    }
                }
                return c1.getName().compareToIgnoreCase(c2.getName());
            }
        });
    }

    /**
     * Sorts the given channel list according to the channel settings.
     *
     * @param channels The channel list.
     */
    public void sortChannels(List<Channel> channels) {
        if (channels != null) {
            // Check settings for preferred channel order.
            Settings settings = new SettingsDatabaseManager(context).getSettings();
            switch (settings.getChannelSettings()){
                case ALPHABETICAL:
                    sortChannelsName(channels);
                    break;
                case TYPE:
                    sortChannelsType(channels);
                    break;
                case TYPE_AND_FACULTY:
                    sortChannelsTypeFaculty(channels);
                    break;
            }
        }
    }

    public String getFormattedDateShort(DateTime date) {
        // Format the date for output.
        String datePattern = context.getString(R.string.general_date_pattern_short);
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern(datePattern);
        return dtfOut.print(date);
    }

    public String getFormattedDateLong(DateTime date) {
        // Format the date for output.
        String datePattern = context.getString(R.string.general_date_pattern_long);
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern(datePattern);
        return dtfOut.print(date);
    }

    public String getFormattedDateOnly(DateTime date) {
        // Format the date for output.
        String datePattern = context.getString(R.string.general_date_pattern_date_only);
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern(datePattern);
        return dtfOut.print(date);
    }

    public String getFormattedTimeOnly(DateTime date) {
        // Format the time of the date for output.
        String datePattern = context.getString(R.string.general_date_pattern_time_only);
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern(datePattern);
        return dtfOut.print(date);
    }

    public int fetchAccentColor() {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }
}
