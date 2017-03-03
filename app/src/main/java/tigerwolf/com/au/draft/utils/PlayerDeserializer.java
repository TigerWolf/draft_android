package tigerwolf.com.au.draft.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Map;

import tigerwolf.com.au.draft.models.Player;
import tigerwolf.com.au.draft.models.Team;

/**
 * Created by Henrique on 10/02/2017.
 */

public class PlayerDeserializer implements JsonDeserializer<Player> {

    @Override
    public Player deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        Gson gson = new GsonBuilder().create();

        Player player;
        Team   team = gson.fromJson(obj, Team.class);

        if (obj.get("positions").isJsonArray()) {
            player = gson.fromJson(obj, Player.class);
        } else {
            // There are some players with empty positions, Gson recognizes as empty string. Ex: ""
            obj.remove("positions");
            player = gson.fromJson(obj, Player.class);
        }

        player.setTeam(team);

        return player;
    }
}
