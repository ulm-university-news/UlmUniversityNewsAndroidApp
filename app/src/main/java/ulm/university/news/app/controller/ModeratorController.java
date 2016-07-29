package ulm.university.news.app.controller;

import android.content.Context;

import java.util.List;

import ulm.university.news.app.data.Moderator;
import ulm.university.news.app.manager.database.ChannelDatabaseManager;
import ulm.university.news.app.manager.database.ModeratorDatabaseManager;

/**
 * This class provides static util methods regarding the moderator resource which are often used across different
 * activities.
 *
 * @author Matthias Mak
 */
public class ModeratorController {
    /** This classes tag for logging. */
    private static final String TAG = "ModeratorController";

    public static boolean storeModerators(Context context, List<Moderator> moderators, int channelId) {
        ModeratorDatabaseManager moderatorDBM = new ModeratorDatabaseManager(context);
        ChannelDatabaseManager channelDBM = new ChannelDatabaseManager(context);

        for (Moderator m : moderators) {
            moderatorDBM.storeModerator(m);
            moderatorDBM.updateModerator(m);
            channelDBM.moderateChannel(channelId, m.getId());
            channelDBM.moderateChannel(channelId, m.getId(), m.isActive());
        }

        return true;
    }
}
