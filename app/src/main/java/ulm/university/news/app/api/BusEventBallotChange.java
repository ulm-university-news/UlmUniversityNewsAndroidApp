package ulm.university.news.app.api;

import ulm.university.news.app.data.Ballot;

/**
 * This is a helper class which is used to send a updated ballot through the event bus.
 *
 * @author Matthias Mak
 */
public class BusEventBallotChange {
    private Ballot ballot;

    public BusEventBallotChange(Ballot ballot) {
        this.ballot = ballot;
    }

    public Ballot getBallot() {
        return ballot;
    }

    public void setBallot(Ballot ballot) {
        this.ballot = ballot;
    }

    @Override
    public String toString() {
        return "BusEventBallotChange{" +
                "ballot=" + ballot +
                '}';
    }
}
