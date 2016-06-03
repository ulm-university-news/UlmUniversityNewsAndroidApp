package ulm.university.news.app.api;

import java.util.List;

import ulm.university.news.app.data.Ballot;

/**
 * This is a helper class which is used to send a list of ballots through the event bus.
 *
 * @author Matthias Mak
 */
public class BusEventBallots {
    private List<Ballot> ballots;

    public BusEventBallots(List<Ballot> ballots) {
        this.ballots = ballots;
    }

    public List<Ballot> getBallots() {
        return ballots;
    }

    public void setBallots(List<Ballot> ballots) {
        this.ballots = ballots;
    }

    @Override
    public String toString() {
        return "BusEventBallots{" +
                "ballots=" + ballots +
                '}';
    }
}
