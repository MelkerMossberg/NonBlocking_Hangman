package NonBlocking_Hangman_Server.net;

import NonBlocking_Hangman_Server.game.GameHandler;
import org.json.simple.JSONObject;

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

    ClientSession(SelectionKey selectionKey, SocketChannel socketChannel){
        this.selectionKey = selectionKey;
        this.socketChannel = socketChannel;
        state = LOGIN;
        authHandler = new AuthHandler();
        gameHandler = new GameHandler();
    }

    String handleClientAction(byte[] data) {
        switch (state){
            case LOGIN: {
                String output = authHandler.handleLogin(data);
                System.out.println(output);
                if (output.equals("Login success")) {
                    state = GAME_START;
                    return handleClientAction(null);
                }
                return packageJSON(output);
            }
            case GAME_START: {
                String output = gameHandler.startFirstGame();
                state = KEEP_GUESSING;
                return packageJSON(output);
            }
            case KEEP_GUESSING: {
                String input = new String(data).trim();
                if (input.equals("quit")) return input;
                String output = gameHandler.handleGuess(input.toLowerCase());
                return packageJSON(output);
            }
        }
        return "If we end up here, ClientSession.handleClientAction() is not working";
    }

    private String packageJSON(String body) {
        JSONObject message = new JSONObject();
        if (state == LOGIN) message.put("state", "login");
        else message.put("state", "game");

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
