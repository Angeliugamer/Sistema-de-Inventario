package Models;

public class User {
    private String id;
    private String username;
    private String passwordHash;
    private String passwordSalt;
    private boolean admin;

    public User() {}

    public User(String id, String username,
                String passwordHash, String passwordSalt, boolean admin) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.admin = admin;
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
    public boolean isAdmin() {return admin;}
}
