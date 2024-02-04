package bg.sofia.uni.fmi.mjt.authserver.ban;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class BanManager implements BanManagerApi {
    private final HashMap<String, LocalDateTime> bannedIps;
    private final HashMap<String, Integer> soonToBan;
    private final int maxAttempts;
    private final long banDuration;

    public BanManager(int maxAttempts, long banDuration) {
        bannedIps = new HashMap<>();
        soonToBan = new HashMap<>();
        this.maxAttempts = maxAttempts;
        this.banDuration = banDuration;
    }

    public void addFailedAttempt(String userIp) {
        if (soonToBan.containsKey(userIp)) {
            soonToBan.put(userIp, soonToBan.get(userIp) + 1);
        } else {
            soonToBan.put(userIp, 1);
        }
        if (soonToBan.get(userIp) >= maxAttempts) {
            banUser(userIp);
        }
    }

    public void banUser(String userIp) {
        bannedIps.put(userIp, LocalDateTime.now().plus(banDuration, TimeUnit.MILLISECONDS.toChronoUnit()));
    }

    public void clearUserAttempts(String userIp) {
        soonToBan.remove(userIp);
    }

    public boolean checkBanned(String userIp) {
        if (bannedIps.containsKey(userIp)) {
            if (bannedIps.get(userIp).isBefore(LocalDateTime.now())) {
                bannedIps.remove(userIp);
            } else {
                return true;
            }
        }
        return false;
    }
}
