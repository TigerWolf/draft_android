package tigerwolf.com.au.draft.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import tigerwolf.com.au.draft.R;
import tigerwolf.com.au.draft.models.Player;

/**
 * Created by Henrique on 10/02/2017.
 */

public class PlayersAdapter extends BaseAdapter {

    private static LayoutInflater mInflater = null;
    private static List<Player> mPlayers;

    public PlayersAdapter(Context context, List<Player> players) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mPlayers = players;
    }

    @Override
    public int getCount() {
        return this.mPlayers.size();
    }

    @Override
    public Player getItem(int position) {
        return this.mPlayers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public class Holder {
        TextView textViewPlayerName;
        TextView textViewTeamAbbr;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView;
        rowView = mInflater.inflate(R.layout.list_players_row, null);
        holder.textViewPlayerName = (TextView) rowView.findViewById(R.id.list_players_row_name);
        holder.textViewTeamAbbr = (TextView) rowView.findViewById(R.id.list_players_row_team);

        holder.textViewPlayerName.setText(mPlayers.get(position).getGivenName() + " " + mPlayers.get(position).getSurname());
        holder.textViewTeamAbbr.setText(mPlayers.get(position).getTeam().getAbbreviation());

        return rowView;
    }
}
