package tigerwolf.com.au.draft.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconTextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import tigerwolf.com.au.draft.R;
import tigerwolf.com.au.draft.models.Player;

/**
 * Created by Henrique on 10/02/2017.
 */

public class PlayersAdapter extends BaseAdapter {

    private static LayoutInflater mInflater = null;
    private List<Player> mPlayers;
    private Context context;

    public PlayersAdapter(Context context, List<Player> players) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mPlayers = players;
        this.context = context;
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
        ImageView imageViewPlayer;
        IconTextView iconTextViewLoading;
        TextView textViewPlayerName;
        TextView textViewTeamAbbr;
        IconTextView iconTextViewCheck;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder = new Holder();
        View rowView;
        rowView = mInflater.inflate(R.layout.list_players_row, null);
        Player player = mPlayers.get(position);

        // findViewById
        holder.iconTextViewLoading = (IconTextView) rowView.findViewById(R.id.list_players_row_loading);
        holder.textViewPlayerName  = (TextView) rowView.findViewById(R.id.list_players_row_name);
        holder.textViewTeamAbbr    = (TextView) rowView.findViewById(R.id.list_players_row_team);
        holder.imageViewPlayer     = (ImageView) rowView.findViewById(R.id.list_players_row_picture);
        holder.iconTextViewCheck   = (IconTextView) rowView.findViewById(R.id.list_players_row_check);

        // Text information
        holder.textViewPlayerName.setText(player.getGivenName() + " " + player.getSurname());
        holder.textViewTeamAbbr.setText(player.getTeam().getAbbreviation());

        if (player.isDrafted()) {
            holder.iconTextViewCheck.setVisibility(View.VISIBLE);
        } else {
            holder.iconTextViewCheck.setVisibility(View.GONE);
        }

        // Assync image loading
        Picasso.with(context)
                .load(mPlayers.get(position).getPhotoURL())
                .into(holder.imageViewPlayer, new Callback() {
                    @Override
                    public void onSuccess() { // Swaps display (ImageView <-> IconTextView)
                        displayLoadingSpinner(holder, false);
                    }

                    @Override
                    public void onError() {
                        displayLoadingSpinner(holder, false);
                        holder.imageViewPlayer.setImageDrawable(createErrorDrawable());
                    }
                });

        return rowView;
    }

    private Drawable createErrorDrawable() {
        return new IconDrawable(context, FontAwesomeIcons.fa_exclamation_circle)
                .colorRes(R.color.colorPrimaryDark)
                .actionBarSize();
    }

    private void displayLoadingSpinner(Holder holder, boolean value) {
        if (value) {
            holder.iconTextViewLoading.setVisibility(View.VISIBLE);
            holder.imageViewPlayer.setVisibility(View.GONE);
        } else {
            holder.iconTextViewLoading.setVisibility(View.GONE);
            holder.imageViewPlayer.setVisibility(View.VISIBLE);
        }
    }

    public void updatePlayers(List<Player> players) {
        mPlayers = players;
        //Triggers the list update
        notifyDataSetChanged();
    }
}
