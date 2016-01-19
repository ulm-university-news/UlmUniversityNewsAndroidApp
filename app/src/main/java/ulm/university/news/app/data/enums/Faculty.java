package ulm.university.news.app.data.enums;

/**
 * The Faculty to which the Lecture belongs. Possible values are NATURAL_SCIENCES,
 * ENGINEERING_COMPUTER_SCIENCE_PSYCHOLOGY, MATHEMATICS_ECONOMICS and MEDICINES.
 *
 * @author Matthias Mak
 * @author Philipp Speidel
 */
public enum Faculty {
    ENGINEERING_COMPUTER_SCIENCE_PSYCHOLOGY, MATHEMATICS_ECONOMICS, MEDICINES, NATURAL_SCIENCES;

    public static final Faculty values[] = values();
}
