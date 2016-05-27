package ulm.university.news.app.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.data.User;
import ulm.university.news.app.util.Util;

/**
 * The GroupAPI is responsible for sending requests regarding the group resource. Required data is handed over from
 * calling controller (e.g. Activity). After the request was executed, the API parses the response data. On  success
 * a corresponding data object, on failure a ServerError will be created. Then the API uses the EventBus to  send the
 * response object and to notify the controller which called the API method. The controller accesses the object from
 * the EventBus which was delivered by the API. The controller will handle the response according to the type of the
 * response object.
 *
 * @author Matthias Mak
 */
public class GroupAPI extends MainAPI {
    /** This classes tag for logging. */
    private static final String TAG = "GroupAPI";
    /** The reference for the GroupAPI Singleton class. */
    private static GroupAPI _instance;
    /** The REST servers internet address pointing to the group resource. */
    private String serverAddressGroup;

    // Constants.
    public static final String JOIN_GROUP = "joinGroup";
    public static final String LEAVE_GROUP = "removeUserFromGroup";
    public static final String REMOVE_USER_FROM_GROUP = "removeUserFromGroup";
    public static final String DELETE_GROUP = "deleteGroup";

    /**
     * Get the instance of the GroupAPI class.
     *
     * @return Instance of GroupAPI.
     */
    public static synchronized GroupAPI getInstance(Context context) {
        if (_instance == null) {
            _instance = new GroupAPI(context);
        }
        return _instance;
    }

    /**
     * Creates an instance of GroupAPI and initialises values.
     *
     * @param context The context within the GroupAPI is used.
     */
    private GroupAPI(Context context) {
        super(context);
        serverAddressGroup = serverAddress + "/group";
    }

    public void getGroup(int groupId) {
        // Add id to url.
        String url = serverAddressGroup + "/" + groupId;

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                // Use a list of groups as deserialization type.
                Group group = gson.fromJson(json, Group.class);
                EventBus.getDefault().post(group);
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_GET, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    public void getGroups(String groupName, String groupType) {
        HashMap<String, String> params = new HashMap<>();
        if (groupName != null) {
            params.put("groupName", groupName);
        }
        if (groupType != null) {
            params.put("groupType", groupType);
        }
        // Add parameters to url.
        String url = serverAddressGroup + getUrlParams(params);

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                // Use a list of groups as deserialization type.
                Type listType = new TypeToken<List<Group>>() {
                }.getType();
                List<Group> groups = gson.fromJson(json, listType);
                EventBus.getDefault().post(groups);
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_GET, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * Creates a new group on the server. The data of the new group is provided within the group object. The
     * generated group resource will be converted to a group object and will be passed to the controller.
     *
     * @param group The group object including the data of the new group.
     */
    public void createGroup(Group group) {
        // Parse group object to JSON String.
        String jsonGroup = gson.toJson(group, Group.class);

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                Group groupResponse = gson.fromJson(json, Group.class);
                EventBus.getDefault().post(groupResponse);
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_POST, serverAddressGroup);
        rTask.setBody(jsonGroup);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * Deletes the group with given id from the server.
     *
     * @param groupId The id of the group which should be deleted.
     */
    public void deleteGroup(int groupId) {
        // Add group id to url.
        String url = serverAddressGroup + "/" + groupId;
        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                EventBus.getDefault().post(new BusEvent(DELETE_GROUP, null));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_DELETE, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * Adds the local user to the group with given id.
     *
     * @param groupId The group id.
     * @param password The group password which is required to enter the group.
     */
    public void joinGroup(int groupId, String password) {
        // Add group id to url.
        String url = serverAddressGroup + "/" + groupId + "/user";
        String body = "{\"password\":\"" + password + "\"}";

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                EventBus.getDefault().post(new BusEvent(JOIN_GROUP, null));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_POST, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        rTask.setBody(body);
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * Deletes the local user as a group member from the group with given id.
     *
     * @param groupId The group id.
     */
    public void leaveGroup(int groupId) {
        // Add channel id to url.
        String url = serverAddressGroup + "/" + groupId + "/user/" + Util.getInstance(context).getLocalUser().getId();

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                EventBus.getDefault().post(new BusEvent(LEAVE_GROUP, null));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_DELETE, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * Deletes the user identified by id as a group member from the group with given id.
     *
     * @param groupId The group id.
     * @param userId The user who should be removed from the group.
     */
    public void removeUserFromGroup(int groupId, int userId) {
        // Add channel id to url.
        String url = serverAddressGroup + "/" + groupId + "/user/" + userId;

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                EventBus.getDefault().post(new BusEvent(REMOVE_USER_FROM_GROUP, null));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_DELETE, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    public void getGroupMembers(int groupId) {
        // Add group id to url.
        String url = serverAddressGroup + "/" + groupId + "/user";

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                // Use a list of groups as deserialization type.
                Type listType = new TypeToken<List<User>>() {
                }.getType();
                List<User> users = gson.fromJson(json, listType);
                EventBus.getDefault().post(new BusEventGroupMembers(users));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_GET, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }
}
