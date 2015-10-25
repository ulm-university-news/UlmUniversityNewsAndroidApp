package ulm.university.news.app.api;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The RequestTask sends a request build with given parameters to the REST server. The parameters have to be in the
 * following order: httpMethod, url (including url params), accessToken, body (JSON as String)
 *
 * @author Matthias Mak
 */
public class RequestTask extends AsyncTask<String, Void, String> {

    /** This classes tag for logging. */
    private static final String LOG_TAG = "RequestTask";

    @Override
    protected String doInBackground(String... params) {
        // Parameters have to be in the following order:
        // httpMethod, url (including url params), accessToken, body (JSON as String)
        if (params.length < 3) {
            Log.e(LOG_TAG, "Missing parameters. Method aborted.");
            return null;
        } else if (params.length > 4) {
            Log.e(LOG_TAG, "To many parameters. Method aborted.");
            return null;
        }
        // Assign corresponding parameters.
        String httpMethod = params[0];
        String url = params[1];
        String accessToken = params[2];
        String body = null;
        if (params.length == 4) {
            body = params[3];
        }
        Log.d(LOG_TAG, "httpMethod:" + httpMethod + ", url:" + url + ", accessToken:" + accessToken + ", body:" + body);

        // Execute request and return response as String.
        return executeRequest(httpMethod, url, accessToken, body);
    }

    /**
     * Establishes an HttpUrlConnection to the given url. Uses given http method, access token and body to send the
     * request. Retrieves the response content as a Stream, which it returns as a String.
     *
     * @param httpMethod The http method which should be used to perform the request.
     * @param url The url to which the connection should be established.
     * @param accessToken The access token of the user.
     * @param body The content of the request.
     * @return The response content.
     */
    private String executeRequest(String httpMethod, String url, String accessToken, String body) {
        InputStream is = null;
        try {
            URL myUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) myUrl.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod(httpMethod);
            conn.setRequestProperty("Authorization", accessToken);
            conn.setDoInput(true);
            conn.setDoInput(true);

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
            Log.d(LOG_TAG, "responseCode: " + responseCode);

            // If response code indicates an error, use ErrorStream instead of InputStream to read response.
            if (responseCode > 299) {
                is = conn.getErrorStream();
            } else {
                is = conn.getInputStream();
            }

            // Convert InputStream to String.
            String responseBody = readStream(is);
            Log.d(LOG_TAG, "responseBody: " + responseBody);
            return responseBody;
        } catch (IOException e) {
            Log.e(LOG_TAG, "errorMessage: " + e.getMessage());
            return null;
        } finally {
            // Makes sure that the InputStream is closed after the app is finished using it.
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Unable to close InputStream.");
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
}
