/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ulm.university.news.app.util;

import android.util.Log;

import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The Constants class provides a variety of application information.
 */
public class Constants {

    /** The reference for the Constants Singleton class. */
    private static Constants _instance;

    /** This classes tag for logging. */
    private static final String LOG_TAG = "Constants";

    // PushManager:
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String PUSH_TOKEN_CREATED = "pushTokenCreated";

    // Error codes:
    // General:
    public static final int CONNECTION_FAILURE = 9999;

    /** The REST servers internet root address. */
    private String serverAddress = null;

    /** The time zone where the server is located. */
    public static final DateTimeZone TIME_ZONE = DateTimeZone.forID("Europe/Berlin");

    /**
     * Get an instance of the Constants class.
     *
     * @return Instance of Constants.
     */
    public static synchronized Constants getInstance() {
        if (_instance == null) {
            _instance = new Constants();
        }
        return _instance;
    }

    /**
     * Gets the REST servers internet root address.
     *
     * @return The internet address.
     */
    public String getServerAddress() {
        if (serverAddress == null) {
            Properties serverInfo = retrieveServerInfo();
            if (serverInfo != null) {
                serverAddress = serverInfo.getProperty("serverAddress");
            }
            if (serverAddress == null) {
                Log.e(LOG_TAG, "Couldn't read server address of properties object.");
                return null;
            }
        }
        return serverAddress;
    }

    /**
     * Reads the properties file which contains information about the REST server. Returns the properties in a
     * Properties object.
     *
     * @return Properties object, or null if reading of the properties file has failed.
     */
    private Properties retrieveServerInfo() {
        Properties serverInfo = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream("assets/Server.properties");
        if (input == null) {
            Log.e(LOG_TAG, "Could not localize the file Server.properties.");
            return null;
        }
        try {
            serverInfo.load(input);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to load the server properties.");
            return null;
        }
        return serverInfo;
    }

}
