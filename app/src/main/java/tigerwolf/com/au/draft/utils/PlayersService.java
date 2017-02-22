package tigerwolf.com.au.draft.utils;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tigerwolf.com.au.draft.models.Player;

import com.google.gson.reflect.TypeToken;

/**
 * Created by Henrique on 10/02/2017.
 */

public class PlayersService {

    private static PlayersService instance = null;

    // Server URL
    private String url = "http://challengecup.club:8080/api/v1/drafts/players";

    // OkHttp resources
    private final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // Default strings for broadcast
    // We use this because the request is assynchronous
    public static final String LOADING_PLAYERS_FINISHED = "loading_players_finished";
    public static final String PLAYERS_LIST_CHANGED = "players_list_changed";

    public List<Player> playerList = new ArrayList<Player>();

    public void loadPlayers(final Context context) {
        (new Thread() {
            @Override
            public void run() {
                List<Player> players = new ArrayList<Player>();
                try {
                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    Response response = client.newCall(request).execute();
                    String json = response.body().string();

                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(Player.class, new PlayerDeserializer())
                            .create();

                    Type listType = new TypeToken<List<Player>>() {}.getType();
                    playerList = gson.fromJson(json, listType);
                    Collections.sort(playerList);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    Intent i = new Intent(LOADING_PLAYERS_FINISHED);
                    context.sendBroadcast(i);
                }
            }
        }).start();
    }

    public List<Player> getDraftedPlayers() {
        List<Player> draftedPlayers = new ArrayList<Player>();
        for (Player p : playerList) {
            if (p.isDrafted()) {
                draftedPlayers.add(p);
            }
        }
        return draftedPlayers;
    }

    public void togglePlayerDraftedStatus(Player player) {
        for(Player p : playerList) {
            if (p.equals(player)) {
                p.toggleDrafted();
                return;
            }
        }
    }

    public List<Player> getFilteredPlayers(String name) {
        List<Player> filteredPlayers = new ArrayList<Player>();
        for(Player p : playerList) {
            String searchName = name.toUpperCase();
            String givenName  = p.getGivenName().toUpperCase();
            String surname    = p.getSurname().toUpperCase();
            if (givenName.contains(searchName) || surname.contains(searchName)) {
                filteredPlayers.add(p);
            }
        }
        return filteredPlayers;
    }

    protected PlayersService() {
        // Exists only to defeat instantiation.
    }

    public static PlayersService getInstance() {
        if(instance == null) {
            instance = new PlayersService();
        }
        return instance;
    }
}
