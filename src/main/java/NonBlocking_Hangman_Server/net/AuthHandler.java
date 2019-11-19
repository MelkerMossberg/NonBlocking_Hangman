package NonBlocking_Hangman_Server.net;

import NonBlocking_Hangman_Server.database.Database;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.io.IOException;
import java.security.Key;

public class AuthHandler {
    boolean askedForUsername = false;
    boolean askedForPassword = false;
    String givenUsername;
    String givenPassword;
    Database db;
    Key key;
    ClientSession clientSession;

    public AuthHandler(ClientSession clientSession){
        db = new Database();
        this.clientSession = clientSession;
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    public String handleLogin(String input) {
        if (!askedForUsername){
            askedForUsername = true;
            return "Please enter username: ";
        }
        if (!askedForPassword){
            askedForPassword = true;
            //Todo: Check if username exists in db. Otherwise ask for username again
            givenUsername = input;
            return "Please enter password: ";
        }
        //Todo: Check if password matches given username. Otherwise ask for password again
        givenPassword = input;
        if (credentialsCorrect()){
            return "Login success";
        }else return "Failed to login";
    }

    String createJWT() {
        try {
            String jws = Jwts.builder().setSubject(givenUsername).signWith(key).compact();
            return jws;
        } catch (JwtException e) {
            e.printStackTrace();
        }
        return "Create JWT did not work";
    }

    private boolean credentialsCorrect() {
        return givenUsername.equals(db.getUsername()) && givenPassword.equals(db.getPassword());
    }

    void verifyJWT(String jwt) {
        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(jwt);

        } catch (JwtException e) {
            // Close connection if JWT exception is thrown
            e.printStackTrace();
            try {
                clientSession.socketChannel.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
