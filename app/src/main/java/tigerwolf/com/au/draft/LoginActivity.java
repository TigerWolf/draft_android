package tigerwolf.com.au.draft;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tigerwolf.com.au.draft.utils.LoginService;

public class LoginActivity extends AppCompatActivity {

    private EditText mUserInput;
    private EditText mPassInput;
    private Button   mLoginButton;

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Links the variables to the xml file
        this.loadViewsById();

        // Buttons' actions
        this.createOnClickFunctions();
    }

    private void loadViewsById() {
        this.mUserInput = (EditText) findViewById(R.id.activity_login_input_user);
        this.mPassInput = (EditText) findViewById(R.id.activity_login_input_pass);
        this.mLoginButton = (Button) findViewById(R.id.activity_login_button_login);
    }

    private void createOnClickFunctions() {
        this.mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = mUserInput.getText().toString();
                String pass = mPassInput.getText().toString();

                if (user == null || user.isEmpty()) {
                    displayMessage("Empty username");
                } else if (pass == null || pass.isEmpty()) {
                    displayMessage("Empty password");
                } else {
                    LoginService.getInstance().login(user, pass, getApplicationContext());
                }
            }
        });
    }

    private void displayMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void manageLoginResponse() {
        String message;

        if (LoginService.getInstance().token.isLoggedIn()) {
            message = "Sucess!";
        } else {
            message = "Whoops :( Error code: " + LoginService.getInstance().errorCode;
        }

        displayMessage(message);
    }

    @Override
    public void onResume() {
        super.onResume();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                manageLoginResponse();
            }
        };

        registerReceiver(receiver, new IntentFilter(LoginService.LOGIN_PROCESS_FINISHED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
