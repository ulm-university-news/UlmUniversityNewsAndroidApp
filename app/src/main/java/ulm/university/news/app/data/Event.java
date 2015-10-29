package ulm.university.news.app.data;

import org.joda.time.DateTime;

import ulm.university.news.app.data.enums.ChannelType;

/**
 * The Event class is a sub class of Channel. This class adds fields to describe an Event.
 *
 * @author Matthias Mak
 * @author Philipp Speidel
 */
public class Event extends Channel {
    /** The cost of the Event. */
    String cost;
    /** The person who organizes the Event. */
    String organizer;

    public Event() {
    }

    public Event(int id, String name, String description, ChannelType type, DateTime creationDate, DateTime
            modificationDate, String term, String locations, String dates, String contacts, String website, String
            cost, String organizer) {
        super(id, name, description, type, creationDate, modificationDate, term, locations, dates, contacts, website);
        this.cost = cost;
        this.organizer = organizer;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    @Override
    public String toString() {
        return "Event{" +
                "cost='" + cost + '\'' +
                ", organizer='" + organizer + '\'' +
                "} " + super.toString();
    }
}
