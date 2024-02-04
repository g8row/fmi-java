package bg.sofia.uni.fmi.mjt.authserver.client;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Scanner;

// NIO, blocking
public class Client {

    private final int serverPort;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;
    private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    private final String logDir;

    public Client(int port, String logDir) throws IOException {
        this.logDir = logDir;
        serverPort = port;
        createLogDirIfNotExists();
    }

    private void createLogDirIfNotExists() throws IOException {
        Path path = Paths.get(logDir);
        if (Files.notExists(path)) {
            Files.createDirectory(path);
        }
    }

    public void start() {
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
            System.out.println("Unable to open socket. " +
                    "Try again later or contact administrator by providing the logs in " + logDir);
        }
    }

    private boolean connectToServer(SocketChannel socketChannel) {
        try {
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, serverPort));
            return true;
        } catch (IOException e) {
            System.out.println("Unable to connect to the server. " +
                    "Try again later or contact administrator by providing the logs in " + logDir);
            try {
                logClientError(e);
            } catch (IOException ex) {
                System.out.println("can't write logs." + ex.getMessage());
            }
            return false;
        }
    }

    private boolean sendMessage(SocketChannel socketChannel, String message) {
        buffer.clear(); // switch to writing mode
        buffer.put(message.getBytes()); // buffer fill
        buffer.flip(); // switch to reading mode
        try {
            socketChannel.write(buffer); // buffer drain
            return true;
        } catch (IOException e) {
            System.out.println("Unable to send message to the server. " +
                    "Try again later or contact administrator by providing the logs in " + logDir);
            try {
                logClientError(e);
            } catch (IOException ex) {
                System.out.println("can't write logs." + ex.getMessage());
            }
            return false;
        }

    }

    private boolean receiveAndPrintReply(SocketChannel socketChannel) {
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
            System.out.println("Unable to receive message server. " +
                    "Try again later or contact administrator by providing the logs in " + logDir);
            try {
                logClientError(e);
            } catch (IOException ex) {
                System.out.println("can't write logs." + ex.getMessage());
            }
            return false;
        }
    }

    private void logClientError(Exception e) throws IOException {
        try (FileWriter fileWriter = new FileWriter(Paths.get(logDir + "/" + getDateHour()) + ".txt")) {
            fileWriter.write(e.getMessage() + System.lineSeparator() + Arrays.toString(e.getStackTrace()));
        }
    }

    private String getDateHour() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd+HH-mm-ss")
                .format(LocalDateTime.now());
    }
}