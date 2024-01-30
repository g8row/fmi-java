package bg.sofia.uni.fmi.mjt.authserver.client;

import java.time.LocalDateTime;
import java.util.UUID;

public class Session {
    String sessionId;
    String userId;
    String username;
    LocalDateTime ttl;
    Boolean admin;

    private static final int MINUTES_TTL = 5;

    public String getUserId() {
        return userId;
    }

    public Session(String userId, String username, Boolean admin) {
        this.userId = userId;
        sessionId = UUID.randomUUID().toString();
        ttl = LocalDateTime.now().plusMinutes(MINUTES_TTL);
        this.username = username;
        this.admin = admin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getTtl() {
        return ttl;
    }

    public void setTtl(LocalDateTime ttl) {
        this.ttl = ttl;
    }

    @Override
    public String toString() {
        return "SessionID: " + sessionId;
    }
}
