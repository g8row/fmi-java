package bg.sofia.uni.fmi.mjt.authserver.user;

import bg.sofia.uni.fmi.mjt.authserver.exception.InvalidCommandException;
import bg.sofia.uni.fmi.mjt.authserver.exception.InvalidSessionException;
import bg.sofia.uni.fmi.mjt.authserver.exception.PermissionException;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserExistsException;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.authserver.response.Response;

import java.io.IOException;
import java.util.Map;

/**
 * Provides an interface for managing user-related operations in the authentication server.
 */
public interface UserManagerApi {

    /**
     * Parses the provided command and performs the corresponding action.
     *
     * @param command The user command to be parsed and executed.
     * @param userIp  The IP address of the user.
     * @return A {@link Response} object representing the result of the operation.
     * @throws IOException If an I/O error occurs during command execution.
     */
    Response parseCommand(String command, String userIp) throws IOException;

    /**
     * Registers a new user based on the provided options and arguments.
     *
     * @param optionsAndArgs The options and arguments for user registration.
     * @param userIp         The IP address of the user.
     * @return A {@link Response} object representing the result of the registration operation.
     * @throws IOException            If an I/O error occurs during registration.
     */
    Response register(Map<String, String> optionsAndArgs, String userIp) throws IOException;

    /**
     * Logs in a user based on the provided options and arguments.
     *
     * @param optionsAndArgs The options and arguments for user login.
     * @param userIp         The IP address of the user.
     * @return A {@link Response} object representing the result of the login operation.
     * @throws IOException            If an I/O error occurs during login.
     * @throws InvalidCommandException If the command is invalid.
     */
    Response login(Map<String, String> optionsAndArgs, String userIp) throws IOException, InvalidCommandException;

    /**
     * Logs out a user based on the provided options and arguments.
     *
     * @param optionsAndArgs The options and arguments for user logout.
     * @param userIp         The IP address of the user.
     * @return A {@link Response} object representing the result of the logout operation.
     * @throws IOException            If an I/O error occurs during logout.
     */
    Response logout(Map<String, String> optionsAndArgs, String userIp) throws IOException;

    /**
     * Updates a user's information based on the provided options and arguments.
     *
     * @param optionsAndArgs The options and arguments for updating the user.
     * @param userIp         The IP address of the user.
     * @return A {@link Response} object representing the result of the update operation.
     * @throws IOException            If an I/O error occurs during the update.
     */
    Response updateUser(Map<String, String> optionsAndArgs, String userIp) throws IOException;

    /**
     * Deletes a user based on the provided options and arguments.
     *
     * @param optionsAndArgs The options and arguments for deleting the user.
     * @param userIp         The IP address of the user.
     * @return A {@link Response} object representing the result of the deletion operation.
     * @throws IOException            If an I/O error occurs during deletion.
     */
    Response deleteUser(Map<String, String> optionsAndArgs, String userIp) throws IOException;

    /**
     * Resets a user's password based on the provided options and arguments.
     *
     * @param optionsAndArgs The options and arguments for resetting the password.
     * @param userIp         The IP address of the user.
     * @return A {@link Response} object representing the result of the password reset operation.
     * @throws IOException            If an I/O error occurs during password reset.
     */
    Response resetPassword(Map<String, String> optionsAndArgs, String userIp) throws IOException;

    /**
     * Adds an admin user based on the provided options and arguments.
     *
     * @param optionsAndArgs The options and arguments for adding an admin user.
     * @param userIp         The IP address of the user.
     * @return A {@link Response} object representing the result of the admin addition operation.
     * @throws IOException            If an I/O error occurs during admin addition.
     */
    Response addAdminUser(Map<String, String> optionsAndArgs, String userIp) throws IOException;

    /**
     * Removes an admin user based on the provided options and arguments.
     *
     * @param optionsAndArgs The options and arguments for removing an admin user.
     * @param userIp         The IP address of the user.
     * @return A {@link Response} object representing the result of the admin removal operation.
     * @throws IOException            If an I/O error occurs during admin removal.
     */
    Response removeAdminUser(Map<String, String> optionsAndArgs, String userIp) throws IOException;

    /**
     * Logs in a user with a session ID.
     *
     * @param sessionId The session ID of the user.
     * @param userIp    The IP address of the user.
     * @return A {@link Response} object representing the result of the login operation.
     * @throws IOException            If an I/O error occurs during login.
     */
    Response loginWithSession(String sessionId, String userIp) throws IOException;

    /**
     * Logs in a user with a username and password.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @param userIp   The IP address of the user.
     * @return A {@link Response} object representing the result of the login operation.
     * @throws IOException            If an I/O error occurs during login.
     */
    Response loginWithUsernamePassword(String username, String password, String userIp) throws IOException;
}
