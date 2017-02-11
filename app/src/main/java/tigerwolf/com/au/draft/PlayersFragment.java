package tigerwolf.com.au.draft;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import tigerwolf.com.au.draft.models.Player;
import tigerwolf.com.au.draft.utils.PlayersAdapter;
import tigerwolf.com.au.draft.utils.PlayersService;

public class PlayersFragment extends Fragment {

    private BroadcastReceiver playerListLoadedReceiver;
    private BroadcastReceiver playerListUpdatedReceiver;

    private PlayersAdapter playersAdapter;
    private ListView       mListViewPlayers;
    private ProgressDialog progressDialog;
    private AlertDialog.Builder confirmDialogBuilder;

    private List<Player> players;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        view = inflater.inflate(R.layout.fragment_players, container, false);

        // Links the ListView on the xml to the variable
        this.linkListViewPlayers();

        // Loads data
        this.reloadPlayersList();

        return view;
    }

    private void reloadPlayersList() {
        PlayersService.getInstance().loadPlayers(getActivity());
        createLoadingDialog();
    }

    private void createLoadingDialog() {
        this.progressDialog = new ProgressDialog(getActivity());
        this.progressDialog.setMessage("Loading players");
        this.progressDialog.show();
    }

    private void createConfirmationDialog(String message, final int position) {
        this.confirmDialogBuilder = new AlertDialog.Builder(getContext());
        this.confirmDialogBuilder.setTitle("Draft");
        this.confirmDialogBuilder.setMessage(message);

        this.confirmDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                draftPlayer(position);
            }
        });

        this.confirmDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    // Function that links the ListView on the XML
    // This is also used to refresh the page when received a broadcast
    private void linkListViewPlayers() {
        this.players = PlayersService.getInstance().playerList;

        this.playersAdapter = new PlayersAdapter(getContext(), this.players);

        this.mListViewPlayers = (ListView) view.findViewById(R.id.fragment_players_list_view);
        this.mListViewPlayers.setAdapter(this.playersAdapter);
        this.mListViewPlayers.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                String message = "Do you want to draft this player?";
                if (PlayersService.getInstance().playerList.get(pos).isDrafted()) {
                    message = "Do you want to remove this player from your team?";
                }

                createConfirmationDialog(message, pos);
                AlertDialog alertDialog = confirmDialogBuilder.create();
                alertDialog.show();
            }
        });
    }

    private void draftPlayer(int pos) {
        PlayersService.getInstance().togglePlayerDraftedStatus(this.players.get(pos));

        Intent i = new Intent(PlayersService.PLAYERS_LIST_CHANGED);
        getContext().sendBroadcast(i);
    }

    @Override
    public void onResume() {
        super.onResume();

        playerListLoadedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                progressDialog.dismiss();
                linkListViewPlayers();
            }
        };

        playerListUpdatedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                playersAdapter.notifyDataSetChanged();
            }
        };

        getActivity().registerReceiver(playerListLoadedReceiver, new IntentFilter(PlayersService.LOADING_PLAYERS_FINISHED));
        getActivity().registerReceiver(playerListUpdatedReceiver, new IntentFilter(PlayersService.PLAYERS_LIST_CHANGED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(playerListLoadedReceiver);
    }
}
