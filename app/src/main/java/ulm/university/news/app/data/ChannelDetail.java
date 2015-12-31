package ulm.university.news.app.data;

/**
 * This class holds information to represent a channel details name, value and icon.
 *
 * @author Matthias Mak
 */
public class ChannelDetail {
    private String name;
    private String value;
    private int resource;

    public ChannelDetail(String name, String value, int resource) {
        this.name = name;
        this.value = value;
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "ChannelDetail{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", resource=" + resource +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }
}
