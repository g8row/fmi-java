package bg.sofia.uni.fmi.mjt.authserver.server;

import bg.sofia.uni.fmi.mjt.authserver.client.Session;
import bg.sofia.uni.fmi.mjt.authserver.exception.InvalidCommandException;
import bg.sofia.uni.fmi.mjt.authserver.exception.InvalidSessionException;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserExistsException;
import bg.sofia.uni.fmi.mjt.authserver.user.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Server {
    public static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;

    private static final String DB_PATH = "src/bg/sofia/uni/fmi/mjt/authserver/database/db.txt";
    private final Collection<Session> sessions = new ArrayList<>();
    private boolean isServerWorking;
    private Selector selector;

    public Server() throws IOException {
        createDbFileIfNotExists();
    }

    String parseCommand(String command) throws IOException {
        if (command == null) {
            throw new InvalidCommandException("command is null");
        }
        String[] args = command.split(" +");

        Iterator<String> it = Arrays.stream(args).iterator();
        try {
            switch (Command.valueOf(it.next().replaceAll("-", "_").toUpperCase())) {
                case Command.REGISTER -> {
                    return register(it);
                }
                case Command.LOGIN -> {
                    return login(it);
                }
                case Command.LOGOUT -> {
                    return logout(it);
                }
                case Command.UPDATE_USER -> {
                    return updateUser(it);
                }
                case Command.RESET_PASSWORD -> {
                    return resetPassword(it);
                }
                case Command.ADD_ADMIN_USER -> {
                    return addAdminUser(it);
                }
                default -> throw new InvalidCommandException("invalid input");
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidCommandException("invalid input", e);
        }
    }

    private Session getSessionByUser(String userId) {
        return sessions.stream().filter(x -> x.getUserId().equals(userId)).findFirst().orElse(null);
    }

    private Session getSessionBySessionId(String sessionId) {
        return sessions.stream().filter(x -> x.getSessionId().equals(sessionId)).findFirst().orElse(null);
    }

    private void checkSession(String sessionId) {
        if (sessionId == null || getSessionBySessionId(sessionId) == null) {
            throw new InvalidSessionException("Session is not valid");
        }
    }

    private String addAdminUser(Iterator<String> it) {
        String sessionId = null;
        String username = null;
        while (it.hasNext()) {
            Map.Entry<String, String> optionAndArg = getNextOptionAndArg(it);
            switch (optionAndArg.getKey()) {
                case "--username" -> username = optionAndArg.getValue();
                case "--session-id" -> sessionId = optionAndArg.getValue();
                default -> throw new InvalidCommandException("Unknown option: " + optionAndArg.getKey());
            }
        }
        List<String> strings = null;
        try {
            strings = Files.readAllLines(Path.of(DB_PATH));
            for (String string : strings) {
                User user = User.of(string);
                if (user.getUserId().equals(getSessionBySessionId(sessionId).getUserId()) && user.isAdmin()) {
                    //editInDb();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "added admin user";
    }

    private String resetPassword(Iterator<String> it) throws IOException {
        String sessionId = null;
        String username = null;
        String oldPassword = null;
        String newPassword = null;
        while (it.hasNext()) {
            Map.Entry<String, String> optionAndArg = getNextOptionAndArg(it);
            switch (optionAndArg.getKey()) {
                case "--username" -> username = optionAndArg.getValue();
                case "--new-password" -> newPassword = optionAndArg.getValue();
                case "--old-password" -> oldPassword = optionAndArg.getValue();
                case "--session-id" -> sessionId = optionAndArg.getValue();
                default -> throw new InvalidCommandException("Unknown option: " + optionAndArg.getKey());
            }
        }
        if (sessionId == null || username == null || oldPassword == null || newPassword == null) {
            throw new InvalidCommandException("All arguments are required");
        }
        checkSession(sessionId);
        editInDb(sessionId, username, null, null, null, oldPassword, newPassword);
        return "successfully reset password";
    }

    private String updateUser(Iterator<String> it) throws IOException {
        String sessionId = null;
        String username = null;
        String email = null;
        String firstName = null;
        String lastName = null;
        while (it.hasNext()) {
            Map.Entry<String, String> optionAndArg = getNextOptionAndArg(it);
            switch (optionAndArg.getKey()) {
                case "--new-username" -> username = optionAndArg.getValue();
                case "--new-first-name" -> firstName = optionAndArg.getValue();
                case "--new-last-name" -> lastName = optionAndArg.getValue();
                case "--new-email" -> email = optionAndArg.getValue();
                case "--session-id" -> sessionId = optionAndArg.getValue();
                default -> throw new InvalidCommandException("Unknown option: " + optionAndArg.getKey());
            }
        }
        checkSession(sessionId);
        editInDb(sessionId, username, email, firstName, lastName, null, null);
        return "successfully edited user";
    }

    private void editInDb(String sessionId, String username, String email, String firstName,
                          String lastName, String oldPassword, String newPassword) throws IOException {
        Path tempFile = Files.createTempFile("tempFile", "txt");
        BufferedReader reader = new BufferedReader(new FileReader(DB_PATH));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile.toFile()));
        String currentLine;
        String userId = getSessionBySessionId(sessionId).getUserId();
        while ((currentLine = reader.readLine()) != null) {
            String[] values = currentLine.split(",");
            if (values[User.ID_POS].equals(userId)) {
                if (oldPassword == null || newPassword == null) {
                    currentLine = ((username != null) ? username : values[User.USERNAME_POS]) + "," + values[User.SALT_POS] + ","
                            + values[User.PASS_POS] + "," + values[User.ID_POS] + "," +
                            ((firstName != null) ? firstName : values[User.FNAME_POS]) + "," +
                            ((lastName != null) ? lastName : values[User.LNAME_POS]) + "," +
                            ((email != null) ? email : values[User.EMAIL_POS]);
                } else if (User.getHashedPassword(oldPassword, values[User.SALT_POS])
                        .equals(values[User.PASS_POS])) {
                    currentLine = (new User(values[User.USERNAME_POS], newPassword, values[User.FNAME_POS],
                            values[User.LNAME_POS], values[User.EMAIL_POS], Boolean.parseBoolean(values[User.ADMIN_POS]))).toString();
                } else {
                    throw new InvalidCommandException("Wrong password");
                }
            }
            writer.write(currentLine + System.lineSeparator());
        }
        writer.close();
        reader.close();
        Files.move(tempFile, Paths.get(DB_PATH), StandardCopyOption.REPLACE_EXISTING);
    }

    private Map.Entry<String, String> getNextOptionAndArg(Iterator<String> it) {
        String option = it.next();
        if (!it.hasNext()) {
            throw new InvalidCommandException("Missing value for option: " + option);
        }
        String arg = it.next();
        if (User.FIELDS.contains(arg)) {
            throw new InvalidCommandException("Invalid " + option.substring(2) + ": " + arg);
        }
        return Map.entry(option, arg);
    }
    /*Collection<User> getUsers() throws IOException {
        ArrayList<User> users;
        Path path = Paths.get(DB_PATH);
        List<String> strings = Files.readAllLines(path);
        for (String string : strings) {
            Iterator<String> it = Arrays.stream(string.split(",")).iterator();
            if (it.hasNext()) {
                if (it.next().equals())
            }
        }
    }*/

    private String logout(Iterator<String> it) {
        String sessionId = null;
        if (it.hasNext()) {
            Map.Entry<String, String> optionAndArg = getNextOptionAndArg(it);
            if (optionAndArg.getKey().equals("--session-id")) {
                sessionId = optionAndArg.getValue();
            } else {
                throw new InvalidCommandException("Unknown option: " + optionAndArg.getKey());
            }
        }
        if (sessionId != null) {
            String finalSessionId = sessionId;
            sessions.removeIf(x -> x.getSessionId().equals(finalSessionId));
            return "successfully logged out";
        }
        throw new InvalidCommandException("Invalid command");
    }

    private String login(Iterator<String> it) throws IOException {
        String sessionId = null;
        String username = null;
        String password = null;

        while (it.hasNext()) {
            Map.Entry<String, String> optionAndArg = getNextOptionAndArg(it);
            switch (optionAndArg.getKey()) {
                case "--username" -> username = optionAndArg.getValue();
                case "--password" -> password = optionAndArg.getValue();
                case "--session-id" -> sessionId = optionAndArg.getValue();
                default -> throw new InvalidCommandException("Unknown option: " + optionAndArg.getKey());
            }
        }
        if (sessionId != null) {
            return loginWithSession(sessionId);
        } else if (username != null && password != null) {
            return loginWithUsernamePassword(username, password);
        }
        throw new InvalidCommandException("invalid input");
    }

    private String loginWithSession(String sessionId) {
        checkSession(sessionId);
        return sessionId;
    }

    private String loginWithUsernamePassword(String username, String password) throws IOException {
        Path path = Paths.get(DB_PATH);
        List<String> strings = Files.readAllLines(path);
        for (String string : strings) {
            Iterator<String> itStr = Arrays.stream(string.split(",")).iterator();
            if (itStr.hasNext()) {
                if (itStr.next().equals(username)) {
                    String salt = itStr.next();
                    String passwordHash = itStr.next();
                    String userId = itStr.next();
                    if (passwordHash.equals(User.getHashedPassword(password, salt))) {
                        Session newSession = new Session(userId);
                        sessions.removeIf(x -> x.getUserId().equals(userId));
                        sessions.add(newSession);
                        return newSession.toString();
                    }
                }
            }
        }
        throw new InvalidCommandException("invalid input");
    }

    private String register(Iterator<String> it) throws IOException {
        User user = new User();
        while (it.hasNext()) {
            Map.Entry<String, String> optionAndArg = getNextOptionAndArg(it);
            switch (optionAndArg.getKey()) {
                case "--username" -> user.setUsername(optionAndArg.getValue());
                case "--password" -> user.setPassword(optionAndArg.getValue());
                case "--first-name" -> user.setFirstName(optionAndArg.getValue());
                case "--last-name" -> user.setLastName(optionAndArg.getValue());
                case "--email" -> user.setEmail(optionAndArg.getValue());
                default -> throw new InvalidCommandException("Unknown option: " + optionAndArg.getKey());
            }
        }

        addToDb(user);
        Session newSession = new Session(user.getUserId());
        sessions.add(newSession);
        return newSession.toString();
    }

    private void addToDb(User user) throws IOException {
        if (user.getUsername() == null || user.getPassword() == null || user.getFirstName() == null
                || user.getLastName() == null || user.getEmail() == null) {
            throw new InvalidCommandException("All fields are required");
        }
        Path path = Paths.get(DB_PATH);
        List<String> strings = Files.readAllLines(path);
        for (String string : strings) {
            if (string.startsWith(user.getUsername() + ",")) {
                throw new UserExistsException("can't add user, user already exists");
            }
        }
        try (FileWriter fileWriter = new FileWriter(DB_PATH, true)) {
            fileWriter.write(user.toString());
        } catch (Exception e) {
            throw new InvalidCommandException("FileWriterProblem", e);
        }
    }

    private void createDbFileIfNotExists() throws IOException {
        Path path = Paths.get(DB_PATH);
        if (Files.notExists(path)) {
            Files.createFile(path);
        }
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            isServerWorking = true;
            while (isServerWorking) {
                handleClientRequests(buffer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to start server", e);
        }
    }

    private void handleClientRequests(ByteBuffer buffer) throws IOException {
        sessions.removeIf(session -> session.getTtl().isBefore(LocalDateTime.now()));
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

    private void processReadableKey(ByteBuffer buffer, SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        String clientInput = getClientInput(clientChannel, buffer);
        System.out.println(clientInput);
        if (clientInput != null) {
            try {
                writeClientOutput(clientChannel, parseCommand(clientInput), buffer);
            } catch (InvalidCommandException e) {
                writeClientOutput(clientChannel, "Invalid input, please try again" + e.getMessage(), buffer);
            } catch (UserExistsException e) {
                writeClientOutput(clientChannel, "Username is taken, please try again" + e.getMessage(), buffer);
            } catch (InvalidSessionException e) {
                writeClientOutput(clientChannel, "Session is invalid, please login/register" + e.getMessage(), buffer);
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