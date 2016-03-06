package ulm.university.news.app.data;

import ulm.university.news.app.data.enums.Language;
import ulm.university.news.app.data.enums.NotificationSettings;
import ulm.university.news.app.data.enums.OrderSettings;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class Settings {
    public OrderSettings channelSettings;
    public OrderSettings conversationSettings;
    public OrderSettings groupSettings;
    public OrderSettings ballotSettings;
    public OrderSettings announcementSettings;
    public OrderSettings generalSettings;
    public Language language;
    public NotificationSettings notificationSettings;

    public Settings() {
    }

    @Override
    public String toString() {
        return "Settings{" +
                "channelSettings=" + channelSettings +
                ", conversationSettings=" + conversationSettings +
                ", groupSettings=" + groupSettings +
                ", ballotSettings=" + ballotSettings +
                ", announcementSettings=" + announcementSettings +
                ", generalSettings=" + generalSettings +
                ", language=" + language +
                ", notificationSettings=" + notificationSettings +
                '}';
    }

    public OrderSettings getChannelSettings() {
        return channelSettings;
    }

    public void setChannelSettings(OrderSettings channelSettings) {
        this.channelSettings = channelSettings;
    }

    public OrderSettings getConversationSettings() {
        return conversationSettings;
    }

    public void setConversationSettings(OrderSettings conversationSettings) {
        this.conversationSettings = conversationSettings;
    }

    public OrderSettings getGroupSettings() {
        return groupSettings;
    }

    public void setGroupSettings(OrderSettings groupSettings) {
        this.groupSettings = groupSettings;
    }

    public OrderSettings getBallotSettings() {
        return ballotSettings;
    }

    public void setBallotSettings(OrderSettings ballotSettings) {
        this.ballotSettings = ballotSettings;
    }

    public OrderSettings getAnnouncementSettings() {
        return announcementSettings;
    }

    public void setAnnouncementSettings(OrderSettings announcementSettings) {
        this.announcementSettings = announcementSettings;
    }

    public OrderSettings getGeneralSettings() {
        return generalSettings;
    }

    public void setGeneralSettings(OrderSettings generalSettings) {
        this.generalSettings = generalSettings;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public NotificationSettings getNotificationSettings() {
        return notificationSettings;
    }

    public void setNotificationSettings(NotificationSettings notificationSettings) {
        this.notificationSettings = notificationSettings;
    }
}
