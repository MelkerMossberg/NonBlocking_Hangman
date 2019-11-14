package NonBlocking_Hangman_Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectionHandler extends Thread{

    Socket socket;
    Client client;
    BufferedReader din;
    PrintWriter dout;
    volatile boolean shouldRun = true;

    public ConnectionHandler(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;
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
        while (shouldRun){
            try{
                String input = din.readLine();
                System.out.println(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendToServer(String body){
        dout.println(body);
        dout.flush();
    }
}
