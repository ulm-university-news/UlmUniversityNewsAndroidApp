package ulm.university.news.app.api;

/**
 * Since the EventBus doesn't provide passing multiple parameters a helper class is required. The BusEvent is a
 * helper which wraps an object and an action. The action describes the object included in the BusEvent.
 *
 * @author Matthias Mak
 */
public class BusEvent {
    private String action;
    private Object object;

    /**
     * Creates a new BusEvent which is used to send to the EventBus.
     * @param action The action which describes the included object.
     * @param object The object which should be sent.
     */
    public BusEvent(String action, Object object) {
        this.action = action;
        this.object = object;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
