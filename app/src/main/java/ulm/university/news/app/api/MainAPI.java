package ulm.university.news.app.api;

import android.content.Context;

import ulm.university.news.app.Constants;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class MainAPI {
    /** The context (Activity) within the UserAPI is used. */
    protected Context context;
    /** The REST servers internet root address. */
    protected String serverAddress;
    /** The http method GET. */
    protected final static String METHOD_GET = "GET";
    /** The http method POST. */
    protected final static String METHOD_POST = "POST";
    /** The http method PATCH. */
    protected final static String METHOD_PATCH = "PATCH";
    /** The http method DELETE. */
    protected final static String METHOD_DELETE = "DELETE";

    public MainAPI(Context context) {
        this.context = context;
        serverAddress = Constants.getInstance().getServerAddress();
    }
}
