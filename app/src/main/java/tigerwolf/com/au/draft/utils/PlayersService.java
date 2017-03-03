package tigerwolf.com.au.draft.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import tigerwolf.com.au.draft.models.Player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Henrique on 10/02/2017.
 */

public class PlayersService {

    public enum RequestType {
        LOAD_PLAYERS, DRAFT_PLAYER
    }

    private static PlayersService instance = null;

    // Server URL
    private String url = "http://challengecup.club:8080/api/v1/drafts";

    public int errorCode = 0; // Last error code
    public List<String> errorField = new ArrayList<String>();

    // OkHttp resources
    private final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // Default strings for broadcast
    // We use this because the request is assynchronous
    public static final String LOADING_PLAYERS_FINISHED = "loading_players_finished";
    public static final String PLAYER_DRAFTED = "players_drafted";
    public static final String TEAM_LIST_CHANGED = "team_list_changed";

    public List<Player> playerList = new ArrayList<Player>();

    /**
     * Loads all players in the server. Every time it loads the player's list, it loads the team list too.
     * @param context
     */
    public void loadPlayers(final Context context) {
        (new Thread() {
            @Override
            public void run() {
                List<Player> players = new ArrayList<Player>();
                try {
                    Request request = new Request.Builder()
                            .url(url + "/players")
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
                    // After requesting player's list, load the drafted ones
                    refreshDraftStatusOfPlayerList(context, RequestType.LOAD_PLAYERS);
                    // Loads the team list
                    loadMyTeam(context);
                }
            }
        }).start();
    }

    /**
     * Refresh the playerList draft status, based on the server
     * @param context
     * @param requestType The type of the request that called this function. It's used to broadcast the correct String.
     */
    public void refreshDraftStatusOfPlayerList(final Context context, final RequestType requestType) {
        (new Thread() {
            @Override
            public void run() {
                try {
                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    Response response = client.newCall(request).execute();
                    String json = response.body().string();

                    JsonParser parser = new JsonParser();
                    JsonObject rootObj = parser.parse(json).getAsJsonObject();

                    clearDraftedStatusCache();

                    // For each element in the data array
                    for(JsonElement e : rootObj.getAsJsonArray("data")) {
                        // Get player id
                        String draftedPlayerId = e.getAsJsonObject().get("player_id").getAsString();
                        // Search in the player's list
                        for (Player p : playerList) {
                            if (p.getId().equals(draftedPlayerId)) {
                                p.setDrafted(true);
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    // Finished
                    broadcast(requestType, context);
                }
            }
        }).start();
    }

    /**
     * Sends a POST request to the server to draft a player.
     * After sending the POST, it syncs the drafted status with the server
     * and syncs the team list
     * @param playerId
     * @param position
     * @param context
     */
    public void postDraftStatus(final String playerId, final String position, final Context context) {
        (new Thread() {
            @Override
            public void run() {
                try {
                    errorCode = 0;
                    String json = createJson(playerId, position);

                    RequestBody body = RequestBody.create(JSON, json);
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .addHeader("Authorization", LoginService.getInstance().token.getValue())
                            .build();
                    Response response = client.newCall(request).execute();
                    JsonParser parser = new JsonParser();

                    // Checks if the request was successful
                    if (!response.isSuccessful()) {
                        errorCode = response.code();

                        if (errorCode == 422) {
                            JsonObject obj = (JsonObject) parser.parse(response.body().string());
                            Set<Map.Entry<String, JsonElement>> entries = obj.get("errors").getAsJsonObject().entrySet();
                            for (Map.Entry<String, JsonElement> entry: entries) {
                                errorField.add(entry.getKey());
                            }
                        }
                    }

                    if (response.code() == 201) {
                        // Do something?
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    refreshDraftStatusOfPlayerList(context, RequestType.DRAFT_PLAYER);
                    loadMyTeam(context);
                }
            }
        }).start();
    }

    public void loadMyTeam(final Context context) {
        (new Thread() {
            @Override
            public void run() {
                List<Player> players = new ArrayList<Player>();
                try {
                    Request request = new Request.Builder()
                            .url(url + "/me")
                            .addHeader("Authorization", LoginService.getInstance().token.getValue())
                            .build();

                    Response response = client.newCall(request).execute();
                    String json = response.body().string();

                    JsonParser parser = new JsonParser();
                    JsonObject rootObj = parser.parse(json).getAsJsonObject();

                    clearTeamStatusCache();

                    // For each element in the data array
                    for(JsonElement e : rootObj.getAsJsonArray("data")) {
                        // Get player id
                        String draftedPlayerId = e.getAsJsonObject().get("player_id").getAsString();
                        // Search in the player's list
                        for (Player p : playerList) {
                            if (p.getId().equals(draftedPlayerId)) {
                                p.setMyTeam(true);
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    Intent i = new Intent(TEAM_LIST_CHANGED);
                    context.sendBroadcast(i);
                }
            }
        }).start();
    }

    public void createPlayersSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://challengecup.club:8080/socket/websocket")
                .build();
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                Log.d("DRAFT", "WebSocket - Message received: " + text);
            }

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                Log.d("DRAFT", "WebSocket open!");
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                Log.d("DRAFT", "WebSocket closed");
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                Log.d("DRAFT", "WebSocket failed!");
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
                Log.d("DRAFT", "WebSocket - ByteString received: " + bytes);
            }
        };
        WebSocket ws = client.newWebSocket(request, listener);
    }

    /**
     * Return a list of all drafted players
     * @return
     */
    public List<Player> getDraftedPlayers() {
        List<Player> draftedPlayers = new ArrayList<Player>();
        for (Player p : playerList) {
            if (p.isDrafted()) {
                draftedPlayers.add(p);
            }
        }
        return draftedPlayers;
    }

    /**
     * Function that drafts a player. It posts a request to the server and then refresh the draft status
     * of all players calling the function "refreshDraftStatusOfPlayerList"
     * @param player playerId and playerPositions
     * @param context
     */
    public void draftPlayer(Player player, Context context) {
        for(Player p : playerList) {
            if (p.equals(player)) {
                postDraftStatus(player.getId(), player.getPositionsAsString(), context);
                return;
            }
        }
    }

    /**
     * Clears the draft status of all players in the playerList
     */
    private void clearDraftedStatusCache() {
        for (Player p : playerList) {
            p.setDrafted(false);
        }
    }

    /**
     * Clears the team status of all players in the playerList
     */
    private void clearTeamStatusCache() {
        for (Player p : playerList) {
            p.setMyTeam(false);
        }
    }

    /**
     * Returns a list of players based on the name
     * @param name filter
     * @return
     */
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

    public List<Player> getMyTeamPlayers() {
        List<Player> myTeam = new ArrayList<Player>();

        for(Player p : playerList) {
            if (p.isMyTeam()) {
                myTeam.add(p);
            }
        }

        return myTeam;
    }

    /**
     * Broacasts a message to stop any loading dialogs in the activity
     * @param afterRequest Which resquest caused that refresh
     * @param context
     */
    private void broadcast(RequestType afterRequest, Context context) {
        Intent i;
        switch (afterRequest) {
            case LOAD_PLAYERS:
                i = new Intent(LOADING_PLAYERS_FINISHED);
                context.sendBroadcast(i);
                break;
            case DRAFT_PLAYER:
                i = new Intent(PLAYER_DRAFTED);
                context.sendBroadcast(i);
                break;
        }
    }

    /**
     * Used to submit data when drafting a player
     * @param playerId
     * @param position
     * @return
     */
    private String createJson(String playerId, String position) {
        return "{\"draft\":{\"player_id\":\"" + playerId + "\",\"position\":\"" + position + "\"}}";
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
