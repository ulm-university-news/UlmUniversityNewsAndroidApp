package ulm.university.news.app.api;

import android.content.Context;

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
public class GroupAPI extends MainAPI{
    /** This classes tag for logging. */
    private static final String TAG = "GroupAPI";
    /** The reference for the GroupAPI Singleton class. */
    private static GroupAPI _instance;
    /** The REST servers internet address pointing to the group resource. */
    private String serverAddressGroup;

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
}
