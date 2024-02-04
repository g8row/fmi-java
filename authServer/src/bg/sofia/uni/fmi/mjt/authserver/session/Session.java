package bg.sofia.uni.fmi.mjt.authserver.session;

import java.time.LocalDateTime;
import java.util.UUID;

public class Session {
    String sessionId;
    String userId;
    String username;
    LocalDateTime ttl;
    Boolean admin;

    private static final int MINUTES_TTL = 1;

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

    public String getUserId() {
        return userId;
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

    public LocalDateTime getTtl() {
        return ttl;
    }

    public void setTtl(LocalDateTime ttl) {
        this.ttl = ttl;
    }

    public String toString() {
        return "SessionID: " + sessionId;
    }

}
