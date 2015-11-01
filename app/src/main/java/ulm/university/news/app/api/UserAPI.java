package ulm.university.news.app.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import ulm.university.news.app.data.LocalUser;
import ulm.university.news.app.util.Constants;
import ulm.university.news.app.controller.CreateAccountActivity;

/**
 * The UserAPI is responsible for sending requests regarding the user resource. Required data ist handed over from
 * calling controller (Activity). After the request has been executed successfully, the UserAPI calls the
 * corresponding controller method and delivers the response data. If the execution of the request has failed for
 * whatever reasons, the ServerError is passed to the controller where it will be handled.
 *
 * @author Matthias Mak
 */
public class UserAPI extends MainAPI {

    /** This classes tag for logging. */
    private static final String LOG_TAG = "UserAPI";

    /** The REST servers internet address pointing to the user resource. */
    private String serverAddressUser;

    /** The Gson object used to parse from an to JSON. */
    private Gson gson;

    // TODO Implement API class as Singleton?
    // TODO Pass context on each method call to prevent memory leaks?
    // http://stackoverflow.com/questions/7880657/best-practice-to-pass-context-to-non-activity-classes
    // #comment9617210_7880657

    /**
     * Creates an instance of UserAPI and initialises values.
     *
     * @param context The context (Activity) within the UserAPI is used.
     */
    public UserAPI(Context context) {
        super(context);
        gson = new Gson();
        serverAddressUser = serverAddress + "/user";
    }

    /**
     * Creates a new localUser account on the server. The data of the new localUser is provided within the localUser object. The
     * generated localUser resource will be converted to a localUser object and will be passed to the controller.
     *
     * @param localUser The localUser object including the data of the new localUser.
     */
    public void createLocalUser(final LocalUser localUser) {
        // Parse localUser object to JSON String.
        String jsonUser = gson.toJson(localUser, LocalUser.class);
        Log.d(LOG_TAG, "jsonUser: " + jsonUser);

        RequestTask getUserTask = new RequestTask() {
            @Override
            protected void onPostExecute(String jsonResult) {
                // Check if a server error has occurred.
                if (!hasServerErrorOccurred(jsonResult)) {
                    // No error occurred. Parse JSON String to localUser object.
                    LocalUser localUserS = gson.fromJson(jsonResult, LocalUser.class);
                    ((CreateAccountActivity) context).createLocalUser(localUserS);
                }
            }
        };
        // Send request to server.
        getUserTask.execute(METHOD_POST, serverAddressUser, null, jsonUser);
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
