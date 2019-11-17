package NonBlocking_Hangman_Client.net;

import NonBlocking_Hangman_Client.Client;
import NonBlocking_Hangman_Client.view.View;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ConnectionHandler extends Thread{

    Socket socket;
    Client client;
    View view;
    BufferedReader din;
    PrintWriter dout;
    volatile boolean shouldRun = true;
    JSONParser jsonParser;
    public String jwt;

    public ConnectionHandler(Socket socket, Client client) {
        this.socket = socket;
        try {
            socket.setKeepAlive(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        this.client = client;
        this.view = new View();
        this.jsonParser = new JSONParser();
        try {
            din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            dout = new PrintWriter(socket.getOutputStream(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    // Listen for server input
    public void run() {
        int s = 0;

        while (shouldRun){
            System.out.println("Run()");
            try{
                StringBuilder sb = new StringBuilder();
                while(din.ready()){
                    s = (char) din.read();
                    sb.append((char)s);
                }
                System.out.println(sb.toString());
                break;
                /*while((s = din.read())>=0){
                    System.out.print(s);
                }*/
                //System.out.println(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void stateHandler(String input) {

        JSONObject message = (JSONObject) parseJSONObject(input);
        String state = message.get("state").toString();

        switch(state) {
            case "quit":
                closeConnection();
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

    private Object parseJSONObject(String input) {
        Object JSONInput = null;
        try {
            JSONInput = jsonParser.parse(input);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return JSONInput;
    }

    private boolean controlByteLength(String body, int promisedLength) {
        int measuredLength = measureStringByteLength(body);
        System.out.println("Measured: " + measuredLength + ", Promised: " + promisedLength);
        return measuredLength == promisedLength;
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

    private void closeConnection() {
        System.out.print("Quitting...");
        shouldRun = false;
        try {
            din.close();
            socket.close();
            dout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(1);
    }

    public void sendToServer(String body){
        JSONObject message = new JSONObject();
        message.put("jwt", this.jwt);
        message.put("body", body);
        message.put("content-length", measureStringByteLength(body));

        dout.println(message.toJSONString());
        dout.flush();
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
}
