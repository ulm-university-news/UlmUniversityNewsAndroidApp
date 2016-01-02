package ulm.university.news.app.data;

import org.joda.time.DateTime;

import java.util.List;

import ulm.university.news.app.data.enums.GroupType;

/**
 * The Group class represents a group. It contains the relevant data for the group and provides methods to access this
 * data. A group is an enclosed area protected by a password. Only the participants of the group have access to the
 * contents and resources within this enclosed area.
 *
 * @author Matthias Mak
 * @author Philipp Speidel
 */
public class Group {
    /** The unique id of the group. */
    private int id;
    /** The name of the group. */
    private String name;
    /** The description of the group. */
    private String description;
    /** The type of the group. Defines whether the group is a tutorial group or a working group. */
    private GroupType groupType;
    /** The date and time when the group was created. */
    private DateTime creationDate;
    /** The date and time when the group was updated the last time. */
    private DateTime modificationDate;
    /** The specified term. */
    private String term;
    /** The password which protects the group form unwished participants. Every user needs to provide the
     * password to be able to join the group.*  */
    private String password;
    /** The id of the user which assumes the role of the group administrator for the group. */
    private int groupAdmin;
    /** A list of conversations which belong to the group. */
    private List<Conversation> conversations;
    /** A list of participants of the group. */
    private List<User> participants;
    /** A list of ballots which belong to the group.  */
    private List<Ballot> ballots;

    /**
     * Creates an instance of the Group class.
     */
    public Group(){

    }

    /**
     * Creates an instance of the Group class.
     *
     * @param name The name of the group.
     * @param description The description of the group.
     * @param groupType The type of the group.
     * @param creationDate The date and time when the group was created.
     * @param modificationDate The date and time when the group was modified the last time.
     * @param term The specified term.
     * @param password The password which protects the group from unwished participants.
     * @param groupAdmin The id of the user which assumes the role of the group administrator.
     */
    public Group(String name, String description, GroupType groupType, DateTime creationDate, DateTime
            modificationDate, String term, String password, int groupAdmin){
        this.name = name;
        this.description = description;
        this.groupType = groupType;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
        this.term = term;
        this.password = password;
        this.groupAdmin = groupAdmin;
    }

    /**
     * Creates an instance of the Group class.
     *
     * @param id The unique id of the group.
     * @param name The name of the group.
     * @param description The description of the group.
     * @param groupType The type of the group.
     * @param creationDate The date and time when the group was created.
     * @param modificationDate The date and time when the group was modified the last time.
     * @param term The specified term.
     * @param password The password which protects the group from unwished participants.
     * @param groupAdmin The id of the user which assumes the role of the group administrator.
     */
    public Group(int id, String name, String description, GroupType groupType, DateTime creationDate, DateTime
            modificationDate, String term, String password, int groupAdmin){
        this.id = id;
        this.name = name;
        this.description = description;
        this.groupType = groupType;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
        this.term = term;
        this.password = password;
        this.groupAdmin = groupAdmin;
    }

    /**
     * Checks if an user with the specified id is the group administrator of this group.
     *
     * @param userId The id of the user.
     * @return Returns true if the user is the group administrator, false otherwise.
     */
    public boolean isGroupAdmin(int userId){
        return userId == groupAdmin;
    }

    public int getGroupAdmin() {
        return groupAdmin;
    }

    public void setGroupAdmin(int groupAdmin) {
        this.groupAdmin = groupAdmin;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GroupType getGroupType() {
        return groupType;
    }

    public void setGroupType(GroupType groupType) {
        this.groupType = groupType;
    }

    public DateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(DateTime creationDate) {
        this.creationDate = creationDate;
    }

    public DateTime getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(DateTime modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Ballot> getBallots() {
        return ballots;
    }

    public void setBallots(List<Ballot> ballots) {
        this.ballots = ballots;
    }

    public List<Conversation> getConversations() {
        return conversations;
    }

    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", groupType=" + groupType +
                ", creationDate=" + creationDate +
                ", modificationDate=" + modificationDate +
                ", term='" + term + '\'' +
                ", groupAdmin=" + groupAdmin +
                ", conversations=" + conversations +
                ", participants=" + participants +
                ", ballots=" + ballots +
                '}';
    }
}
