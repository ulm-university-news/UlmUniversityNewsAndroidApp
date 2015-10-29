package ulm.university.news.app.api;

import android.util.Log;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import ulm.university.news.app.data.Channel;
import ulm.university.news.app.data.Event;
import ulm.university.news.app.data.Lecture;
import ulm.university.news.app.data.Sports;
import ulm.university.news.app.data.enums.ChannelType;

/**
 * The ChannelDeserializer is used to deserialize channel class or appropriate channel subclass. The correct
 * deserialization is based on the channel type included in the JSON String.
 *
 * @author Matthias Mak
 */
public class ChannelDeserializer implements JsonDeserializer<Channel> {
    /** This classes tag for logging. */
    private static final String LOG_TAG = "ChannelDeserializer";

    /** The Gson object used to parse from JSON with default deserializer. */
    private Gson gson;

    /**
     * Creates an instance of ChannelDeserializer and initialises Gson.
     */
    public ChannelDeserializer() {
        // Make sure, dates are deserialized properly.
        gson = Converters.registerDateTime(new GsonBuilder()).create();
    }

    @Override
    public Channel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws
            JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        // Deserialize as Channel class or appropriate Channel subclass.
        if (jsonObject.has("type")) {
            ChannelType type = ChannelType.valueOf(jsonObject.get("type").getAsString());
            Log.d(LOG_TAG, "channelType: " + type);
            switch (type) {
                case LECTURE:
                    return context.deserialize(jsonObject, Lecture.class);
                case EVENT:
                    return context.deserialize(jsonObject, Event.class);
                case SPORTS:
                    return context.deserialize(jsonObject, Sports.class);
                default:
                    // Deserialize OTHER and STUDENT_GROUP as normal Channel object.
                    return gson.fromJson(json, Channel.class);
            }
        }
        throw new RuntimeException("Deserialization failed. No channel type found.");
    }
}
