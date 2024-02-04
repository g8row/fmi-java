package bg.sofia.uni.fmi.mjt.authserver.session;

/**
 * An interface for managing user sessions in the authentication server.
 */
public interface SessionManagerApi {

    /**
     * Adds a new session to the session manager.
     *
     * @param session The session to be added.
     */
    void add(Session session);

    /**
     * Retrieves a session based on the provided session ID.
     *
     * @param sessionId The session ID to search for.
     * @return The {@link Session} object associated with the provided session ID, or null if not found.
     */
    Session getSessionBySessionId(String sessionId);

    /**
     * Removes sessions associated with the provided user ID.
     *
     * @param userId The user ID whose sessions should be removed.
     */
    void removeByUserId(String userId);

    /**
     * Removes a session based on the provided session ID.
     *
     * @param sessionId The session ID to be removed.
     */
    void removeBySessionId(String sessionId);

    /**
     * Removes sessions associated with the provided username.
     *
     * @param username The username whose sessions should be removed.
     */
    void removeByUsername(String username);

    /**
     * Updates the admin status of a user's session.
     *
     * @param username The username of the user whose session admin status should be updated.
     * @param admin    The new admin status to be set.
     */
    void updateSessionAdmin(String username, Boolean admin);

    /**
     * Cleans up expired sessions and removes them from the session manager.
     */
    void cleanSessions();

    /**
     * Updates the username associated with a user's session.
     *
     * @param username    The current username of the user.
     * @param newUsername The new username to be associated with the user's session.
     */
    void updateSessionUsername(String username, String newUsername);
}
