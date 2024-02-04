package bg.sofia.uni.fmi.mjt.authserver.ban;

/**
 * An interface for managing bans based on IP addresses.
 */
public interface BanManagerApi {

    /**
     * Records a failed login attempt for the specified IP address and may apply a ban if the maximum attempts are reached.
     *
     * @param userIp The IP address associated with the login attempt.
     */
    void addFailedAttempt(String userIp);

    /**
     * Bans the specified IP address.
     *
     * @param userIp The IP address to be banned.
     */
    void banUser(String userIp);

    /**
     * Clears the login attempts for the specified IP address.
     *
     * @param userIp The IP address for which login attempts should be cleared.
     */
    void clearUserAttempts(String userIp);

    /**
     * Checks if the specified IP address is currently banned.
     *
     * @param userIp The IP address to check.
     * @return {@code true} if the IP address is banned, {@code false} otherwise.
     */
    boolean checkBanned(String userIp);
}
