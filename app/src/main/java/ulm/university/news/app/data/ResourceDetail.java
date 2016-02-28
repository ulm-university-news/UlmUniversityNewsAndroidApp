package ulm.university.news.app.data;

/**
 * This class holds information to represent a resources details with name, value and icon.
 *
 * @author Matthias Mak
 */
public class ResourceDetail {
    private String name;
    private String value;
    private int resource;

    public ResourceDetail(String name, String value, int resource) {
        this.name = name;
        this.value = value;
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "ResourceDetail{" +
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
