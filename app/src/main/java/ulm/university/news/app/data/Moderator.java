package ulm.university.news.app.data;

import java.util.Locale;

import ulm.university.news.app.data.enums.Language;

/**
 * This class represents the Moderator of the application. A Moderator manages channels. In addition to his
 * capabilities as a Moderator he can also be an administrator with additional rights.
 *
 * @author Matthias Mak
 * @author Philipp Speidel
 */
public class Moderator {
    /** The unique id of the Moderator. */
    int id;
    /** The unique user name of the Moderator. */
    String name;
    /** The first name of the Moderator. */
    String firstName;
    /** The last name of the Moderator. */
    String lastName;
    /** The email address of the Moderator. */
    String email;
    /** The access token of the Moderator. The token is unique and unambiguously identifies the moderator. */
    String serverAccessToken;
    /** The password of the Moderator. */
    String password;
    /** The motivation of the account application. */
    String motivation;
    /** The preferred language of the Moderator. */
    Language language;
    /** Defines whether the Moderators account is locked or not. */
    Boolean locked;
    /** Defines whether the Moderator has admin rights or not. */
    Boolean admin;
    /** Defines whether the Moderator account is marked as deleted or not. */
    Boolean deleted;
    /** Defines whether the Moderator actively manages a certain channel or not. */
    Boolean active;

    /**
     * Empty constructor. Needed values are set with corresponding set methods.
     */
    public Moderator() {
    }

    /**
     * Creates an instance of moderator.
     *
     * @param id The unique id of the moderator.
     * @param name The unique user name of the moderator.
     * @param firstName The first name of the moderator.
     * @param lastName The last name of the Moderator.
     * @param email The email address of the Moderator.
     * @param serverAccessToken The access token of the Moderator.
     * @param password The password of the Moderator.
     * @param motivation The motivation of the account application.
     * @param language The preferred language of the Moderator.
     * @param locked Defines whether the Moderators account is locked or not.
     * @param admin Defines whether the Moderator has admin rights or not.
     * @param deleted Defines whether the Moderator account is marked as deleted or not.
     * @param active Defines whether the Moderator actively manages a certain channel or not.
     */
    public Moderator(int id, String name, String firstName, String lastName, String email, String serverAccessToken,
                     String password, String motivation, Language language, Boolean locked, Boolean admin,
                     Boolean deleted, Boolean active) {
        this.id = id;
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.serverAccessToken = serverAccessToken;
        this.password = password;
        this.motivation = motivation;
        this.language = language;
        this.locked = locked;
        this.admin = admin;
        this.deleted = deleted;
        this.active = active;
    }

    /**
     * Creates an instance of Moderator.
     *
     * @param name The unique user name of the Moderator.
     * @param firstName The first name of the Moderator.
     * @param lastName The last name of the Moderator.
     * @param email The email address of the Moderator.
     * @param serverAccessToken The access token of the Moderator.
     * @param password The password of the Moderator.
     * @param motivation The motivation of the account application.
     * @param language The preferred language of the Moderator.
     * @param locked Defines whether the Moderators account is locked or not.
     * @param admin Defines whether the Moderator has admin rights or not.
     * @param deleted Defines whether the Moderator account is marked as deleted or not.
     * @param active Defines whether the Moderator actively manages a certain channel or not.
     */
    public Moderator(String name, String firstName, String lastName, String email, String serverAccessToken,
                     String password, String motivation, Language language, Boolean locked, Boolean admin,
                     Boolean deleted, Boolean active) {
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.serverAccessToken = serverAccessToken;
        this.password = password;
        this.motivation = motivation;
        this.language = language;
        this.locked = locked;
        this.admin = admin;
        this.deleted = deleted;
        this.active = active;
    }

    /**
     * Returns the moderators language as a Locale.
     *
     * @return The Locale of the moderators language.
     */
    public Locale getLanguageAsLocale() {
        switch (language) {
            case GERMAN: return Locale.GERMAN;
            case ENGLISH: return Locale.ENGLISH;
            default: return Locale.ENGLISH;
        }
    }

    @Override
    public String toString() {
        return "Moderator{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", serverAccessToken='" + serverAccessToken + '\'' +
                ", password='" + password + '\'' +
                ", motivation='" + motivation + '\'' +
                ", language=" + language +
                ", locked=" + locked +
                ", admin=" + admin +
                ", deleted=" + deleted +
                ", active=" + active +
                '}';
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getServerAccessToken() {
        return serverAccessToken;
    }

    public void setServerAccessToken(String serverAccessToken) {
        this.serverAccessToken = serverAccessToken;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean isLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Boolean isAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public Boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
}
