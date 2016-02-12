package ulm.university.news.app.api;

import java.util.List;

import ulm.university.news.app.data.Group;

/**
 * This is a helper class which is used to send a list of groups through the event bus.
 *
 * @author Matthias Mak
 */
public class BusEventGroups {
    private List<Group> groups;

    public BusEventGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<Group> getGroups() {
        return groups;
    }

    @Override
    public String toString() {
        return "BusEventGroups{" +
                "groups=" + groups +
                '}';
    }
}
