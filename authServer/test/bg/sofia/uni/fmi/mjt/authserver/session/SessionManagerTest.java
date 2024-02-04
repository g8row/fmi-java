package bg.sofia.uni.fmi.mjt.authserver.session;

import static org.junit.jupiter.api.Assertions.*;

import bg.sofia.uni.fmi.mjt.authserver.exception.InvalidSessionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager();
    }

    @Test
    void testAddSession() {
        // Arrange
        Session session = new Session("userId", "testUser", false);

        // Act
        sessionManager.add(session);

        // Assert
        assertSame(sessionManager.getSessionBySessionId(session.getSessionId()), session);
    }

    @Test
    void testGetSessionBySessionId() {
        // Arrange
        Session session = new Session("userId", "testUser", false);
        sessionManager.add(session);

        // Act
        Session retrievedSession = sessionManager.getSessionBySessionId(session.getSessionId());

        // Assert
        assertEquals(session, retrievedSession);
    }

    @Test
    void testRemoveByUserId() {
        // Arrange
        Session session = new Session("userId", "testUser", false);
        sessionManager.add(session);

        // Act
        sessionManager.removeByUserId("userId");

        // Assert
        assertThrows(InvalidSessionException.class,()-> sessionManager.getSessionBySessionId(session.getSessionId()));
    }

    @Test
    void testCleanSessions() {
        // Arrange
        Session validSession = new Session("validUserId", "validUser", false);
        Session expiredSession = new Session("expiredUserId", "expiredUser", false);
        expiredSession.setTtl(LocalDateTime.now().minusMinutes(1)); // Set TTL to be in the past
        validSession.setTtl(LocalDateTime.now().plusMinutes(1)); // Set TTL to be in the past

        sessionManager.add(validSession);
        sessionManager.add(expiredSession);

        // Act
        sessionManager.cleanSessions();

        // Assert
        assertNotNull(sessionManager.getSessionBySessionId(validSession.getSessionId()));
        assertThrows(InvalidSessionException.class,()-> sessionManager.getSessionBySessionId(expiredSession.getSessionId()));
    }

    @Test
    void testRemoveBySessionId() {
        // Arrange
        Session session = new Session("testSessionId", "testUser", false);
        sessionManager.add(session);

        // Act
        sessionManager.removeByUserId("testSessionId");

        // Assert
        assertTrue(sessionManager.getSessions().isEmpty());
    }

    @Test
    void testRemoveByUsername() {
        // Arrange
        Session session = new Session("testSessionId", "testUser", false);
        sessionManager.add(session);

        // Act
        sessionManager.removeByUsername("testUser");

        // Assert
        assertTrue(sessionManager.getSessions().isEmpty());
    }

    @Test
    void testUpdateSessionAdmin() {
        // Arrange
        Session session = new Session("testSessionId", "testUser", false);
        sessionManager.add(session);

        // Act
        sessionManager.updateSessionAdmin("testUser", true);

        // Assert
        assertTrue(sessionManager.getSessions().iterator().next().getAdmin());
    }

    @Test
    void testUpdateSessionUsername() {
        // Arrange
        Session session = new Session("testSessionId", "oldUsername", false);
        sessionManager.add(session);

        // Act
        sessionManager.updateSessionUsername("oldUsername", "newUsername");

        // Assert
        assertEquals("newUsername", sessionManager.getSessions().iterator().next().getUsername());
    }

    // Add more test cases for other methods
}
