package ulm.university.news.app.data.enums;

/**
 * The notification settings define which new messages should be announced.
 *
 * @author Matthias Mak
 * @author Philipp Speidel
 */
public enum NotificationSettings {
    ALL, PRIORITY, NONE, GENERAL;

    public static final NotificationSettings values[] = values();
}
