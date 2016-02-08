package ulm.university.news.app.data.enums;

/**
 * The Notification defines which new messages should be announced.
 *
 * @author Matthias Mak
 * @author Philipp Speidel
 */
public enum Notification {
    PRIORITY, ALL, NONE;

    public static final Notification values[] = values();
}
