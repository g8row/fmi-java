package bg.sofia.uni.fmi.mjt.authserver.client;

import java.time.LocalDateTime;
import java.util.UUID;

public class Session {
    String sessionId;
    String userId;
    LocalDateTime ttl;

    private static final int MINUTES_TTL = 5;

    public String getUserId() {
        return userId;
    }

    public Session(String userId) {
        this.userId = userId;
        sessionId = UUID.randomUUID().toString();
        ttl = LocalDateTime.now().plusMinutes(MINUTES_TTL);
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
