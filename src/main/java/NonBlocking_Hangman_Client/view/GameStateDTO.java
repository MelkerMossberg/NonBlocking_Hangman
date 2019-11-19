package NonBlocking_Hangman_Client.view;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GameStateDTO {
    public String state;
    public String previousWord;
    public String guessedLetters;
    public int gameScore;
    public int totalLives;
    public int livesLeft;
    public int numCorrectGuesses;
    public String theHiddenWord;
}
