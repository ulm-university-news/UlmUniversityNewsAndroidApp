package ulm.university.news.app.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import ulm.university.news.app.data.Ballot;
import ulm.university.news.app.data.Conversation;
import ulm.university.news.app.data.ConversationMessage;
import ulm.university.news.app.data.Group;
import ulm.university.news.app.data.Option;
import ulm.university.news.app.data.User;
import ulm.university.news.app.util.Util;

/**
 * The GroupAPI is responsible for sending requests regarding the group resource. Required data is handed over from
 * calling controller (e.g. Activity). After the request was executed, the API parses the response data. On  success
 * a corresponding data object, on failure a ServerError will be created. Then the API uses the EventBus to  send the
 * response object and to notify the controller which called the API method. The controller accesses the object from
 * the EventBus which was delivered by the API. The controller will handle the response according to the type of the
 * response object.
 *
 * @author Matthias Mak
 */
public class GroupAPI extends MainAPI {
    /** This classes tag for logging. */
    private static final String TAG = "GroupAPI";
    /** The reference for the GroupAPI Singleton class. */
    private static GroupAPI _instance;
    /** The REST servers internet address pointing to the group resource. */
    private String serverAddressGroup;

    // Constants.
    public static final String JOIN_GROUP = "joinGroup";
    public static final String LEAVE_GROUP = "removeUserFromGroup";
    public static final String CHANGE_GROUP_ADMIN = "changeGroupAdmin";
    public static final String REMOVE_USER_FROM_GROUP = "removeUserFromGroup";
    public static final String DELETE_GROUP = "deleteGroup";
    public static final String DELETE_CONVERSATION = "deleteConversation";
    public static final String DELETE_BALLOT = "deleteBallot";
    public static final String VOTE_CREATED = "voteCreated";
    public static final String VOTE_DELETED = "voteDeleted";

    /**
     * Get the instance of the GroupAPI class.
     *
     * @return Instance of GroupAPI.
     */
    public static synchronized GroupAPI getInstance(Context context) {
        if (_instance == null) {
            _instance = new GroupAPI(context);
        }
        return _instance;
    }

    /**
     * Creates an instance of GroupAPI and initialises values.
     *
     * @param context The context within the GroupAPI is used.
     */
    private GroupAPI(Context context) {
        super(context);
        serverAddressGroup = serverAddress + "/group";
    }

    public void getGroup(int groupId) {
        // Add id to url.
        String url = serverAddressGroup + "/" + groupId;

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                // Use a list of groups as deserialization type.
                Group group = gson.fromJson(json, Group.class);
                EventBus.getDefault().post(group);
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_GET, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    public void getGroups(String groupName, String groupType) {
        HashMap<String, String> params = new HashMap<>();
        if (groupName != null) {
            params.put("groupName", groupName);
        }
        if (groupType != null) {
            params.put("groupType", groupType);
        }
        // Add parameters to url.
        String url = serverAddressGroup + getUrlParams(params);

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                // Use a list of groups as deserialization type.
                Type listType = new TypeToken<List<Group>>() {
                }.getType();
                List<Group> groups = gson.fromJson(json, listType);
                EventBus.getDefault().post(groups);
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_GET, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * Creates a new group on the server. The data of the new group is provided within the group object. The
     * generated group resource will be converted to a group object and will be passed to the controller.
     *
     * @param group The group object including the data of the new group.
     */
    public void createGroup(Group group) {
        // Parse group object to JSON String.
        String jsonGroup = gson.toJson(group, Group.class);

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                Group groupResponse = gson.fromJson(json, Group.class);
                EventBus.getDefault().post(groupResponse);
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_POST, serverAddressGroup);
        rTask.setBody(jsonGroup);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * Changes an existing group on the server. The changed data is provided within the group object.
     *
     * @param group The group object including the new group data.
     */
    public void changeGroup(Group group) {
        // Add group id to url.
        String url = serverAddressGroup + "/" + group.getId();
        // Parse group object to JSON String.
        String jsonGroup = gson.toJson(group, Group.class);

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                Group groupResponse = gson.fromJson(json, Group.class);
                EventBus.getDefault().post(groupResponse);
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_PATCH, url);
        rTask.setBody(jsonGroup);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * Changes the admin of an existing group on the server. The changed data is provided within the group object.
     *
     * @param group The group object including the new admin id.
     */
    public void changeGroupAdmin(Group group) {
        // Add group id to url.
        String url = serverAddressGroup + "/" + group.getId();
        // Parse group object to JSON String.
        String jsonGroup = gson.toJson(group, Group.class);

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                Group groupResponse = gson.fromJson(json, Group.class);
                EventBus.getDefault().post(new BusEvent(CHANGE_GROUP_ADMIN, groupResponse));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_PATCH, url);
        rTask.setBody(jsonGroup);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * Deletes the group with given id from the server.
     *
     * @param groupId The id of the group which should be deleted.
     */
    public void deleteGroup(int groupId) {
        // Add group id to url.
        String url = serverAddressGroup + "/" + groupId;
        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                EventBus.getDefault().post(new BusEvent(DELETE_GROUP, null));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_DELETE, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * Adds the local user to the group with given id.
     *
     * @param groupId The group id.
     * @param password The group password which is required to enter the group.
     */
    public void joinGroup(int groupId, String password) {
        // Add group id to url.
        String url = serverAddressGroup + "/" + groupId + "/user";
        String body = "{\"password\":\"" + password + "\"}";

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                EventBus.getDefault().post(new BusEvent(JOIN_GROUP, null));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_POST, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        rTask.setBody(body);
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * Deletes the local user as a group member from the group with given id.
     *
     * @param groupId The group id.
     */
    public void leaveGroup(int groupId) {
        // Add channel id to url.
        String url = serverAddressGroup + "/" + groupId + "/user/" + Util.getInstance(context).getLocalUser().getId();

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                EventBus.getDefault().post(new BusEvent(LEAVE_GROUP, null));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_DELETE, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * Deletes the user identified by id as a group member from the group with given id.
     *
     * @param groupId The group id.
     * @param userId The user who should be removed from the group.
     */
    public void removeUserFromGroup(int groupId, int userId) {
        // Add channel id to url.
        String url = serverAddressGroup + "/" + groupId + "/user/" + userId;

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                EventBus.getDefault().post(new BusEvent(REMOVE_USER_FROM_GROUP, null));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_DELETE, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    public void getGroupMembers(int groupId) {
        // Add group id to url.
        String url = serverAddressGroup + "/" + groupId + "/user";

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                // Use a list of users as deserialization type.
                Type listType = new TypeToken<List<User>>() {
                }.getType();
                List<User> users = gson.fromJson(json, listType);
                EventBus.getDefault().post(new BusEventGroupMembers(users));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_GET, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * Creates a new conversation on the server.
     *
     * @param conversation The conversation object including the data of the new conversation.
     */
    public void createConversation(int groupId, Conversation conversation) {
        // Add group id to url.
        String url = serverAddressGroup + "/" + groupId + "/conversation";
        // Parse conversation object to JSON String.
        String jsonConversation = gson.toJson(conversation, Conversation.class);

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                Conversation conversationResponse = gson.fromJson(json, Conversation.class);
                EventBus.getDefault().post(conversationResponse);
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_POST, url);
        rTask.setBody(jsonConversation);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * Changes a conversation on the server. This method can be used to change the title of the conversation or to
     * close or open the conversation.
     *
     * @param conversation The conversation object including the data of the updated conversation.
     * @param groupId The id of the group.
     */
    public void changeConversation(int groupId, Conversation conversation) {
        // Add group id to url.
        String url = serverAddressGroup + "/" + groupId + "/conversation/" + conversation.getId();
        // Parse conversation object to JSON String.
        String jsonConversation = gson.toJson(conversation, Conversation.class);

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                Conversation conversationResponse = gson.fromJson(json, Conversation.class);
                EventBus.getDefault().post(new BusEventConversationChange(conversationResponse));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_PATCH, url);
        rTask.setBody(jsonConversation);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * Deletes a conversation on the server.
     *
     * @param conversationId The id of the conversation that should be deleted.
     * @param groupId The id of the group.
     */
    public void deleteConversation(int groupId, int conversationId) {
        // Add group id to url.
        String url = serverAddressGroup + "/" + groupId + "/conversation/" + conversationId;

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                EventBus.getDefault().post(new BusEvent(DELETE_CONVERSATION, null));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_DELETE, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    public void getConversations(int groupId) {
        // Add group id to url.
        String url = serverAddressGroup + "/" + groupId + "/conversation";

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                // Use a list of conversations as deserialization type.
                Type listType = new TypeToken<List<Conversation>>() {
                }.getType();
                List<Conversation> conversations = gson.fromJson(json, listType);
                EventBus.getDefault().post(new BusEventConversations(conversations));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_GET, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    public void getConversation(int groupId, int conversationId) {
        // Add group id to url.
        String url = serverAddressGroup + "/" + groupId + "/conversation/" + conversationId;

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                Conversation conversation = gson.fromJson(json, Conversation.class);
                EventBus.getDefault().post(conversation);
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_GET, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * Creates a new conversation message on the server.
     *
     * @param conversationMessage The conversation message object including the data of the new conversation message.
     */
    public void createConversationMessage(int groupId, ConversationMessage conversationMessage) {
        // Add group and conversation id to url.
        String url = serverAddressGroup + "/" + groupId + "/conversation/"
                + conversationMessage.getConversationId() + "/message";
        // Parse conversation message object to JSON String.
        String jsonConversationMessage = gson.toJson(conversationMessage, ConversationMessage.class);

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                ConversationMessage conversationMessageResponse = gson.fromJson(json, ConversationMessage.class);
                EventBus.getDefault().post(conversationMessageResponse);
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_POST, url);
        rTask.setBody(jsonConversationMessage);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    public void getConversationMessages(int groupId, int conversationId, Integer messageNumber) {
        // Add group and conversation id to url.
        String url = serverAddressGroup + "/" + groupId + "/conversation/" + conversationId + "/message";
        HashMap<String, String> params = new HashMap<>();
        if (messageNumber != null) {
            params.put("messageNr", messageNumber.toString());
        }
        // Add parameters to url.
        url += getUrlParams(params);

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                // Use a list of conversation messages as deserialization type.
                Type listType = new TypeToken<List<ConversationMessage>>() {
                }.getType();
                List<ConversationMessage> conversationMessages = gson.fromJson(json, listType);
                EventBus.getDefault().post(new BusEventConversationMessages(conversationMessages));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_GET, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    public void createBallot(int groupId, Ballot ballot) {
        // Add group id to url.
        String url = serverAddressGroup + "/" + groupId + "/ballot";
        // Parse ballot object to JSON String.
        String jsonBallot = gson.toJson(ballot, Ballot.class);

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                Ballot ballotResponse = gson.fromJson(json, Ballot.class);
                EventBus.getDefault().post(ballotResponse);
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_POST, url);
        rTask.setBody(jsonBallot);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    public void changeBallot(int groupId, Ballot ballot) {
        // Add group and ballot id to url.
        String url = serverAddressGroup + "/" + groupId + "/ballot/" + ballot.getId();
        // Parse ballot object to JSON String.
        String jsonBallot = gson.toJson(ballot, Ballot.class);

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                Ballot ballotResponse = gson.fromJson(json, Ballot.class);
                EventBus.getDefault().post(new BusEventBallotChange(ballotResponse));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_PATCH, url);
        rTask.setBody(jsonBallot);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    public void getBallots(int groupId) {
        // Add group id to url.
        String url = serverAddressGroup + "/" + groupId + "/ballot";

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                // Use a list of ballots as deserialization type.
                Type listType = new TypeToken<List<Ballot>>() {
                }.getType();
                List<Ballot> ballots = gson.fromJson(json, listType);
                EventBus.getDefault().post(new BusEventBallots(ballots));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_GET, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    public void getBallot(int groupId, int ballotId) {
        // Add group and ballot id to url.
        String url = serverAddressGroup + "/" + groupId + "/ballot/" + ballotId;

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                // Use a list of ballots as deserialization type.
                Ballot ballot = gson.fromJson(json, Ballot.class);
                EventBus.getDefault().post(ballot);
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_GET, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    public void deleteBallot(int groupId, int ballotId) {
        // Add group and ballot id to url.
        String url = serverAddressGroup + "/" + groupId + "/ballot/" + ballotId;

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {

                EventBus.getDefault().post(new BusEvent(DELETE_BALLOT, null));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_DELETE, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    public void getOptions(int groupId, int ballotId, Boolean withVotes) {
        // Add group and ballot id to url.
        String url = serverAddressGroup + "/" + groupId + "/ballot/" + ballotId + "/option";
        HashMap<String, String> params = new HashMap<>();
        if (withVotes != null) {
            params.put("subresources", withVotes.toString());
        }
        // Add parameters to url.
        url += getUrlParams(params);

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                // Use a list of options as deserialization type.
                Type listType = new TypeToken<List<Option>>() {
                }.getType();
                List<Option> options = gson.fromJson(json, listType);
                EventBus.getDefault().post(new BusEventOptions(options));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_GET, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * Creates a new ballot option on the server.
     *
     * @param option The option object including the data of the new option.
     */
    public void createOption(int groupId, int ballotId, Option option) {
        // Add group and ballot id to url.
        String url = serverAddressGroup + "/" + groupId + "/ballot/" + ballotId + "/option";
        // Parse option object to JSON String.
        String jsonOption = gson.toJson(option, Option.class);

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                Option optionResponse = gson.fromJson(json, Option.class);
                EventBus.getDefault().post(optionResponse);
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_POST, url);
        rTask.setBody(jsonOption);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * Creates a votes for an option on the server.
     *
     * @param groupId The id of the group.
     * @param ballotId The id of the ballot.
     * @param optionId The id of the option.
     */
    public void createVote(int groupId, int ballotId, final int optionId) {
        // Add group, ballot and option id to url.
        String url = serverAddressGroup + "/" + groupId + "/ballot/" + ballotId + "/option/" + optionId + "/user";

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                EventBus.getDefault().post(new BusEvent(VOTE_CREATED, optionId));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_POST, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }

    /**
     * Deletes a vote of the local user from a option on the server.
     *
     * @param groupId The id of the group.
     * @param ballotId The id of the ballot.
     * @param optionId The id of the option.
     */
    public void deleteVote(int groupId, int ballotId, final int optionId) {
        int userId = Util.getInstance(context).getLocalUser().getId();
        // Add group, ballot, option and user id to url.
        String url = serverAddressGroup + "/" + groupId + "/ballot/" + ballotId + "/option/" + optionId + "/user/" +
                userId;

        RequestCallback rCallback = new RequestCallback() {
            @Override
            public void onResponse(String json) {
                EventBus.getDefault().post(new BusEvent(VOTE_DELETED, optionId));
            }
        };
        RequestTask rTask = new RequestTask(rCallback, this, METHOD_DELETE, url);
        rTask.setAccessToken(Util.getInstance(context).getAccessToken());
        Log.d(TAG, rTask.toString());
        new Thread(rTask).start();
    }
}
