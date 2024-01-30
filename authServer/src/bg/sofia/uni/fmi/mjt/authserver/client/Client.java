package bg.sofia.uni.fmi.mjt.authserver.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

// NIO, blocking
public class Client {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;
    private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

    void start() {
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            if (!connectToServer(socketChannel)) {
                return;
            }

            System.out.println("Connected to the server.");

            while (true) {
                String message = scanner.nextLine(); // read a line from the console
                if ("quit".equals(message)) {
                    break;
                }

                if (!sendMessage(socketChannel, message) || !receiveAndPrintReply(socketChannel)) {
                    break;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the network communication", e);
        }
    }

    private boolean connectToServer(SocketChannel socketChannel) {
        try {
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            return true;
        } catch (IOException e) {
            System.out.println("Can't connect to the server, try again");
            return false;
        }
    }

    private boolean sendMessage(SocketChannel socketChannel, String message) throws IOException {
        buffer.clear(); // switch to writing mode
        buffer.put(message.getBytes()); // buffer fill
        buffer.flip(); // switch to reading mode

        try {
            socketChannel.write(buffer); // buffer drain
            return true;
        } catch (IOException e) {
            System.out.println("Disconnected from server, aborting...");
            return false;
        }
    }

    private boolean receiveAndPrintReply(SocketChannel socketChannel) throws IOException {
        buffer.clear(); // switch to writing mode
        try {
            socketChannel.read(buffer); // buffer fill
            buffer.flip(); // switch to reading mode

            byte[] byteArray = new byte[buffer.remaining()];
            buffer.get(byteArray);
            String reply = new String(byteArray, StandardCharsets.UTF_8); // buffer drain

            System.out.println("The server replied <" + reply + ">");
            return true;
        } catch (IOException e) {
            System.out.println("Disconnected from server, aborting...");
            return false;
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
}