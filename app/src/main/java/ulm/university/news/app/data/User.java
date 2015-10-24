package ulm.university.news.app.data;

import ulm.university.news.app.data.enums.Platform;

/**
 * This class represents an user of the application. The class contains information about the user which is relevant
 * for certain functionalities within the application.
 *
 * @author Matthias Mak
 * @author Philipp Speidel
 */
public class User {
    /** The id of the user. */
    private int id;
    /** The username of the user. The username is used to make users identifiable in a Group. */
    private String name;
    /** The old username of the user after name change. */
    private String oldName;
    /** Identifies weather the users name has changed or not. */
    private boolean nameChanged;
    /**
     * The access token for the user. The token of a user is unique in the whole system and unambiguously identifies
     * the user.
     */
    private String serverAccessToken;
    /**
     * The push token is used to identify the user, or rather his device, in the notification service of the
     * corresponding platform. It is required for sending push notifications to the user's device.
     */
    private String pushAccessToken;
    /**
     * The platform indicates which operating system runs on the user's device. This information is required to use
     * the correct push notification service for sending the notifications to the device.
     */
    private Platform platform;

    /**
     * Creates an instance of the User class.
     */
    public User() {
    }

    /**
     * Creates an instance of the User class.
     *
     * @param name            The username of the user.
     * @param pushAccessToken The push access token which identifies the user in the push notification service.
     * @param platform        The platform of the user's device.
     */
    public User(String name, String pushAccessToken, Platform platform) {
        this.name = name;
        this.pushAccessToken = pushAccessToken;
        this.platform = platform;
    }

    /**
     * Creates an instance of the User class.
     *
     * @param id                The id of the user.
     * @param name              The username of the user.
     * @param serverAccessToken The access token which is assigned to this user.
     * @param pushAccessToken   The push access token which identifies the user in the push notification service.
     * @param platform          The platform of the user's device.
     */
    public User(int id, String name, String serverAccessToken, String pushAccessToken, Platform platform) {
        this.id = id;
        this.name = name;
        this.serverAccessToken = serverAccessToken;
        this.pushAccessToken = pushAccessToken;
        this.platform = platform;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", oldName='" + oldName + '\'' +
                ", nameChanged=" + nameChanged +
                ", serverAccessToken='" + serverAccessToken + '\'' +
                ", pushAccessToken='" + pushAccessToken + '\'' +
                ", platform=" + platform +
                '}';
    }

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public boolean isNameChanged() {
        return nameChanged;
    }

    public void setNameChanged(boolean nameChanged) {
        this.nameChanged = nameChanged;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServerAccessToken() {
        return serverAccessToken;
    }

    public void setServerAccessToken(String serverAccessToken) {
        this.serverAccessToken = serverAccessToken;
    }

    public String getPushAccessToken() {
        return pushAccessToken;
    }

    public void setPushAccessToken(String pushAccessToken) {
        this.pushAccessToken = pushAccessToken;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }
}
