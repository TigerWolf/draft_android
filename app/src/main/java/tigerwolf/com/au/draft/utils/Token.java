package tigerwolf.com.au.draft.utils;

/**
 * Created by Henrique on 10/02/2017.
 */

public class Token {

    private String value;
    private boolean loggedIn;

    /**
     * Clears token value and sets loggedIn = false
     */
    public void clearToken() {
        this.value = null;
        this.loggedIn = false;
    }

    public void setToken(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public void setLoggedIn(boolean value) {
        this.loggedIn = value;
    }

    public boolean isLoggedIn() {
        return this.loggedIn;
    }

    @Override
    public String toString() {
        return "Token{" +
                "value='" + value + '\'' +
                '}';
    }
}
