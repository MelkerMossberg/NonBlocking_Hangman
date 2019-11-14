package NonBlocking_Hangman_Server.net;

import NonBlocking_Hangman_Server.database.Database;

public class AuthHandler {
    boolean askedForUsername = false;
    boolean askedForPassword = false;
    String givenUsername;
    String givenPassword;
    Database db;

    public AuthHandler(){
        db = new Database();
    }

    public String handleLogin(byte[] input) {
        if (!askedForUsername){
            askedForUsername = true;
            return "Please enter username: ";
        }
        if (!askedForPassword){
            askedForPassword = true;
            //Todo: Check if username exists in db. Otherwise ask for username again
            givenUsername = new String(input).trim();
            return "Please enter password: ";
        }
        //Todo: Check if password matches given username. Otherwise ask for password again
        givenPassword = new String(input).trim();
        if (credentialsCorrect()){
            return "Login success";
        }else return "Failed to login";
    }

    private boolean credentialsCorrect() {
        return givenUsername.equals(db.getUsername()) && givenPassword.equals(db.getPassword());
    }
}
