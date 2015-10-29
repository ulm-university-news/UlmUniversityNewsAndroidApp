package ulm.university.news.app.api;

import android.content.Context;
import android.util.Log;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import ulm.university.news.app.controller.CreateAccountActivity;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Lecture;
import ulm.university.news.app.util.Constants;

/**
 * The ChannelAPI is responsible for sending requests regarding the channel resource. Required data ist handed over from
 * calling controller (Activity). After the request has been executed successfully, the ChannelAPI calls the
 * corresponding controller method and delivers the response data. If the execution of the request has failed for
 * whatever reasons, the ServerError is passed to the controller where it will be handled.
 *
 * @author Matthias Mak
 */
public class ChannelAPI extends MainAPI {
    /** This classes tag for logging. */
    private static final String LOG_TAG = "ChannelAPI";

    /** The REST servers internet address pointing to the channel resource. */
    private String serverAddressChannel;

    /** The Gson object used to parse from an to JSON. */
    private Gson gson;

    /**
     * Creates an instance of ChannelAPI and initialises values.
     *
     * @param context The context (Activity) within the ChannelAPI is used.
     */
    public ChannelAPI(Context context) {
        super(context);
        // Make sure, channel class or appropriate channel subclass is deserialized properly.
        ChannelDeserializer cd = new ChannelDeserializer();
        // Make sure, dates are deserialized properly.
        gson = Converters.registerDateTime(new GsonBuilder()).registerTypeAdapter(Channel.class, cd).create();
        serverAddressChannel = serverAddress + "/channel";
    }

    public void getChannel(String accessToken, int channelId) {
        // Add channel id to url.
        String url = serverAddressChannel;
        url += "/" + channelId;
        Log.d(LOG_TAG, "url: " + url);
        gson = Converters.registerDateTime(new GsonBuilder()).create();

        RequestTask getUserTask = new RequestTask() {
            @Override
            protected void onPostExecute(String jsonResult) {
                // Check if a server error has occurred.
                if (!hasServerErrorOccurred(jsonResult)) {
                    // No error occurred. Parse JSON String to user object.
                    Channel channel = gson.fromJson(jsonResult, Lecture.class);
                    Log.d(LOG_TAG, channel.toString());
                    // ((CreateAccountActivity) context).createUserAccount(userS);
                }
            }
        };
        // Send request to server.
        getUserTask.execute(METHOD_GET, url, accessToken);
    }


    public void getChannels(String accessToken, Integer moderatorId, String lastUpdated) {
        // Add parameters to url.
        String url = serverAddressChannel;
        if (moderatorId != null) {
            url += "moderatorId=" + moderatorId;
        }
        if (lastUpdated != null) {
            url += "lastUpdated=" + lastUpdated;
        }
        Log.d(LOG_TAG, "url: " + url);


        RequestTask getUserTask = new RequestTask() {
            @Override
            protected void onPostExecute(String jsonResult) {
                // Check if a server error has occurred.
                if (!hasServerErrorOccurred(jsonResult)) {
                    // No error occurred. Parse JSON String to user object.
                    Type listType = new TypeToken<List<Channel>>() {}.getType();
                    // In this test code i just shove the JSON here as string.
                    List<Channel> channels = gson.fromJson(jsonResult, listType);
                    Log.d(LOG_TAG, channels.toString());
                    // ((CreateAccountActivity) context).createUserAccount(userS);
                }
            }
        };
        // Send request to server.
        getUserTask.execute(METHOD_GET, url, accessToken);
    }

    /**
     * Checks if response includes a server error. If an error has occurred it will be passed to and handled by the
     * corresponding controller method.
     *
     * @param jsonResult The result of the request as a String.
     * @return true if a ServerError has occurred.
     */
    private boolean hasServerErrorOccurred(String jsonResult) {
        Log.d(LOG_TAG, "jsonResult: " + jsonResult);
        boolean hasErrorOccurred = false;
        if (jsonResult == null) {
            // Failed to connect to server.
            hasErrorOccurred = true;
            ServerError se = new ServerError(503, Constants.CONNECTION_FAILURE);
            ((CreateAccountActivity) context).handleServerError(se);
        } else if (jsonResult.contains("errorCode")) {
            // Parse JSON String to ServerError object.
            hasErrorOccurred = true;
            ServerError se = gson.fromJson(jsonResult, ServerError.class);
            ((CreateAccountActivity) context).handleServerError(se);
        }
        return hasErrorOccurred;
    }
}