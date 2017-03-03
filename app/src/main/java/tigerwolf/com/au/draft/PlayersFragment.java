package tigerwolf.com.au.draft;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import tigerwolf.com.au.draft.models.Player;
import tigerwolf.com.au.draft.utils.LoginService;
import tigerwolf.com.au.draft.utils.PlayersAdapter;
import tigerwolf.com.au.draft.utils.PlayersService;

public class PlayersFragment extends Fragment {

    private BroadcastReceiver playerListLoadedReceiver;
    private BroadcastReceiver playerDrafted;

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

    private void createDraftConfirmationDialog(String message, final int position) {
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

    private void createLogoutConfirmationDialog(String message) {
        this.confirmDialogBuilder = new AlertDialog.Builder(getContext());
        this.confirmDialogBuilder.setTitle("Logout");
        this.confirmDialogBuilder.setMessage(message);

        this.confirmDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout();
            }
        });

        this.confirmDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = this.confirmDialogBuilder.create();
        alertDialog.show();
    }

    private void displayInformationDialog(String message) {
        this.confirmDialogBuilder = new AlertDialog.Builder(getContext());
        this.confirmDialogBuilder.setTitle("Draft");
        this.confirmDialogBuilder.setMessage(message);

        this.confirmDialogBuilder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        this.confirmDialogBuilder.show();
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
                    return;
                }

                createDraftConfirmationDialog(message, pos);
                AlertDialog alertDialog = confirmDialogBuilder.create();
                alertDialog.show();
            }
        });
    }

    private void draftPlayer(int pos) {
        createLoadingDialog();
        PlayersService.getInstance().draftPlayer(this.players.get(pos), getContext());
    }

    private void handleSearch(String value) {
        // Clears
        if (value == null) {
            List<Player> players = PlayersService.getInstance().playerList;
            playersAdapter.updatePlayers(players);
        } else {
            List<Player> filteredPlayers = PlayersService.getInstance().getFilteredPlayers(value);
            playersAdapter.updatePlayers(filteredPlayers);
        }
    }

    private void logout() {
        LoginService.getInstance().logout();

        Intent i = new Intent(getActivity(), LoginActivity.class);
        startActivity(i);

        getActivity().finish();
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

        playerDrafted = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                playersAdapter.notifyDataSetChanged();
                progressDialog.dismiss();

                if (PlayersService.getInstance().errorCode == 422) {
                    displayInformationDialog("This player has already been drafted.");
                }
            }
        };

        getActivity().registerReceiver(playerListLoadedReceiver, new IntentFilter(PlayersService.LOADING_PLAYERS_FINISHED));
        getActivity().registerReceiver(playerDrafted, new IntentFilter(PlayersService.PLAYER_DRAFTED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(playerListLoadedReceiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.players_menu, menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(true);

        // Text changed listers
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String query) {
                handleSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                handleSearch(query);
                return false;
            }
        });

        // OnClose listener: http://stackoverflow.com/questions/13920960/searchview-oncloselistener-does-not-get-invoked/14622049#14622049
        searchView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewDetachedFromWindow(View arg0) {
                handleSearch(null);
            }

            @Override
            public void onViewAttachedToWindow(View arg0) {}
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                createLogoutConfirmationDialog("Are you sure you want to logout?");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
