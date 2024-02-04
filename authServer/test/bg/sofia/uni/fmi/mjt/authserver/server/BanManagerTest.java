package bg.sofia.uni.fmi.mjt.authserver.server;

import bg.sofia.uni.fmi.mjt.authserver.ban.BanManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BanManagerTest {

    private BanManager banManager;

    @BeforeEach
    void setUp() {
        banManager = new BanManager(5, TimeUnit.MILLISECONDS.toMillis(10));
    }

    @Test
    void testAddFailedAttempt() {
        String userIp = "1.1.1.1";


        for (int i = 0; i < 4; i++) {
            banManager.addFailedAttempt(userIp);
            assertFalse(banManager.checkBanned(userIp));
        }


        banManager.addFailedAttempt(userIp);
        assertTrue(banManager.checkBanned(userIp));
    }

    @Test
    void testBanUser() {
        String userIp = "2.2.2.2";
        banManager.banUser(userIp);

        assertTrue(banManager.checkBanned(userIp));
    }

    @Test
    void testClearUserAttempts() {
        String userIp = "3.3.3.3";


        for (int i = 0; i < 3; i++) {
            banManager.addFailedAttempt(userIp);
        }


        banManager.clearUserAttempts(userIp);


        banManager.addFailedAttempt(userIp);
        assertFalse(banManager.checkBanned(userIp));
    }

    @Test
    void testCheckBannedNotBanned() {
        String userIp = "4.4.4.4";

        assertFalse(banManager.checkBanned(userIp));
    }

    @Test
    void testCheckBannedAlreadyBanned() {
        String userIp = "5.5.5.5";
        banManager.banUser(userIp);

        assertTrue(banManager.checkBanned(userIp));
    }

    @Test
    void testCheckBannedExpiredBan() throws InterruptedException {
        String userIp = "6.6.6.6";
        banManager.banUser(userIp);


        Thread.sleep(TimeUnit.MILLISECONDS.toMillis(11));

        assertFalse(banManager.checkBanned(userIp));
    }
}
