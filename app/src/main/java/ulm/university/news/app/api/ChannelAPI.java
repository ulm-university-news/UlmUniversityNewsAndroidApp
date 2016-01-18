package ulm.university.news.app.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.data.Announcement;
import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Moderator;

/**
 * The ChannelAPI is responsible for sending requests regarding the channel resource. Required data is handed over from
 * calling controller (e.g. Activity). After the request was executed, the API parses the response data. On  success
 * a corresponding data object, on failure a ServerError will be created. Then the API uses the EventBus to  send the
 * response object and to notify the controller which called the API method. The controller accesses the object from
 * the EventBus which was delivered by the API. The controller will handle the response according to the type of the
 * response object.
 *
 * @author Matthias Mak
 */
public class ChannelAPI extends MainAPI {
    /** This classes tag for logging. */
    private static final String TAG = "ChannelAPI";
    /** The reference for the ChannelAPI Singleton class. */
    private static ChannelAPI _instance;
    /** The REST servers internet address pointing to the channel resource. */
    private String serverAddressChannel;

    // Constants.
    public static final String GET_CHANNEL = "getChannel";
    public static final String UPDATE_CHANNEL = "updateChannel";
    public static final String GET_CHANNELS = "getChannels";
    public static final String SUBSCRIBE_CHANNEL = "subscribeChannel";
    public static final String UNSUBSCRIBE_CHANNEL = "unsubscribeChannel";
    public static final String GET_RESPONSIBLE_MODERATORS = "getResponsibleModerators";

    /**
     * Get the instance of the ChannelAPI class.
     *
     * @return Instance of ChannelAPI.
     */
    public static synchronized ChannelAPI getInstance(Context context) {
        if (_instance == null) {
            _instance = new ChannelAPI(context);
        }
        return _instance;
    }

    /**
     * Creates an instance of ChannelAPI and initialises values.
     *
     * @param context The context within the ChannelAPI is used.
     */
    private ChannelAPI(Context context) {
        super(context);
        serverAddressChannel = serverAddress + "/channel";
    }

    public void getChannel(int channelId) {
        // Add channel id to url.
        String url = serverAddressChannel + "/" + channelId;
        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                Channel channel = gson.fromJson(json, Channel.class);
                // Use BusEvent to add an action which identifies this method.
                EventBus.getDefault().post(new BusEvent(GET_CHANNEL, channel));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_GET, url);
        rTask.setAccessToken(accessToken);
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    public void getChannels(Integer moderatorId, String lastUpdated) {
        HashMap<String, String> params = new HashMap<>();
        if (moderatorId != null) {
            params.put("moderatorId", moderatorId.toString());
        }
        if (lastUpdated != null) {
            params.put("lastUpdated", lastUpdated);
        }
        // Add parameters to url.
        String url = serverAddressChannel + getUrlParams(params);

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                // Use a list of channels as deserialization type.
                Type listType = new TypeToken<List<Channel>>() {
                }.getType();
                List<Channel> channels = gson.fromJson(json, listType);
                EventBus.getDefault().post(channels);
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_GET, url);
        rTask.setAccessToken(accessToken);
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    public void subscribeChannel(int channelId) {
        // Add channel id to url.
        String url = serverAddressChannel + "/" + channelId + "/user";

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                EventBus.getDefault().post(new BusEvent(SUBSCRIBE_CHANNEL, null));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_POST, url);
        rTask.setAccessToken(accessToken);
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    public void unsubscribeChannel(int channelId) {
        // Add channel id to url.
        String url = serverAddressChannel + "/" + channelId + "/user";

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                EventBus.getDefault().post(new BusEvent(UNSUBSCRIBE_CHANNEL, null));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_DELETE, url);
        rTask.setAccessToken(accessToken);
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    public void getAnnouncements(int channelId, Integer messageNumber) {
        // Create url to specific channel and point to announcement resource.
        String url = serverAddressChannel + "/" + channelId + "/announcement";
        HashMap<String, String> params = new HashMap<>();
        if (messageNumber != null) {
            params.put("messageNr", messageNumber.toString());
        }
        // Add parameters to url.
        url += getUrlParams(params);

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                // Use a list of announcements as deserialization type.
                Type listType = new TypeToken<List<Announcement>>() {
                }.getType();
                List<Announcement> announcements = gson.fromJson(json, listType);
                EventBus.getDefault().post(announcements);
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_GET, url);
        rTask.setAccessToken(accessToken);
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    public void getResponsibleModerators(int channelId) {
        // Create url to specific channel and point to moderator resource.
        String url = serverAddressChannel + "/" + channelId + "/moderator";

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                // Use a list of announcements as deserialization type.
                Type listType = new TypeToken<List<Moderator>>() {
                }.getType();
                List<Moderator> moderators = gson.fromJson(json, listType);
                EventBus.getDefault().post(new BusEvent(GET_RESPONSIBLE_MODERATORS ,moderators));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_GET, url);
        rTask.setAccessToken(accessToken);
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }
}