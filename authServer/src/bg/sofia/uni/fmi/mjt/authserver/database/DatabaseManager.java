package bg.sofia.uni.fmi.mjt.authserver.database;

import bg.sofia.uni.fmi.mjt.authserver.exception.DatabaseException;
import bg.sofia.uni.fmi.mjt.authserver.exception.InvalidCommandException;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.authserver.user.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class DatabaseManager implements DatabaseManagerApi {
    private final String dbPath;
    private final Map<String, User> users;

    public DatabaseManager(String dbPath) throws IOException {
        createDbFileIfNotExists(dbPath);
        try (FileReader fileReader = new FileReader(dbPath)) {
            users = loadFromDb(fileReader);
        }
        this.dbPath = dbPath;
    }

    private Map<String, User> loadFromDb(Reader reader) {
        Map<String, User> newUsers = new LinkedHashMap<>();
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                String[] values = currentLine.split(",");
                User user = new User(values[User.USERNAME_POS], values[User.SALT_POS], values[User.PASS_POS],
                        values[User.ID_POS], values[User.FNAME_POS], values[User.LNAME_POS], values[User.EMAIL_POS],
                        Boolean.parseBoolean(values[User.ADMIN_POS]));
                newUsers.put(values[User.ID_POS], user);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return newUsers;
    }

    public void editPassword(String userId, String oldPassword, String newPassword) {
        User userToEdit = users.get(userId);
        if (userToEdit.getPasswordHash().equals(User.getHashedPassword(oldPassword, userToEdit.getSalt()))) {
            userToEdit.setPasswordHash(User.getHashedPassword(newPassword, userToEdit.getSalt()));
        }
        writeToDb();
    }

    @Override
    public void editUser(String userId, String username,
                         String firstName, String lastName, String email, Boolean admin) {
        User user = users.get(userId);
        if (username != null) {
            if (users.values().stream().anyMatch(x -> x.getUsername().equals(username))) {
                throw new InvalidCommandException("Username taken");
            }
            user.setUsername(username);
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (firstName != null) {
            user.setFirstName(firstName);
        }
        if (lastName != null) {
            user.setLastName(lastName);
        }
        if (admin != null) {
            user.setAdmin(admin);
        }
        writeToDb();
    }

    public void addUser(User user) {
        users.put(user.getUserId(), user);
        writeToDb();
    }

    public void deleteUser(String userId) {
        if (!users.containsKey(userId)) {
            throw new DatabaseException("no user with this userId");
        }
        users.remove(userId);
        writeToDb();
    }

    private void writeToDb() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(dbPath))) {
            String db = users.values().stream().collect(StringBuilder::new,
                    (x, y) -> x.append(y.toString()), (y, x) -> x.append(y.toString())).toString();
            if (!db.isEmpty()) {
                bufferedWriter.write(db);
            }
        } catch (IOException e) {
            throw new DatabaseException("Problem with writing to database", e);
        }
    }

    private void createDbFileIfNotExists(String dbPath) throws IOException {
        Path path = Paths.get(dbPath);
        if (Files.notExists(path)) {
            Files.createFile(path);
        }
    }

    public User findUserInDatabase(String username) {
        Optional<User> user = users.values().stream().filter(x -> x.getUsername().equals(username)).findAny();
        if (user.isEmpty()) {
            throw new UserNotFoundException("User does not exist");
        }
        return user.get();
    }

    public boolean isLastAdmin() {
        return users.values().stream().filter(User::isAdmin)
                .limit(2)
                .count() < 2;
    }
}
