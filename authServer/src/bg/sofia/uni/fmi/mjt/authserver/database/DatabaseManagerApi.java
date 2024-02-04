package bg.sofia.uni.fmi.mjt.authserver.database;

import bg.sofia.uni.fmi.mjt.authserver.exception.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.authserver.user.User;

/**
 * An interface for managing user data in the authentication server database.
 */
public interface DatabaseManagerApi {

    /**
     * Edits user information in the database.
     *
     * @param userId     The ID of the user to be edited.
     * @param username   The new username (or null if not edited).
     * @param email      The new email (or null if not edited).
     * @param firstName  The new first name (or null if not edited).
     * @param lastName   The new last name (or null if not edited).
     * @param admin      The new admin status (or null if not edited).
     */
    void editUser(String userId, String username, String email, String firstName, String lastName, Boolean admin);

    /**
     * Edits the password of a user in the database.
     *
     * @param userId      The ID of the user whose password is to be edited.
     * @param oldPassword The old password to be verified before editing.
     * @param newPassword The new password to be set.
     */
    void editPassword(String userId, String oldPassword, String newPassword);

    /**
     * Adds a new user to the database.
     *
     * @param user The user to be added.
     */
    void addUser(User user);

    /**
     * Deletes a user from the database.
     *
     * @param userId The ID of the user to be deleted.
     */
    void deleteUser(String userId);

    /**
     * Finds and retrieves a user from the database based on the username.
     *
     * @param username The username of the user to search for.
     * @return The {@link User} object associated with the provided username.
     * @throws UserNotFoundException If the user with the given username does not exist in the database.
     */
    User findUserInDatabase(String username);

    /**
     * Checks if the current admin user is the last admin in the database.
     *
     * @return True if the current admin is the last admin, false otherwise.
     */
    boolean isLastAdmin();
}
