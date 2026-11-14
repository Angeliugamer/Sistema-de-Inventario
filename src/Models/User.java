package Models;

public class User {
    private String id;
    private String username;
    private String passwordHash;
    private String passwordSalt;

    public User(String id, String username,
                String passwordHash, String passwordSalt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }
}
