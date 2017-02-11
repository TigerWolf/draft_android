package tigerwolf.com.au.draft;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import tigerwolf.com.au.draft.models.Player;
import tigerwolf.com.au.draft.utils.PlayersAdapter;
import tigerwolf.com.au.draft.utils.PlayersService;

public class TeamFragment extends Fragment {

    private BroadcastReceiver playerListUpdateReceiver;

    private View     view;
    private ListView mListViewTeam;

    private PlayersAdapter playersAdapter;
    private List<Player>   players;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.fragment_team, container, false);

        // Links the ListView on the xml to the variable
        this.linkListViewTeam();

        return view;
    }

    private void linkListViewTeam() {
        this.players = PlayersService.getInstance().getDraftedPlayers();

        this.playersAdapter = new PlayersAdapter(getContext(), players);
        this.mListViewTeam = (ListView) view.findViewById(R.id.fragment_team_list_view);
        this.mListViewTeam.setAdapter(this.playersAdapter);
        this.mListViewTeam.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                PlayersService.getInstance().togglePlayerDraftedStatus(players.get(pos));

                Intent i = new Intent(PlayersService.PLAYERS_LIST_CHANGED);
                getContext().sendBroadcast(i);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        playerListUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                players = PlayersService.getInstance().getDraftedPlayers();
                playersAdapter.updatePlayers(players);
            }
        };

        getActivity().registerReceiver(playerListUpdateReceiver, new IntentFilter(PlayersService.PLAYERS_LIST_CHANGED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(playerListUpdateReceiver);
    }
}
