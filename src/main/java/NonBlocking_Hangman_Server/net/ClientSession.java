package NonBlocking_Hangman_Server.net;

import NonBlocking_Hangman_Server.game.GameHandler;

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
                if (output.equals("Login success")) {
                    state = GAME_START;
                    return handleClientAction(null);
                }
                return output;
            }
            case GAME_START: {
                String output = gameHandler.startFirstGame();
                state = KEEP_GUESSING;
                return output;
            }
            case KEEP_GUESSING: {
                String input = new String(data).trim();
                String output = gameHandler.handleGuess(input.toLowerCase());
                if (input.equals("quit")) return input;
                return output;
            }
        }

        return "If we end up here, ClientSession.handleClientAction() is not working";
    }

}
