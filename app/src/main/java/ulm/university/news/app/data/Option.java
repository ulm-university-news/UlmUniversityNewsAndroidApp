package ulm.university.news.app.data;

import java.util.List;

/**
 * The Option class represents an option of a ballot. An option always belongs to a ballot. The ballot can have
 * multiple options for which users can createVote. The option consists of a text and can contain a list of ids of users
 * who have voted for the specific option.
 *
 * @author Matthias Mak
 * @author Philipp Speidel
 */
public class Option {

    /** The unique id of the option. */
    private int id;
    /** The text of the option. */
    private String text;
    /** A list of ids of users who have voted for this option in the corresponding ballot. */
    private List<Integer> voters;

    /**
     * Creates an instance of the Option class.
     */
    public Option() {
    }

    /**
     * Creates an instance of the Option class.
     *
     * @param text The text of the option.
     */
    public Option(String text) {
        this.text = text;
    }

    /**
     * Creates an instance of the Option class.
     *
     * @param id The id of the option.
     * @param text The text of the option.
     */
    public Option(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Integer> getVoters() {
        return voters;
    }

    public void setVoters(List<Integer> voters) {
        this.voters = voters;
    }

    @Override
    public String toString() {
        return "Option{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", voters=" + voters +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Option)) return false;

        Option option = (Option) o;

        if (id != option.id) return false;
        if (text != null ? !text.equals(option.text) : option.text != null) return false;
        return voters != null ? voters.equals(option.voters) : option.voters == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (voters != null ? voters.hashCode() : 0);
        return result;
    }
}
