package ulm.university.news.app.data.enums;

/**
 * The order settings define how different elements of the app are arranged.
 *
 * @author Matthias Mak
 * @author Philipp Speidel
 */
public enum OrderSettings {
    DESCENDING, ASCENDING, ALPHABETICAL, TYPE, TYPE_AND_FACULTY, TYPE_AND_MSG_AMOUNT, NEW_MESSAGES, LATEST_DATE,
    LATEST_VOTE;

    public static final OrderSettings values[] = values();
}
