package tigerwolf.com.au.draft.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Henrique on 10/02/2017.
 */

public class Team {

    @SerializedName("teamId")
    private String id;
    @SerializedName("teamNickname")
    private String nickname;
    @SerializedName("teamName")
    private String name;
    @SerializedName("teamAbbr")
    private String abbreviation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", name='" + name + '\'' +
                ", abbreviation='" + abbreviation + '\'' +
                '}';
    }
}
