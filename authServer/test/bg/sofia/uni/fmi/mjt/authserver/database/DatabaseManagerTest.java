package bg.sofia.uni.fmi.mjt.authserver.database;

import bg.sofia.uni.fmi.mjt.authserver.exception.DatabaseException;
import bg.sofia.uni.fmi.mjt.authserver.exception.InvalidCommandException;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserExistsException;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.authserver.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class DatabaseManagerTest {

    private DatabaseManager databaseManager;

    @TempDir
    Path tempDir;

    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = new File(tempDir.toFile(), "tempDb.csv");
        tempFile.createNewFile();
    }

    @AfterEach
    void tearDown() {
        tempFile.delete();
    }

    @Test
    void testEditPassword() throws IOException {

        String oldPassword = "oldPass";
        String newPassword = "newPass";


        User newUser = new User("newUser", "oldPass", "John", "Doe", "john@example.com", false);

        String userId = newUser.getUserId();

        databaseManager = new DatabaseManager(tempFile.getAbsolutePath());
        databaseManager.addUser(newUser);
        databaseManager.editPassword(userId, oldPassword, newPassword);


        try (Scanner scanner = new Scanner(tempFile)) {
            String[] values = scanner.nextLine().split(",");
            assertEquals(values[User.USERNAME_POS], newUser.getUsername());
            assertEquals(values[User.FNAME_POS], newUser.getFirstName());
            assertEquals(values[User.LNAME_POS], newUser.getLastName());
            assertEquals(values[User.EMAIL_POS], newUser.getEmail());
            assertEquals(Boolean.parseBoolean(values[User.ADMIN_POS]), newUser.isAdmin());
            assertEquals(User.getHashedPassword(newPassword, values[User.SALT_POS]), values[User.PASS_POS]);
            assertEquals(values[User.ID_POS], newUser.getUserId());
        }
    }

    @Test
    void testEditUser() throws IOException {

        String username = "newUsername";
        String firstName = "John1";
        String lastName = "Doe1";
        String email = "john1@example.com";
        Boolean admin = true;

        User newUser = new User("newUser", "pass123", "John", "Doe", "john@example.com", false);

        String userId = newUser.getUserId();

        databaseManager = new DatabaseManager(tempFile.getAbsolutePath());
        databaseManager.addUser(newUser);
        databaseManager.editUser(userId, username, firstName, lastName, email, admin);


        try (Scanner scanner = new Scanner(tempFile)) {
            String[] values = scanner.nextLine().split(",");
            assertEquals(values[User.USERNAME_POS], username);
            assertEquals(values[User.FNAME_POS], firstName);
            assertEquals(values[User.LNAME_POS], lastName);
            assertEquals(values[User.EMAIL_POS], email);
            assertEquals(Boolean.parseBoolean(values[User.ADMIN_POS]), newUser.isAdmin());
            assertEquals(User.getHashedPassword("pass123", values[User.SALT_POS]), values[User.PASS_POS]);
            assertEquals(values[User.ID_POS], userId);
        }
    }

    @Test
    void testAddUser() throws IOException {

        User newUser = new User("newUser", "pass123", "John", "Doe", "john@example.com", true);


        databaseManager = new DatabaseManager(tempFile.getAbsolutePath());
        databaseManager.addUser(newUser);


        try (Scanner scanner = new Scanner(tempFile)) {
            String[] values = scanner.nextLine().split(",");
            assertEquals(values[User.USERNAME_POS], newUser.getUsername());
            assertEquals(values[User.FNAME_POS], newUser.getFirstName());
            assertEquals(values[User.LNAME_POS], newUser.getLastName());
            assertEquals(values[User.EMAIL_POS], newUser.getEmail());
            assertEquals(Boolean.parseBoolean(values[User.ADMIN_POS]), newUser.isAdmin());
            assertEquals(User.getHashedPassword("pass123", values[User.SALT_POS]), values[User.PASS_POS]);
            assertEquals(values[User.ID_POS], newUser.getUserId());
        }
    }

    @Test
    void testDeleteUser() throws IOException {



        User newUser = new User("newUser", "pass123", "John", "Doe", "john@example.com", false);
        String userIdToDelete = newUser.getUserId();

        databaseManager = new DatabaseManager(tempFile.getAbsolutePath());
        databaseManager.addUser(newUser);
        databaseManager.deleteUser(userIdToDelete);


        try (Scanner scanner = new Scanner(tempFile)) {
            assertThrows(NoSuchElementException.class, scanner::nextLine);
        }
    }

    @Test
    void testEditUserUsernameTaken() throws IOException {

        User existingUser1 = new User("existingUser1", "pass123", "John", "Doe", "john@example.com", false);
        User existingUser2 = new User("existingUser2", "pass123", "John", "Doe", "john@example.com", false);
        String userIdToEdit = existingUser1.getUserId();


        databaseManager = new DatabaseManager(tempFile.getAbsolutePath());
        databaseManager.addUser(existingUser1);
        databaseManager.addUser(existingUser2);


        try (Scanner scanner = new Scanner(tempFile)) {

            String[] values = scanner.nextLine().split(",");
            assertEquals(values[User.USERNAME_POS], existingUser1.getUsername());
            assertEquals(values[User.FNAME_POS], existingUser1.getFirstName());
            assertEquals(values[User.LNAME_POS], existingUser1.getLastName());
            assertEquals(values[User.EMAIL_POS], existingUser1.getEmail());
            assertEquals(Boolean.parseBoolean(values[User.ADMIN_POS]), existingUser1.isAdmin());
            assertEquals(User.getHashedPassword("pass123", values[User.SALT_POS]), values[User.PASS_POS]);
            assertEquals(values[User.ID_POS], existingUser1.getUserId());

            values = scanner.nextLine().split(",");
            assertEquals(values[User.USERNAME_POS], existingUser2.getUsername());
            assertEquals(values[User.FNAME_POS], existingUser2.getFirstName());
            assertEquals(values[User.LNAME_POS], existingUser2.getLastName());
            assertEquals(values[User.EMAIL_POS], existingUser2.getEmail());
            assertEquals(Boolean.parseBoolean(values[User.ADMIN_POS]), existingUser2.isAdmin());
            assertEquals(User.getHashedPassword("pass123", values[User.SALT_POS]), values[User.PASS_POS]);
            assertEquals(values[User.ID_POS], existingUser2.getUserId());


            assertThrows(InvalidCommandException.class, () ->
                    databaseManager.editUser(userIdToEdit, existingUser2.getUsername(),
                            "NewJohn", "NewDoe", "newjohn@example.com", true));
        }
    }

    @Test
    void testIsLastAdminTrue() throws IOException {

        User adminUser = new User("admin", "pass123", "Admin", "User", "admin@example.com", true);


        databaseManager = new DatabaseManager(tempFile.getAbsolutePath());
        databaseManager.addUser(adminUser);


        assertEquals(true, databaseManager.isLastAdmin());
    }

    @Test
    void testIsLastAdminFalse() throws IOException {

        User adminUser1 = new User("admin1", "pass123", "Admin1", "User", "admin1@example.com", true);
        User adminUser2 = new User("admin2", "pass123", "Admin2", "User", "admin2@example.com", true);


        databaseManager = new DatabaseManager(tempFile.getAbsolutePath());
        databaseManager.addUser(adminUser1);
        databaseManager.addUser(adminUser2);


        assertEquals(false, databaseManager.isLastAdmin());
    }

    @Test
    void testLoadFromDb() throws IOException {

        FileWriter fileWriter = new FileWriter(tempFile);
        fileWriter.write("tf,_�њ���\u000Bz!�+儛�,ad5cdda8561cfaed4104cb1298d973e25b800521f93aa1ca201103732aa73c25cf41ad433627ecc696b20c93458ed64adb7950cca0161b965c2888876f0a973d,f1a270e6-5276-499b-89d6-adef2d5bc08d,tf,tf,tf,false\n");
        fileWriter.close();


        DatabaseManager tempDbManager = new DatabaseManager(tempFile.getAbsolutePath());


        databaseManager = new DatabaseManager(tempFile.getAbsolutePath());


        try (Scanner scanner = new Scanner(tempFile)) {
            String[] values = scanner.nextLine().split(",");
            assertEquals(values[User.USERNAME_POS], "tf");
            assertEquals(values[User.FNAME_POS], "tf");
            assertEquals(values[User.LNAME_POS], "tf");
            assertEquals(values[User.EMAIL_POS], "tf");
            assertEquals(Boolean.parseBoolean(values[User.ADMIN_POS]), false);
            assertEquals(User.getHashedPassword("tf2", values[User.SALT_POS]), values[User.PASS_POS]);
            assertEquals(values[User.ID_POS], "f1a270e6-5276-499b-89d6-adef2d5bc08d");
        }
    }

    @Test
    void testDeleteUserNonExistingUser() throws IOException {

        User newUser = new User("newUser", "pass123", "John", "Doe", "john@example.com", false);
        String userIdToDelete = newUser.getUserId();


        databaseManager = new DatabaseManager(tempFile.getAbsolutePath());


        assertThrows(DatabaseException.class, () -> databaseManager.deleteUser(userIdToDelete));
    }

    @Test
    void testWriteToDbIOException() throws IOException {

        User newUser = new User("newUser", "pass123", "John", "Doe", "john@example.com", false);


        databaseManager = new DatabaseManager(tempFile.getAbsolutePath());
        databaseManager.addUser(newUser);


        //assertThrows(DatabaseException.class, () -> databaseManager.writeToDb());
    }

    @Test
    void testCreateFileIfNotExists() throws IOException {

        File nonExistingFile = new File(tempDir.toFile(), "nonExistingFile.csv");
        nonExistingFile.delete();  // Ensure the file does not exist


        databaseManager = new DatabaseManager(nonExistingFile.getAbsolutePath());


        assertTrue(nonExistingFile.exists());
    }

    @Test
    void testFindUserInDatabaseUserFound() throws IOException {

        User adminUser1 = new User("admin1", "pass123", "Admin1", "User", "admin1@example.com", true);


        databaseManager = new DatabaseManager(tempFile.getAbsolutePath());
        databaseManager.addUser(adminUser1);


        assertTrue(databaseManager.findUserInDatabase("admin1") != null);
    }

    @Test
    void testFindUserInDatabaseUserNotFound() throws IOException {


        databaseManager = new DatabaseManager(tempFile.getAbsolutePath());


        assertThrows(UserNotFoundException.class, ()->databaseManager.findUserInDatabase("admin1"));
    }


}
