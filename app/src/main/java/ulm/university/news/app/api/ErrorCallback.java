package ulm.university.news.app.api;

/**
 * This interface is used for a callback mechanism between the RequestTask and an API class.
 *
 * @author Matthias Mak
 */
public interface ErrorCallback {
    /**
     * Called when an error occurred on the RequestTask.
     *
     * @param response The response body as String.
     */
    void onServerError(String response);
}
