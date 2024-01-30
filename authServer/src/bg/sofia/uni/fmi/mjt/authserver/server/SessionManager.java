package bg.sofia.uni.fmi.mjt.authserver.server;

import bg.sofia.uni.fmi.mjt.authserver.client.Session;
import bg.sofia.uni.fmi.mjt.authserver.exception.InvalidSessionException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

public class SessionManager {
    private final Collection<Session> sessions;

    public SessionManager() {
        sessions = new ArrayList<>();
    }

    public void add(Session session) {
        sessions.add(session);
    }

    public Session getSessionBySessionId(String sessionId) {
        Session session = sessions.stream().filter(x -> x.getSessionId().equals(sessionId)).findFirst().orElse(null);
        if (sessionId == null || session == null) {
            throw new InvalidSessionException("Session is not valid");
        }
        return session;
    }

    public void removeByUserId(String userId) {
        sessions.removeIf(x -> x.getUserId().equals(userId));
    }

    public void removeBySessionId(String sessionId) {
        sessions.removeIf(x -> x.getSessionId().equals(sessionId));
    }

    public void removeByUsername(String username) {
        sessions.removeIf(x -> x.getUsername().equals(username));
    }

    void updateSession(String username, Boolean admin) {
        sessions.forEach(x -> {
            if (x.getUsername().equals(username)) {
                x.setAdmin(admin);
            }
        });
    }

    void cleanSessions() {
        sessions.removeIf(session -> session.getTtl().isBefore(LocalDateTime.now()));
    }
}
