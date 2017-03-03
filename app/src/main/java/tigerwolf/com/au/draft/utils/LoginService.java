package tigerwolf.com.au.draft.utils;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Henrique on 10/02/2017.
 */

public class LoginService {

    // Singleton instance
    private static LoginService instance = null;

    // Server URL
    private String url = "http://challengecup.club:8080/api/v1/auth/session";
//    private String url = "http://10.0.2.2:4000/api/v1/auth/session";

    // OkHttp resources
    private final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // Default strings for broadcast
    // We use this because the request is assynchronous
    public static final String LOGIN_PROCESS_FINISHED = "login_finished";
    public static final String LOGIN_PROCESS_FAILED = "login_failed";

    public Token token = new Token(); // Stores the token
    public int errorCode = 0; // Last error code

    /**
     * Receives user, password and context and broadcasts LOGIN_PROCESS_FINISHED when finishes
     * @param user user's login
     * @param pass user's password
     * @param context activity context
     */
    public void login(final String user, final String pass, final Context context) {
        (new Thread() {
            @Override
            public void run() {
                boolean success = false;
                try {
                    token.clearToken();
                    errorCode = 0;
                    String json = createJson(user, pass);

                    RequestBody body = RequestBody.create(JSON, json);
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();

                    // Checks if the request was successful
                    if (!response.isSuccessful()) {
                        errorCode = response.code();
                        throw new IOException("Unexpected code " + response);
                    }

                    // JSON response
                    JsonParser parser = new JsonParser();
                    JsonObject jo = (JsonObject) parser.parse(response.body().string());

                    token.setToken(jo.get("token").getAsString());
                    token.setLoggedIn(true);
                    success = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if (!success){
                        Intent i = new Intent(LOGIN_PROCESS_FAILED);
                        context.sendBroadcast(i);
                    }else {
                        Intent i = new Intent(LOGIN_PROCESS_FINISHED);
                        context.sendBroadcast(i);
                    }
                }
            }
        }).start();
    }

    /**
     * Returns user and password in JSON format
     * @param user user's login
     * @param pass user's password
     * @return JSON string
     */
    private String createJson(String user, String pass) {
        return "{ \"user\": {\"email\":\"" + user + "\",\"password\":\"" + pass + "\"}}";
    }

    protected LoginService() {
        // Exists only to defeat instantiation.
    }

    public static LoginService getInstance() {
        if(instance == null) {
            instance = new LoginService();
        }
        return instance;
    }
}
