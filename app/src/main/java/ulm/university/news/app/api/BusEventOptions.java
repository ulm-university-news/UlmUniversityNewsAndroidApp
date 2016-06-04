package ulm.university.news.app.api;

import java.util.List;

import ulm.university.news.app.data.Option;

/**
 * This is a helper class which is used to send a list of options through the event bus.
 *
 * @author Matthias Mak
 */
public class BusEventOptions {
    private List<Option> options;

    public BusEventOptions(List<Option> options) {
        this.options = options;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "BusEventOptions{" +
                "options=" + options +
                '}';
    }
}
