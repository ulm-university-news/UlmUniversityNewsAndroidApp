package ulm.university.news.app.data;

import ulm.university.news.app.data.enums.Platform;

/**
 * This class represents the local user of the app. The class contains information about the local user which is
 * relevant for certain functionalities within the application.
 *
 * @author Matthias Mak
 * @author Philipp Speidel
 */
public class LocalUser {
    /** The id of the local user. */
    private Integer id;
    /** The username of the local user. The username is used to make users identifiable in a group. */
    private String name;
    /**
     * The access token for the local user. The token of a user is unique in the whole system and unambiguously
     * identifies the user.
     */
    private String serverAccessToken;
    /**
     * The push token is used to identify the local user, or rather his device, in the notification service of the
     * corresponding platform. It is required for sending push notifications to the user's device.
     */
    private String pushAccessToken;
    /**
     * The platform indicates which operating system runs on the user's device. This information is required to use
     * the correct push notification service for sending the notifications to the device.
     */
    private Platform platform;

    /**
     * Creates an instance of the LocalUser class.
     */
    public LocalUser() {
    }

    /**
     * Creates an instance of the LocalUser class.
     *
     * @param name            The username of the user.
     * @param pushAccessToken The push access token which identifies the user in the push notification service.
     * @param platform        The platform of the user's device.
     */
    public LocalUser(String name, String pushAccessToken, Platform platform) {
        this.name = name;
        this.pushAccessToken = pushAccessToken;
        this.platform = platform;
    }

    /**
     * Creates an instance of the LocalUser class.
     *
     * @param id                The id of the user.
     * @param name              The username of the user.
     * @param serverAccessToken The access token which is assigned to this user.
     * @param pushAccessToken   The push access token which identifies the user in the push notification service.
     * @param platform          The platform of the user's device.
     */
    public LocalUser(Integer id, String name, String serverAccessToken, String pushAccessToken, Platform platform) {
        this.id = id;
        this.name = name;
        this.serverAccessToken = serverAccessToken;
        this.pushAccessToken = pushAccessToken;
        this.platform = platform;
    }

    @Override
    public String toString() {
        return "LocalUser{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", serverAccessToken='" + serverAccessToken + '\'' +
                ", pushAccessToken='" + pushAccessToken + '\'' +
                ", platform=" + platform +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
