package NonBlocking_Hangman_Server.database;

public class Database {
    String username;
    String password;

    public Database(){
        username = "erik";
        password = "pass";
    }

    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
}
