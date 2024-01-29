package bg.sofia.uni.fmi.mjt.authserver.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Scanner;

// NIO, blocking
public class Client {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;
    private boolean loggedIn = false;
    private Session session = null;
    private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

    void start(){
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            System.out.println("Connected to the server.");

            while (true) {
                /*if (loggedIn && session.getTtl().isBefore(LocalDateTime.now())) {
                    session = null;
                    loggedIn = false;
                    System.out.println("session is over, login again");
                }*/
                String message = scanner.nextLine(); // read a line from the console
                if ("quit".equals(message)) {
                    break;
                }
                buffer.clear(); // switch to writing mode
                buffer.put(message.getBytes()); // buffer fill
                buffer.flip(); // switch to reading mode
                socketChannel.write(buffer); // buffer drain

                buffer.clear(); // switch to writing mode
                socketChannel.read(buffer); // buffer fill
                buffer.flip(); // switch to reading mode

                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);
                String reply = new String(byteArray, "UTF-8"); // buffer drain

                System.out.println("The server replied <" + reply + ">");
            }

        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the network communication", e);
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
}