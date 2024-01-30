package bg.sofia.uni.fmi.mjt.authserver.server;

import bg.sofia.uni.fmi.mjt.authserver.database.DatabaseManager;
import bg.sofia.uni.fmi.mjt.authserver.exception.InvalidCommandException;
import bg.sofia.uni.fmi.mjt.authserver.exception.InvalidLoginException;
import bg.sofia.uni.fmi.mjt.authserver.exception.InvalidSessionException;
import bg.sofia.uni.fmi.mjt.authserver.exception.PermissionException;
import bg.sofia.uni.fmi.mjt.authserver.exception.TemporaryLockException;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserExistsException;
import bg.sofia.uni.fmi.mjt.authserver.log.AuditLog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;

public class Server {
    public static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;
    private boolean isServerWorking;
    private Selector selector;
    private final AuditLog auditLog;
    private final SessionManager sessionManager;
    private final UserManager userManager;

    public Server(String dbPath, String logPath) throws IOException {
        isServerWorking = true;
        auditLog = new AuditLog(logPath);
        sessionManager = new SessionManager();
        DatabaseManager databaseManager = new DatabaseManager(dbPath, auditLog, sessionManager);
        userManager = new UserManager(auditLog, databaseManager, sessionManager);
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            while (isServerWorking) {
                handleClientRequests(buffer);
            }
        } catch (IOException e) {
            isServerWorking = false;
            throw new RuntimeException("Failed to start server", e);
        }
    }

    private void handleClientRequests(ByteBuffer buffer) throws IOException {
        sessionManager.cleanSessions();
        int readyChannels = selector.select();
        if (readyChannels == 0) {
            return;
        }
        Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            processKey(buffer, key);
            keyIterator.remove();
        }
    }

    private void processKey(ByteBuffer buffer, SelectionKey key) throws IOException {
        if (key.isReadable()) {
            processReadableKey(buffer, key);
        } else if (key.isAcceptable()) {
            accept(selector, key);
        }
    }

    private void processReadableKey(ByteBuffer buffer, SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        String ip;
        try {
            ip = clientChannel.getRemoteAddress().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String clientInput = getClientInput(clientChannel, buffer);
        if (clientInput != null) {
            try {
                writeClientOutput(clientChannel, userManager.parseCommand(clientInput, ip), buffer);
            } catch (InvalidCommandException e) {
                writeClientOutput(clientChannel, "Invalid input, please try again: " + e.getMessage(), buffer);
            } catch (UserExistsException e) {
                writeClientOutput(clientChannel, "Username is taken, please try again: " + e.getMessage(), buffer);
            } catch (InvalidSessionException e) {
                writeClientOutput(clientChannel, "Invalid session, please login/register: " + e.getMessage(), buffer);
            } catch (PermissionException e) {
                writeClientOutput(clientChannel, "There was a permissions issue: " + e.getMessage(), buffer);
            } catch (TemporaryLockException e) {
                writeClientOutput(clientChannel, "You have been temporarily locked: " + e.getMessage(), buffer);
            } catch (InvalidLoginException e) {
                writeClientOutput(clientChannel, "Invalid login: " + e.getMessage(), buffer);
            } catch (Exception e) {
                writeClientOutput(clientChannel, "There was a problem with the server, try again later", buffer);
                try {
                    auditLog.logError(ip + "\n" + Arrays.toString(e.getStackTrace()));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel, ByteBuffer buffer) {
        buffer.clear();
        try {
            int readBytes = clientChannel.read(buffer);
            if (readBytes < 0) {
                clientChannel.close();
                return null;
            }
            buffer.flip();

            byte[] clientInputBytes = new byte[buffer.remaining()];
            buffer.get(clientInputBytes);

            return new String(clientInputBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            try {
                clientChannel.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            //add log
        }
        return null;
    }

    private void writeClientOutput(SocketChannel clientChannel, String output, ByteBuffer buffer) {
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();

        try {
            clientChannel.write(buffer);
        } catch (IOException e) {
            //add log
            try {
                clientChannel.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }
}