package ulm.university.news.app.api;

import android.content.Context;

/**
 * The ModeratorAPI is responsible for sending requests regarding the moderator resource. Required data is handed over
 * from calling controller (e.g. Activity). After the request was executed, the API parses the response data. On
 * success a corresponding data object, on failure a ServerError will be created. Then the API uses the EventBus to
 * send the response object and to notify the controller which called the API method. The controller accesses the
 * object from the EventBus which was delivered by the API. The controller will handle the response according to the
 * type of the response object.
 *
 * @author Matthias Mak
 */
public class ModeratorAPI extends MainAPI {
    /** This classes tag for logging. */
    private static final String TAG = "ModeratorAPI";
    /** The reference for the ModeratorAPI Singleton class. */
    private static ModeratorAPI _instance;
    /** The REST servers internet address pointing to the moderator resource. */
    private String serverAddressModerator;

    /**
     * Get the instance of the ModeratorAPI class.
     *
     * @return Instance of ModeratorAPI.
     */
    public static synchronized ModeratorAPI getInstance(Context context) {
        if (_instance == null) {
            _instance = new ModeratorAPI(context);
        }
        return _instance;
    }

    /**
     * Creates an instance of ModeratorAPI and initialises values.
     *
     * @param context The context within the ModeratorAPI is used.
     */
    private ModeratorAPI(Context context) {
        super(context);
        serverAddressModerator = serverAddress + "/moderator";
    }
}
