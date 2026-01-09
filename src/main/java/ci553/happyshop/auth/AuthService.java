package ci553.happyshop.auth;

import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private final Map<String, String> users = new HashMap<>();

    public AuthService() {
        // I have coded in basic test users for coursework friendliness and simulation purposes
        users.put("developer", "developer");
        users.put("user", "password");
    }

    public boolean authenticate(String username, String password) {
        return users.containsKey(username)
                && users.get(username).equals(password);
    }
}

