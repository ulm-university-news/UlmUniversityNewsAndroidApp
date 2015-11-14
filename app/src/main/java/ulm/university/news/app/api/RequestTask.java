package ulm.university.news.app.api;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The RequestTask is used to send a request to the REST server. This task runs in a separate thread in the
 * background. This class uses two callback interfaces to notify the caller once the response is received.
 *
 * @author Matthias Mak
 */
public class RequestTask implements Runnable {
    /** This classes tag for logging. */
    private static final String TAG = "RequestTask";
    /** The RequestCallback interface used to notify the caller of the request. */
    private RequestCallback rCallback;
    /** The ErrorCallback interface used to notify the caller of the request. */
    private ErrorCallback eCallback;
    /** The http method which should be used to perform the request. */
    private String httpMethod;
    /** The url to which the connection should be established. */
    private String url;
    /** The access token of the local user. */
    private String accessToken;
    /** The content of the request. */
    private String body;

    /**
     * Creates a new instance of RequestTask with the given parameters.<br>
     * To run the task, at least one of the following parameters must be set: accessToken, body.
     *
     * @param rCallback The caller of the request who handles the successful response.
     * @param eCallback The caller of the request who handles the error response.
     * @param httpMethod The http method which should be used to perform the request.
     * @param url The url to which the connection should be established.
     */
    public RequestTask(RequestCallback rCallback, ErrorCallback eCallback, String httpMethod, String url) {
        this.rCallback = rCallback;
        this.eCallback = eCallback;
        this.httpMethod = httpMethod;
        this.url = url;
    }

    @Override
    public void run() {
        // Check necessary parameters.
        if (httpMethod == null || url == null || (accessToken == null && body == null)) {
            Log.e(TAG, "Missing parameters. Method aborted.");
            return;
        }
        // Execute request and retrieve json String.
        String json = executeRequest();
        // Notify caller.
        if (json == null || json.contains("errorCode")) {
            // A server error has occurred.
            eCallback.onServerError(json);
        } else {
            // No error has occurred.
            rCallback.onResponse(json);
        }
    }

    /**
     * Establishes an HttpUrlConnection to the given url. Uses given http method, access token and body to send the
     * request. Retrieves the response content as a Stream, which it returns as a String.
     *
     * @return The response content.
     */
    private String executeRequest() {
        InputStream is = null;
        try {
            URL myUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) myUrl.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod(httpMethod);
            conn.setDoInput(true);
            conn.setDoInput(true);

            if (accessToken != null) {
                // If access token is given, set it as authorization header.
                conn.setRequestProperty("Authorization", accessToken);
            }

            if (body != null) {
                // If body is given, send it as JSON data.
                conn.setRequestProperty("Content-Type", "application/json");
                OutputStream os = conn.getOutputStream();
                os.write(body.getBytes("UTF-8"));
                os.close();
            }

            // Starts the the connection and executes the query.
            conn.connect();

            // Get http status code.
            int responseCode = conn.getResponseCode();
            Log.d(TAG, "responseCode: " + responseCode);

            // If response code indicates an error, use ErrorStream instead of InputStream to read response.
            if (responseCode > 299) {
                is = conn.getErrorStream();
            } else {
                is = conn.getInputStream();
            }

            // Convert InputStream to String.
            return readStream(is);
        } catch (IOException e) {
            Log.e(TAG, "errorMessage: " + e.getMessage());
            return null;
        } finally {
            // Makes sure that the InputStream is closed after the app is finished using it.
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "Unable to close InputStream.");
                }
            }
        }
    }

    /**
     * Reads an InputStream and converts it to a String.
     *
     * @param inputStream The InputStream which should be converted to a String.
     * @return The String generated from InputStream.
     * @throws IOException If InputStream couldn't be converted to String.
     */
    private String readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return new String(baos.toByteArray(), "UTF-8");
    }

    @Override
    public String toString() {
        return "RequestTask{" +
                "httpMethod='" + httpMethod + '\'' +
                ", url='" + url + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", body='" + body + '\'' +
                '}';
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
