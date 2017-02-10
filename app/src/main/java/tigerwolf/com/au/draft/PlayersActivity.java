package tigerwolf.com.au.draft;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import tigerwolf.com.au.draft.models.Player;
import tigerwolf.com.au.draft.utils.PlayersAdapter;
import tigerwolf.com.au.draft.utils.PlayersService;

public class PlayersActivity extends AppCompatActivity {

    private BroadcastReceiver receiver;

    private ListView mListViewPlayers;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_players);

        // Links the ListView on the xml to the variable
        this.linkListViewPlayers();

        // Loads data
        this.reloadPlayersList();
    }

    private void reloadPlayersList() {
        PlayersService.getInstance().loadPlayers(getApplicationContext());
        createLoadingDialog();
    }

    private void createLoadingDialog() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading players");
        dialog.show();
    }

    // Function that links the ListView on the XML
    // This is also used to refresh the page when received a broadcast
    private void linkListViewPlayers() {
        List<Player> players = PlayersService.getInstance().playerList;

        this.mListViewPlayers = (ListView) findViewById(R.id.activity_players_list_view);
        this.mListViewPlayers.setAdapter(new PlayersAdapter(getApplicationContext(), players));
        this.mListViewPlayers.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                dialog.dismiss();
                linkListViewPlayers();
            }
        };

        registerReceiver(receiver, new IntentFilter(PlayersService.LOADING_PLAYERS_FINISHED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
