package ulm.university.news.app.api;

import android.content.Context;
import android.util.Log;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Moderator;
import ulm.university.news.app.util.Constants;
import ulm.university.news.app.util.Util;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public abstract class MainAPI implements ErrorCallback {
    /** This classes tag for logging. */
    private static final String TAG = "MainAPI";
    /** The context within the MainAPI is used. */
    protected Context context;
    /** The Gson object used to parse from an to JSON. */
    protected Gson gson;
    /** The REST servers internet root address. */
    protected String serverAddress;
    /** The local users server access token. */
    protected String userAccessToken = null;
    /** The local moderators server access token. */
    protected String moderatorAccessToken = null;
    /** The server access token either of the local user or the local moderator. */
    protected String accessToken;
    /** The http method GET. */
    protected final static String METHOD_GET = "GET";
    /** The http method POST. */
    protected final static String METHOD_POST = "POST";
    /** The http method PATCH. */
    protected final static String METHOD_PATCH = "PATCH";
    /** The http method DELETE. */
    protected final static String METHOD_DELETE = "DELETE";

    public MainAPI(Context context) {
        this.context = context.getApplicationContext();
        // Make sure, channel class or appropriate channel subclass is (de)serialized properly.
        ChannelDeserializer cd = new ChannelDeserializer();
        // Make sure, dates are (de)serialized properly.
        gson = Converters.registerDateTime(new GsonBuilder()).registerTypeAdapter(Channel.class, cd).create();
        initServerAddress();
        userAccessToken = Util.getInstance(context).getUserAccessToken();
        Moderator moderator = Util.getInstance(context).getLoggedInModerator();
        if(moderator != null){
            moderatorAccessToken = moderator.getServerAccessToken();
        }
        // Use the access token of the local moderator if available (logged in).
        accessToken = moderatorAccessToken;
        if (accessToken == null) {
            // Otherwise, use the access token of the local user.
            accessToken = userAccessToken;
        }
    }

    /**
     * Gets the REST servers internet root address.
     *
     * @return The internet address.
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * Reads the properties file which contains information about the REST server. Sets the REST servers internet
     * root address.
     */
    private void initServerAddress() {
        Properties serverInfo = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream("assets/Server.properties");
        if (input == null) {
            Log.e(TAG, "Could not localize the file Server.properties.");
        }
        try {
            serverInfo.load(input);
        } catch (IOException e) {
            Log.e(TAG, "Failed to load the server properties.");
        }
        serverAddress = serverInfo.getProperty("serverAddress");
        if (serverAddress == null) {
            Log.e(TAG, "Couldn't read server address of properties object.");
        }
    }

    protected String getUrlParams(HashMap<String, String> params) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                first = false;
                result.append("?");
            } else {
                result.append("&");
            }
            try {
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.d(TAG, "getUrlParams: UnsupportedEncodingException");
                return null;
            }
        }
        return result.toString();
    }

    @Override
    public void onServerError(String response) {
        ServerError serverError = null;
        if (response == null || response.contains("Tomcat")) {
            // Failed to connect to server.
            serverError = new ServerError(503, Constants.CONNECTION_FAILURE);
        } else if (response.contains("errorCode")) {
            // Parse JSON String to ServerError object.
            serverError = gson.fromJson(response, ServerError.class);
        }
        // Send server error
        EventBus.getDefault().post(serverError);
    }
}
