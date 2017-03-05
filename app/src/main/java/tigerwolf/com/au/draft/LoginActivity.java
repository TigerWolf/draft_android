package tigerwolf.com.au.draft;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import tigerwolf.com.au.draft.utils.LoginService;

public class LoginActivity extends AppCompatActivity {

    private EditText mUserInput;
    private EditText mPassInput;
    private Button   mLoginButton;

    // Register a loginFailedReceiver to update the screen when receives a broadcast
    private BroadcastReceiver loginFailedReceiver;
    private BroadcastReceiver loginSucceededReceiver;
    private ProgressDialog progressDialog;

    // Stores LoginActivity reference to be used while creating the loading progressDialog
    private LoginActivity loginActivity = this;

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
                    createLoadingDialog();
                    LoginService.getInstance().login(user, pass, getApplicationContext());
                }
            }
        });
    }

    // Function that handles the login after receiveing the broadcast
    private void manageLoginResponse() {
        String message;

        if (LoginService.getInstance().token.isLoggedIn()) {
            message = "Sucess!";
            openPlayersActivity();
        } else {
            message = "Whoops :( Error code: " + LoginService.getInstance().errorCode;
        }

        displayMessage(message);
    }

    private void manageLoginFailedResponse() {
        String message = "Could not login - check your username and password.";
        displayMessage(message);
    }

    // Creates a small Toast to display some message
    private void displayMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void createLoadingDialog() {
        progressDialog = new ProgressDialog(loginActivity);
        progressDialog.setMessage("Logging in");
        progressDialog.show();
    }

    private void openPlayersActivity() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();

        loginFailedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                progressDialog.dismiss();
                manageLoginFailedResponse();
            }
        };

        registerReceiver(loginFailedReceiver, new IntentFilter(LoginService.LOGIN_PROCESS_FAILED));

        loginSucceededReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                progressDialog.dismiss();
                manageLoginResponse();
            }
        };

        registerReceiver(loginSucceededReceiver, new IntentFilter(LoginService.LOGIN_PROCESS_FINISHED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(loginFailedReceiver);
        unregisterReceiver(loginSucceededReceiver);
    }
}
