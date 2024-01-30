package bg.sofia.uni.fmi.mjt.authserver.database;

import bg.sofia.uni.fmi.mjt.authserver.client.Session;
import bg.sofia.uni.fmi.mjt.authserver.exception.InvalidCommandException;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserExistsException;
import bg.sofia.uni.fmi.mjt.authserver.log.AuditLog;
import bg.sofia.uni.fmi.mjt.authserver.server.Command;
import bg.sofia.uni.fmi.mjt.authserver.server.SessionManager;
import bg.sofia.uni.fmi.mjt.authserver.user.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

public class DatabaseManager {
    private final String dbPath;
    private final AuditLog auditLog;
    private final SessionManager sessionManager;

    public DatabaseManager(String dbPath, AuditLog auditLog, SessionManager sessionManager) throws IOException {
        this.dbPath = dbPath;
        this.auditLog = auditLog;
        this.sessionManager = sessionManager;
        createDbFileIfNotExists();
    }

    public void updatePasswordInDb(String sessionId, String username,
                                   String oldPassword, String newPassword, String userIp) throws IOException {
        Session session = sessionManager.getSessionBySessionId(sessionId);
        auditLog.logCommandStart(LocalDateTime.now(), Command.RESET_PASSWORD,
                "database edit", session.getUserId(), userIp,
                "reset password of" + username);
        try {
            editInDb(false, null, null, null, null, oldPassword, newPassword, null,
                    User.ID_POS, session.getUserId());
        } catch (Exception e) {
            auditLog.logCommandEnd(LocalDateTime.now(), Command.DELETE_USER,
                    "database edit", session.getUserId(), userIp,
                    "fail. " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void updateUserInDb(String sessionId, String username,
                        String email, String firstName, String lastName, String userIp) throws IOException {
        Session session = sessionManager.getSessionBySessionId(sessionId);
        auditLog.logCommandStart(LocalDateTime.now(), Command.RESET_PASSWORD,
                "database edit", session.getUserId(), userIp,
                "edit user of" + username);
        try {
            editInDb(false, username, email, firstName, lastName, null, null, null,
                    User.ID_POS, session.getUserId());
        } catch (Exception e) {
            auditLog.logCommandEnd(LocalDateTime.now(), Command.DELETE_USER,
                    "database edit", session.getUserId(), userIp,
                    "fail. " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void editInDb(boolean delete, String username, String email, String firstName, String lastName,
                          String oldPassword, String newPassword, Boolean admin, int searchByPos, String searchWord)
            throws IOException {
        Path tempFile = Files.createTempFile("tempFile", "txt");
        BufferedReader reader = new BufferedReader(new FileReader(dbPath));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile.toFile()));
        String currentLine;
        while ((currentLine = reader.readLine()) != null) {
            String[] values = currentLine.split(",");
            currentLine += System.lineSeparator();
            if (values[searchByPos].equals(searchWord)) {
                if (!delete) {
                    if (oldPassword == null || newPassword == null) {
                        currentLine = editedLine(username, email, firstName, lastName, admin, values);
                    } else if (User.getHashedPassword(oldPassword, values[User.SALT_POS])
                            .equals(values[User.PASS_POS])) {
                        currentLine = (new User(values[User.USERNAME_POS], newPassword, values[User.FNAME_POS],
                                values[User.LNAME_POS], values[User.EMAIL_POS],
                                Boolean.parseBoolean(values[User.ADMIN_POS]))) + System.lineSeparator();
                    } else {
                        throw new InvalidCommandException("Wrong password");
                    }
                } else {
                    currentLine = "";
                }
            }
            writer.write(currentLine);
        }
        writer.close();
        reader.close();
        Files.move(tempFile, Paths.get(dbPath), StandardCopyOption.REPLACE_EXISTING);
    }

    private String editedLine(String username, String email, String firstName, String lastName,
                              Boolean admin, String[] values) {
        return ((username != null) ? username : values[User.USERNAME_POS]) + "," +
                values[User.SALT_POS] + "," + values[User.PASS_POS] + "," + values[User.ID_POS] + "," +
                ((firstName != null) ? firstName : values[User.FNAME_POS]) + "," +
                ((lastName != null) ? lastName : values[User.LNAME_POS]) + "," +
                ((email != null) ? email : values[User.EMAIL_POS]) + "," +
                ((admin != null) ? admin : values[User.ADMIN_POS]) + System.lineSeparator();
    }

    public void addToDb(User user) throws IOException {
        if (user.getUsername() == null || user.getPassword() == null || user.getFirstName() == null
                || user.getLastName() == null || user.getEmail() == null) {
            throw new InvalidCommandException("All fields are required");
        }
        if (user.getUsername().contains(",") || user.getPassword().contains(",") || user.getFirstName().contains(",")
                || user.getLastName().contains(",") || user.getEmail().contains(",")) {
            throw new InvalidCommandException(", is forbidden");
        }
        if (user.getUsername().contains("\n") || user.getPassword().contains("\n") || user.getFirstName().contains("\n")
                || user.getLastName().contains("\n") || user.getEmail().contains("\n")) {
            throw new InvalidCommandException("newLine is forbidden");
        }
        Path path = Paths.get(dbPath);
        List<String> strings = Files.readAllLines(path);
        for (String string : strings) {
            if (string.startsWith(user.getUsername() + ",")) {
                throw new UserExistsException("can't add user, user already exists");
            }
        }
        try (FileWriter fileWriter = new FileWriter(dbPath, true)) {
            fileWriter.write(user.toString());
        } catch (Exception e) {
            throw new InvalidCommandException("FileWriterProblem", e);
        }
    }

    private void createDbFileIfNotExists() throws IOException {
        Path path = Paths.get(dbPath);
        if (Files.notExists(path)) {
            Files.createFile(path);
        }
    }

    public String findUserInDatabase(String username) throws IOException {
        String user;
        try (Stream<String> stream = Files.lines(Paths.get(dbPath))) {
            user = stream.filter(x -> x.split(",")[User.USERNAME_POS].equals(username))
                    .findFirst()
                    .orElse(null);
        }
        return user;
    }

    public boolean isLastAdmin() throws IOException {
        long n;
        try (Stream<String> stream = Files.lines(Paths.get(dbPath))) {
            n = stream.filter(x -> Boolean.parseBoolean(x.split(",")[User.ADMIN_POS]))
                    .limit(2)
                    .count();
        }
        return n < 2;
    }
}
