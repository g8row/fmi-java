package bg.sofia.uni.fmi.mjt.authserver.log;

import bg.sofia.uni.fmi.mjt.authserver.server.Command;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * An interface for logging audit-related events.
 */
public interface AuditLogApi {

    /**
     * Logs a successful login event.
     *
     * @param timestamp The timestamp of the event.
     * @param user      The username associated with the login.
     * @param userIp    The IP address of the user.
     * @throws IOException If an I/O error occurs while writing to the log.
     */
    void logLogin(LocalDateTime timestamp, String user, String userIp) throws IOException;

    /**
     * Logs a logout event.
     *
     * @param timestamp The timestamp of the event.
     * @param user      The username associated with the logout.
     * @param userIp    The IP address of the user.
     * @throws IOException If an I/O error occurs while writing to the log.
     */
    void logLogout(LocalDateTime timestamp, String user, String userIp) throws IOException;

    /**
     * Logs an unsuccessful login attempt.
     *
     * @param timestamp The timestamp of the event.
     * @param user      The username associated with the unsuccessful login attempt.
     * @param userIp    The IP address of the user.
     * @throws IOException If an I/O error occurs while writing to the log.
     */
    void logUnsuccessfulLogin(LocalDateTime timestamp, String user, String userIp) throws IOException;

    /**
     * Logs the start of a command execution.
     *
     * @param timestamp The timestamp of the event.
     * @param command   The type of command being executed.
     * @param type      The type of command (e.g., database edit).
     * @param user      The username associated with the command.
     * @param userIp    The IP address of the user.
     * @param changes   The changes made by the command.
     * @throws IOException If an I/O error occurs while writing to the log.
     */
    void logCommandStart(LocalDateTime timestamp, Command command, String type,
                         String user, String userIp, String changes) throws IOException;

    /**
     * Logs the end of a command execution.
     *
     * @param timestamp The timestamp of the event.
     * @param command   The type of command being executed.
     * @param type      The type of command (e.g., database edit).
     * @param user      The username associated with the command.
     * @param userIp    The IP address of the user.
     * @param result    The result of the command execution.
     * @throws IOException If an I/O error occurs while writing to the log.
     */
    void logCommandEnd(LocalDateTime timestamp, Command command, String type,
                       String user, String userIp, String result) throws IOException;

    /**
     * Logs an error event.
     *
     * @param text The description of the error.
     * @param e    The exception associated with the error.
     * @throws IOException If an I/O error occurs while writing to the log.
     */
    void logError(String text, Exception e) throws IOException;
}
