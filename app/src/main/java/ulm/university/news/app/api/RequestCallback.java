package ulm.university.news.app.api;

/**
 * This interface is used for a callback mechanism between the RequestTask and an API class.
 *
 * @author Matthias Mak
 */
public interface RequestCallback {
    /**
     * Called when a request was received successfully.
     *
     * @param json The response body as JSON String.
     */
    void onResponse(String json);
}
