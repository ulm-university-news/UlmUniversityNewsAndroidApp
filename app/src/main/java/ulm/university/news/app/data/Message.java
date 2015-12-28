package ulm.university.news.app.data;

import org.joda.time.DateTime;

import ulm.university.news.app.data.enums.Priority;

import static ulm.university.news.app.util.Constants.TIME_ZONE;

/**
 * The Message class represents a general message. A message contains a message text, a number and was created at a
 * certain date and time. Each message is sent with a defined priority.
 *
 * @author Matthias Mak
 * @author Philipp Speidel
 */
public class Message {

    /** The unique id of the message. */
    protected int id;
    /** The text of the message. */
    protected String text;
    /** The number of the message regarding their parent-resource. Messages belonging to a conversation or a channel
     are numbered in ascending order in terms of their creation in the corresponding resource. */
    protected int messageNumber;
    /** The date and time when the message was created. */
    protected DateTime creationDate;
    /** The priority of the message. */
    protected Priority priority;
    /** A flag that indicates weather the message was read or not. */
    boolean read;

    /**
     * Creates an instance of the Message class.
     */
    public Message(){

    }

    /**
     * Creates an instance of the Message class.
     *
     * @param text The text of the message.
     * @param messageNumber The number of the message regarding their parent-resource.
     * @param creationDate The date and time when the message was created.
     * @param priority The priority of the message.
     */
    public Message(String text, int messageNumber, DateTime creationDate, Priority priority){
        this.text = text;
        this.messageNumber = messageNumber;
        this.creationDate = creationDate;
        this.priority = priority;
    }

    /**
     * Creates an instance of the Message class.
     *
     * @param id The id of the message.
     * @param text The text of the message.
     * @param messageNumber The number of the message regarding their parent-resource.
     * @param creationDate The date and time when the message was created.
     * @param priority The priority of the message.
     * @param read Weather the message was already read or not.
     */
    public Message(int id, String text, int messageNumber, DateTime creationDate, Priority priority, boolean read){
        this.id = id;
        this.text = text;
        this.messageNumber = messageNumber;
        this.creationDate = creationDate;
        this.priority = priority;
        this.read = read;
    }

    /**
     * Computes the creation date of the Message. If the creation date is already set, this method does nothing.
     */
    public void computeCreationDate(){
        if (creationDate == null) {
            creationDate = DateTime.now(TIME_ZONE);
        }
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

    public DateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(DateTime creationDate) {
        this.creationDate = creationDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public void setMessageNumber(int messageNumber) {
        this.messageNumber = messageNumber;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", messageNumber=" + messageNumber +
                ", creationDate=" + creationDate +
                ", priority=" + priority +
                ", read=" + read +
                '}';
    }
}
