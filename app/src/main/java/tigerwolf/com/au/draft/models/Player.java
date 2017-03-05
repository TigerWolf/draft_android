package tigerwolf.com.au.draft.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Henrique on 10/02/2017.
 */

public class Player implements Comparable<Player> {

    @SerializedName("playerId")
    private String id;
    private String givenName;
    private String surname;
    private String photoURL;
    private int jumper;
    private boolean drafted = false;
    private List<String> positions = null;
    private Team team;
    private transient boolean myTeam = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public int getJumper() {
        return jumper;
    }

    public void setJumper(int jumper) {
        this.jumper = jumper;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public String getPositionsAsString() {
        if (getPositions() == null || getPositions().size() == 0) return "";

        StringBuilder result = new StringBuilder();

        for(String p : getPositions()) {
            result.append(p).append(" ");
        }

        return result.toString().substring(0, result.toString().length() - 2);
    }

    @Override
    public String toString() {
        return "Player{" +
                "id='" + id + '\'' +
                ", givenName='" + givenName + '\'' +
                ", surname='" + surname + '\'' +
                ", photoURL='" + photoURL + '\'' +
                ", jumper=" + jumper +
                ", team=" + team +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return id.equals(player.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public boolean isDrafted() {
        return drafted;
    }

    public void setDrafted(boolean drafted) {
        this.drafted = drafted;
    }

    @Override
    public int compareTo(Player o) {
        String myName = givenName + " " + surname;
        String hisName = o.givenName + " " + o.surname;
        return myName.compareTo(hisName);
    }

    public List<String> getPositions() {
        return positions;
    }

    public void setPositions(List<String> positions) {
        this.positions = positions;
    }

    public boolean isMyTeam() {
        return myTeam;
    }

    public void setMyTeam(boolean myTeam) {
        this.myTeam = myTeam;
    }
}
