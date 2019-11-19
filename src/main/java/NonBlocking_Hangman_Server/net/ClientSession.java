package NonBlocking_Hangman_Server.net;

import NonBlocking_Hangman_Server.game.GameHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ClientSession {

    SelectionKey selectionKey;
    SocketChannel socketChannel;
    private final static int LOGIN = 0, GAME_START = 1, KEEP_GUESSING = 2;
    private int state;
    private AuthHandler authHandler;
    private GameHandler gameHandler;
    JSONParser jsonParser;

    ClientSession(SelectionKey selectionKey, SocketChannel socketChannel){
        this.selectionKey = selectionKey;
        this.socketChannel = socketChannel;
        state = LOGIN;
        authHandler = new AuthHandler(this);
        gameHandler = new GameHandler();
        jsonParser = new JSONParser();
    }

    String handleClientAction(byte[] data) {
        String body = null;
        if (data != null){
            System.out.println(new String(data).trim());
            body = parseInput("body", new String(data).trim());
        }

        switch (state){
            case LOGIN: {
                String output = authHandler.handleLogin(body);
                System.out.println(output);
                if (output.equals("Login success")) {
                    state = GAME_START;
                    return handleClientAction(null);
                }
                return packageJSON(output, null);
            }
            case GAME_START: {
                String output = gameHandler.startFirstGame();
                state = KEEP_GUESSING;
                String jwtToSend = authHandler.createJWT();
                return packageJSON(output, jwtToSend);
            }
            case KEEP_GUESSING: {
                if (body.equals("quit")) return "quit";
                String jwt =  parseInput("jwt", new String(data).trim());
                authHandler.verifyJWT(jwt);
                String output = gameHandler.handleGuess(body.toLowerCase());
                return packageJSON(output, null);
            }
        }
        return "If we end up here, ClientSession.handleClientAction() is not working";
    }

    String parseInput(String selector, String input) {
        Object JSONInput = null;
        try {
            JSONInput = jsonParser.parse(input);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONObject message = (JSONObject) JSONInput;
        return message.get(selector).toString();
    }

    private String packageJSON(String body, String newJWT) {
        JSONObject message = new JSONObject();
        if (state == LOGIN) message.put("state", "login");
        else message.put("state", "game");
        message.put("jwt", newJWT);
        message.put("body", body);
        int contentLength = measureStringByteLength(body);
        message.put("content-length", Integer.toString(contentLength));
        return message.toJSONString();
    }

    private int measureStringByteLength(String input) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(input);
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
