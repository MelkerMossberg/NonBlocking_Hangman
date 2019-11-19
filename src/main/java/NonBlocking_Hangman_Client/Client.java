package NonBlocking_Hangman_Client;


import NonBlocking_Hangman_Client.view.GameStateDTO;
import NonBlocking_Hangman_Client.view.View;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class Client implements Runnable {

    public static void main(String[] args) {
        Thread client = new Thread(new Client());
        client.start();
    }

    private final static String HOSTNAME = "localhost";
    private final static int PORT = 44444;
    private final static long TIMEOUT = 10000;

    private SocketChannel socketChannel;
    private Selector selector;

    View view;
    GameStateDTO gameStateDTO;
    private JSONParser jsonParser;
    private InputHandler inputHandler;
    private Map<SocketChannel, byte[]> dataToBeHandled = new HashMap<>();

    private Client() {
        init();
        view = new View();
        inputHandler = new InputHandler(this);
        gameStateDTO = new GameStateDTO();
        jsonParser = new JSONParser();
    }

    private void init() {
        if (selector != null) return;
        if (socketChannel != null) return;

        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(HOSTNAME, PORT));
            System.out.println("Connecting to Server on port " + PORT);

            selector = Selector.open();
            //Tell the SelectionKey that the serverSocketChannel should be used to accept connections.
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            System.out.println("OP_Connect");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("Running...");
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // selector.select(TIMEOUT) makes sure we don't block undefinable.
                // Will pass this as soon as we get a connection
                selector.select(TIMEOUT);

                // TIMEOUT expired or we got an event.
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isConnectable()) {
                        System.out.println("Connecting...");
                        connect(key);
                    }

                    if (key.isWritable()) {
                        write(key);
                    }

                    if (key.isReadable()) {
                        read(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void connect(SelectionKey key) throws IOException {
        socketChannel.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        byte[] data = dataToBeHandled.get(channel);
        dataToBeHandled.remove(channel);

        // NIO Writes to the clientChannel
        channel.write(ByteBuffer.wrap(data));

        // After writing we want to be reading on this thread. Create a operation-request.
        key.interestOps(SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        readBuffer.clear();

        int read;
        try {
            read = channel.read(readBuffer);
        } catch (IOException e) {
//            System.out.println("Reading problem, closing connection");
            e.printStackTrace();
            key.cancel();
            channel.close();
            return;
        }

        if (read == -1) {
            System.out.println("Nothing was there to be read, closing connection");
            channel.close();
            key.cancel();
            return;
        }
        // IMPORTANT - don't forget the flip() the buffer. It is like a reset without clearing it.
        readBuffer.flip();
        byte[] data = new byte[1000];
        readBuffer.get(data, 0, read);

        //System.out.println("Received: " + new String(data).trim());
        inputHandler.translateToView(new String(data).trim());

        prepareNextOutput(key);
    }

    private void prepareNextOutput(SelectionKey key) {
        String response = readKeyboardInput();

        byte[] byteResponse = response.getBytes();

        SocketChannel socketChannel = (SocketChannel) key.channel();
        dataToBeHandled.put(socketChannel, byteResponse);
        key.interestOps(SelectionKey.OP_WRITE);
    }

    void closeConnection() {
        System.out.println("Closing server down");
        if (selector != null) {
            try {
                selector.close();
                socketChannel.socket().close();
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public String readKeyboardInput() {
        Scanner console = new Scanner(System.in);
        if (console.hasNext()) {
            String userInput = console.nextLine();
            return userInput;
        } else return readKeyboardInput();
    }
}