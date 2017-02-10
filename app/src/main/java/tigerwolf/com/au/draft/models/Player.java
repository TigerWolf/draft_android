package tigerwolf.com.au.draft.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Henrique on 10/02/2017.
 */

public class Player {

    @SerializedName("playerId")
    private String id;
    private String givenName;
    private String surname;
    private String photoURL;
    private int jumper;
    private Team team;

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
}
