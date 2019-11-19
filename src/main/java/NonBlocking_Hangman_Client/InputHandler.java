package NonBlocking_Hangman_Client;

import NonBlocking_Hangman_Client.view.GameStateDTO;
import NonBlocking_Hangman_Client.view.View;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class InputHandler {

    JSONParser jsonParser;
    Client client;
    View view;
    public String jwt;

    InputHandler(Client client){
        this.jsonParser = new JSONParser();
        this.client = client;
        this.view = new View();
    }

    public void translateToView(String input) {
        JSONObject message = (JSONObject) parseJSONObject(input);
        String state = message.get("state").toString();

        switch(state) {
            case "quit":
                client.closeConnection();
                break;
            case "login":
                System.out.println(message.get("body").toString());
                break;
            case "loginSuccess":
                jwt = message.get("body").toString();
                break;
            case "game":
                String body = message.get("body").toString();
                controlByteLength(body, Integer.parseInt(message.get("content-length").toString()));
                GameStateDTO gameState = readGameState(body);
                view.UpdateGameView(gameState);
                break;
        }
    }

    private boolean controlByteLength(String body, int promisedLength) {
        int measuredLength = measureStringByteLength(body);
        System.out.println("Measured: " + measuredLength + ", Promised: " + promisedLength);
        return measuredLength == promisedLength;
    }
    private int measureStringByteLength(String inputBody) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(inputBody);
            objectOutputStream.flush();
            objectOutputStream.close();
            int length = byteArrayOutputStream.toByteArray().length;
            return length;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private Object parseJSONObject(String input) {
        Object JSONInput = null;
        try {
            JSONInput = jsonParser.parse(input);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return JSONInput;
    }
    private GameStateDTO readGameState(String input) {
        GameStateDTO gameState = new GameStateDTO();
        Object JSONInput = null;
        try {
            JSONInput = jsonParser.parse(input);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONObject wrapper = (JSONObject) JSONInput;
        JSONObject obj = (JSONObject) wrapper.get("gameState");
        gameState.state = (String) obj.get("state");
        gameState.gameScore = Integer.parseInt(obj.get("score").toString());
        gameState.totalLives = Integer.parseInt(obj.get("totalLives").toString());
        gameState.livesLeft = Integer.parseInt(obj.get("livesLeft").toString());
        gameState.numCorrectGuesses = Integer.parseInt(obj.get("numCorrect").toString());
        gameState.guessedLetters = (String)obj.get("prevGuesses");
        gameState.previousWord = (String)obj.get("prevWord");
        gameState.theHiddenWord = (String)obj.get("hiddenWord");

        return gameState;
    }
}
