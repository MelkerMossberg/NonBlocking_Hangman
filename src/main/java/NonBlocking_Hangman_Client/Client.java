package NonBlocking_Hangman_Client;

import NonBlocking_Hangman_Client.net.ConnectionHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    ConnectionHandler cc;

    public static void main(String[]args){
        new Client();
    }

    public Client(){
        try{
            Socket socket = new Socket("localhost", 44444);
            cc = new ConnectionHandler(socket, this);
            cc.start();

            listenUserInput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenUserInput(){
        Scanner console = new Scanner(System.in);
        while(true){
            if (console.hasNext()){
                String userInput = console.nextLine();
                cc.sendToServer(userInput);
            }

        }
    }
}
