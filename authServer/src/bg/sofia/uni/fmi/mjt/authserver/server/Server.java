package bg.sofia.uni.fmi.mjt.authserver.server;

import bg.sofia.uni.fmi.mjt.authserver.ban.BanManager;
import bg.sofia.uni.fmi.mjt.authserver.database.DatabaseManager;
import bg.sofia.uni.fmi.mjt.authserver.log.AuditLog;
import bg.sofia.uni.fmi.mjt.authserver.log.AuditLogApi;
import bg.sofia.uni.fmi.mjt.authserver.response.Response;
import bg.sofia.uni.fmi.mjt.authserver.session.SessionManager;
import bg.sofia.uni.fmi.mjt.authserver.session.SessionManagerApi;
import bg.sofia.uni.fmi.mjt.authserver.user.UserManager;
import bg.sofia.uni.fmi.mjt.authserver.user.UserManagerApi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class Server {
    public final int serverPort;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;
    private boolean isServerWorking;
    private Selector selector;
    private final AuditLogApi auditLog;
    private final SessionManagerApi sessionManager;
    private final UserManagerApi userManager;

    public Server(int port, String dbPath, String logPath, int maxAttempts, long banDuration) throws IOException {
        isServerWorking = true;
        serverPort = port;
        auditLog = new AuditLog(logPath);
        sessionManager = new SessionManager();
        DatabaseManager databaseManager = new DatabaseManager(dbPath);
        userManager = new UserManager(auditLog, databaseManager, sessionManager,
                new BanManager(maxAttempts, banDuration));
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
        sessionManager.cleanSessions();
        try {
            String ip = getClientIp(clientChannel);
            String clientInput = getClientInput(clientChannel, buffer);
            handleClientInput(clientChannel, clientInput, ip, buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getClientIp(SocketChannel clientChannel) throws IOException {
        try {
            return clientChannel.getRemoteAddress().toString();
        } catch (IOException e) {
            auditLog.logError("can't get client ip", e);
            return "";
        }
    }

    private void handleClientInput(SocketChannel clientChannel, String clientInput, String ip, ByteBuffer buffer) {
        try {
            Response response = userManager.parseCommand(clientInput, ip);
            writeClientOutput(clientChannel, response.toString(), buffer);
        } catch (Exception e) {
            writeClientOutput(clientChannel, "There was a problem with the server, try again later", buffer);
            try {
                auditLog.logError(ip, e);
            } catch (IOException ex) {
                System.out.println("can't write log: " + ex.getMessage());
            }
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(SERVER_HOST, serverPort));
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