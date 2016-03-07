package ulm.university.news.app.api;

import android.content.Context;
import android.util.Log;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.data.LocalUser;
import ulm.university.news.app.util.Util;

/**
 * The UserAPI is responsible for sending requests regarding the user resource. Required data is handed over from
 * calling controller (e.g. Activity). After the request was executed, the API parses the response data. On  success
 * a corresponding data object, on failure a ServerError will be created. Then the API uses the EventBus to  send the
 * response object and to notify the controller which called the API method. The controller accesses the object from
 * the EventBus which was delivered by the API. The controller will handle the response according to the type of the
 * response object.
 *
 * @author Matthias Mak
 */
public class UserAPI extends MainAPI {
    /** This classes tag for logging. */
    private static final String TAG = "UserAPI";
    /** The reference for the UserAPI Singleton class. */
    private static UserAPI _instance;
    /** The REST servers internet address pointing to the user resource. */
    private String serverAddressUser;

    // Constants
    public static final String CREATE_LOCAL_USER = "createLocalUser";

    /**
     * Get the instance of the UserAPI class.
     *
     * @return Instance of UserAPI.
     */
    public static synchronized UserAPI getInstance(Context context) {
        if (_instance == null) {
            _instance = new UserAPI(context);
        }
        return _instance;
    }

    /**
     * Creates an instance of UserAPI and initialises values.
     *
     * @param context The context within the UserAPI is used.
     */
    private UserAPI(Context context) {
        super(context);
        serverAddressUser = serverAddress + "/user";
    }

    /**
     * Creates a new localUser account on the server. The data of the new localUser is provided within the localUser
     * object. The generated localUser resource will be converted to a localUser object and will be passed to the
     * controller.
     *
     * @param localUser The localUser object including the data of the new localUser.
     */
    public void createLocalUser(LocalUser localUser) {
        // Parse localUser object to JSON String.
        String jsonUser = gson.toJson(localUser, LocalUser.class);

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                LocalUser localUser = gson.fromJson(json, LocalUser.class);
                EventBus.getDefault().post(localUser);
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_POST, serverAddressUser);
        rTask.setBody(jsonUser);
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * updates the local user account on the server.
     *
     * @param localUser The localUser object including the updated data of the new localUser.
     */
    public void updateLocalUser(LocalUser localUser) {
        String url = serverAddressUser + "/" + localUser.getId();
        // Parse localUser object to JSON String.
        String jsonUser = gson.toJson(localUser, LocalUser.class);

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                LocalUser localUser = gson.fromJson(json, LocalUser.class);
                EventBus.getDefault().post(localUser);
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_PATCH, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        rTask.setBody(jsonUser);
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }
}
