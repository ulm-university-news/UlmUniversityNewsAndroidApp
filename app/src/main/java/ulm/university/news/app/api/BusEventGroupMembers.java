package ulm.university.news.app.api;

import java.util.List;

import ulm.university.news.app.data.User;

/**
 * This is a helper class which is used to send a list of users through the event bus.
 *
 * @author Matthias Mak
 */
public class BusEventGroupMembers {
    private List<User> users;

    public BusEventGroupMembers(List<User> users) {
        this.users = users;
    }

    public List<User> getUsers() {
        return users;
    }

    @Override
    public String toString() {
        return "BusEventGroupMembers{" +
                "users=" + users +
                '}';
    }
}
