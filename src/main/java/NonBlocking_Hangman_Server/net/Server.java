package NonBlocking_Hangman_Server.net;

import java.io.IOException;
        import java.net.InetSocketAddress;
        import java.nio.ByteBuffer;
        import java.nio.channels.SelectionKey;
        import java.nio.channels.Selector;
        import java.nio.channels.ServerSocketChannel;
        import java.nio.channels.SocketChannel;
        import java.util.HashMap;
        import java.util.Iterator;
        import java.util.Map;

public class Server implements Runnable {

    public static void main(String[] args) {
        Thread server = new Thread(new Server());
        server.start();
    }

    private final static String HOSTNAME = "localhost";
    private final static int PORT = 44444;
    private final static long TIMEOUT = 10000;

    private ServerSocketChannel serverChannel;
    private Selector selector;

    private Map<SocketChannel, byte[]> dataToBeHandled = new HashMap<>();
    private final static HashMap<SelectionKey, ClientSession> clientMap = new HashMap<>();

    private Server() { init(); }

    private void init() {
        System.out.println("initializing server");
        if (selector != null) return;
        if (serverChannel != null) return;

        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(HOSTNAME, PORT));

            selector = Selector.open();
             //Tell the SelectionKey that the serverSocketChannel should be used to accept connections.
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("Now accepting connections...");
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

                    if (key.isAcceptable()) {
                        System.out.println("Accepting connection");
                        accept(key);
                    }

                    if (key.isWritable()) {
                        String client = clientMap.get(key).socketChannel.getRemoteAddress().toString();
                        System.out.println("Writing to..." + client);
                        write(key);
                    }

                    if (key.isReadable()) {
                        String client = clientMap.get(key).socketChannel.getRemoteAddress().toString();
                        System.out.println("Reading connection from: " + client);
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

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        // Add a operation to the Selector. It should be a write-operation
        SelectionKey socketKey = socketChannel.register(selector, SelectionKey.OP_WRITE);

        // Start a clientSession (which is identified by its socketChannel). Add this to a hash-map of clients.
        clientMap.put(socketKey, new ClientSession(socketKey, socketChannel));

        String messageToClient = clientMap.get(socketKey).handleClientAction(null);
        byte[] messageBytes = messageToClient.getBytes();
        dataToBeHandled.put(socketChannel, messageBytes);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        byte[] data = dataToBeHandled.get(channel);
        dataToBeHandled.remove(channel);

        // NIO Writes to the clientChannel
        System.out.println("Server Respond: " + new String(data));
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
        System.out.println("Received: " + new String(data).trim());

        // If input is 'quit
        if (new String(data).trim().equalsIgnoreCase("quit")){
            key.cancel();
            channel.close();
            return;
        }

        respond(key, data);
    }

    private void respond(SelectionKey key, byte[] data) {
        String response = clientMap.get(key).handleClientAction(data) + "\n";
        byte[] byteResponse = response.getBytes();

        SocketChannel socketChannel = (SocketChannel) key.channel();
        dataToBeHandled.put(socketChannel, byteResponse);
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void closeConnection() {
        System.out.println("Closing server down");
        if (selector != null) {
            try {
                selector.close();
                serverChannel.socket().close();
                serverChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}