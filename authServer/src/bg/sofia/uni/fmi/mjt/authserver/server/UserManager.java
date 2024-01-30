package bg.sofia.uni.fmi.mjt.authserver.server;

import bg.sofia.uni.fmi.mjt.authserver.client.Session;
import bg.sofia.uni.fmi.mjt.authserver.database.DatabaseManager;
import bg.sofia.uni.fmi.mjt.authserver.exception.InvalidCommandException;
import bg.sofia.uni.fmi.mjt.authserver.exception.InvalidLoginException;
import bg.sofia.uni.fmi.mjt.authserver.exception.PermissionException;
import bg.sofia.uni.fmi.mjt.authserver.exception.TemporaryLockException;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserNotExistException;
import bg.sofia.uni.fmi.mjt.authserver.log.AuditLog;
import bg.sofia.uni.fmi.mjt.authserver.user.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UserManager {
    private final AuditLog auditLog;
    private final DatabaseManager databaseManager;
    private final SessionManager sessionManager;
    private final HashMap<String, LocalDateTime> bannedIps;
    private final HashMap<String, Integer> soonToBan;
    private static final int MAX_ATTEMPTS = 5;
    private static final int BAN_DURATION = 5;

    public UserManager(AuditLog auditLog, DatabaseManager databaseManager, SessionManager sessionManager) {
        this.auditLog = auditLog;
        this.databaseManager = databaseManager;
        this.sessionManager = sessionManager;
        bannedIps = new HashMap<>();
        soonToBan = new HashMap<>();
    }

    String parseCommand(String command, String userIp) throws IOException {
        checkBanned(userIp);
        if (command == null) {
            throw new InvalidCommandException("command is null");
        }
        Iterator<String> it = Arrays.stream(command.split(" +")).iterator();
        String result;
        try {
            Command commandRead = Command.valueOf(it.next().replaceAll("-", "_").toUpperCase());
            result = switch (commandRead) {
                case Command.REGISTER -> register(it, userIp);
                case Command.LOGIN -> login(it, userIp);
                case Command.LOGOUT -> logout(it, userIp);
                case Command.UPDATE_USER -> updateUser(it, userIp);
                case Command.RESET_PASSWORD -> resetPassword(it, userIp);
                case Command.ADD_ADMIN_USER -> addAdminUser(it, userIp);
                case Command.REMOVE_ADMIN_USER -> removeAdminUser(it, userIp);
                case Command.DELETE_USER -> deleteUser(it, userIp);
            };
        } catch (IllegalArgumentException e) {
            throw new InvalidCommandException("invalid input", e);
        }
        return result;
    }

    private String register(Iterator<String> it, String userIp) throws IOException {
        User user = new User();
        while (it.hasNext()) {
            Map.Entry<String, String> optionAndArg = getNextOptionAndArg(it);
            switch (optionAndArg.getKey()) {
                case "--username" -> user.setUsername(optionAndArg.getValue());
                case "--password" -> user.setPassword(optionAndArg.getValue());
                case "--first-name" -> user.setFirstName(optionAndArg.getValue());
                case "--last-name" -> user.setLastName(optionAndArg.getValue());
                case "--email" -> user.setEmail(optionAndArg.getValue());
                default -> throw new InvalidCommandException("Unknown option: " + optionAndArg.getKey());
            }
        }
        databaseManager.addToDb(user);
        Session newSession = new Session(user.getUserId(), user.getUsername(), user.isAdmin());
        sessionManager.add(newSession);
        auditLog.logLogin(LocalDateTime.now(), user.getUserId(), userIp);
        return newSession.toString();
    }

    private String login(Iterator<String> it, String userIp) throws IOException {
        String sessionId = null;
        String username = null;
        String password = null;

        while (it.hasNext()) {
            Map.Entry<String, String> optionAndArg = getNextOptionAndArg(it);
            switch (optionAndArg.getKey()) {
                case "--username" -> username = optionAndArg.getValue();
                case "--password" -> password = optionAndArg.getValue();
                case "--session-id" -> sessionId = optionAndArg.getValue();
                default -> throw new InvalidCommandException("Unknown option: " + optionAndArg.getKey());
            }
        }
        if (sessionId != null) {
            return loginWithSession(sessionId, userIp);
        } else if (username != null && password != null) {
            return loginWithUsernamePassword(username, password, userIp);
        }
        throw new InvalidCommandException("invalid input");
    }

    private String loginWithSession(String sessionId, String userIp) throws IOException {
        Session session = sessionManager.getSessionBySessionId(sessionId);
        auditLog.logLogin(LocalDateTime.now(), session.getUserId(), userIp);
        return sessionId;
    }

    private String loginWithUsernamePassword(String username, String password, String userIp) throws IOException {
        String user = databaseManager.findUserInDatabase(username);
        if (user == null) {
            throw new UserNotExistException("User does not exist");
        }
        String[] values = user.split(",");
        if (values[User.PASS_POS].equals(User.getHashedPassword(password, values[User.SALT_POS]))) {
            Session session = new Session(values[User.ID_POS],
                    values[User.USERNAME_POS], Boolean.parseBoolean(values[User.ADMIN_POS]));
            sessionManager.removeByUserId(values[User.ID_POS]);
            sessionManager.add(session);
            auditLog.logLogin(LocalDateTime.now(), values[User.ID_POS], userIp);
            soonToBan.remove(userIp);
            return session.getSessionId();
        }
        auditLog.logUnsuccessfulLogin(LocalDateTime.now(), values[User.ID_POS], userIp);
        if (soonToBan.containsKey(userIp)) {
            if (soonToBan.get(userIp) + 1 == MAX_ATTEMPTS) {
                soonToBan.remove(userIp);
                bannedIps.put(userIp, LocalDateTime.now().plusMinutes(BAN_DURATION));
            } else {
                soonToBan.put(userIp, soonToBan.get(userIp) + 1);
            }
        } else {
            soonToBan.put(userIp, 1);
        }
        throw new InvalidLoginException("wrong password");
    }

    private String logout(Iterator<String> it, String userIp) throws IOException {
        String sessionId = null;
        if (it.hasNext()) {
            Map.Entry<String, String> optionAndArg = getNextOptionAndArg(it);
            if (optionAndArg.getKey().equals("--session-id")) {
                sessionId = optionAndArg.getValue();
            } else {
                throw new InvalidCommandException("Unknown option: " + optionAndArg.getKey());
            }
        }
        if (sessionId != null) {
            auditLog.logLogout(LocalDateTime.now(),
                    sessionManager.getSessionBySessionId(sessionId).getUserId(), userIp);
            sessionManager.removeBySessionId(sessionId);
            return "successfully logged out";
        }
        throw new InvalidCommandException("Invalid command");
    }

    private String deleteUser(Iterator<String> it, String userIp) throws IOException {
        Map.Entry<String, String> sessionIdAndUsername = getSessionIdAndUsername(it);
        String sessionId = sessionIdAndUsername.getKey();
        String username = sessionIdAndUsername.getValue();
        Session session = sessionManager.getSessionBySessionId(sessionId);
        if (session.getAdmin() && !databaseManager.isLastAdmin()) {
            auditLog.logCommandStart(LocalDateTime.now(), Command.DELETE_USER,
                    "database edit", session.getUserId(), userIp,
                    "delete user: " + username);
            try {
                databaseManager.editInDb(true, null, null, null, null, null, null,
                        null, User.USERNAME_POS, username);
            } catch (Exception e) {
                auditLog.logCommandEnd(LocalDateTime.now(), Command.DELETE_USER,
                        "database edit", session.getUserId(), userIp,
                        "fail. " + e.getMessage());
                throw new RuntimeException(e);
            }
            sessionManager.removeByUsername(username);
            auditLog.logCommandEnd(LocalDateTime.now(), Command.DELETE_USER,
                    "database edit", session.getUserId(), userIp,
                    "success");
        } else {
            auditLog.logCommandEnd(LocalDateTime.now(), Command.DELETE_USER,
                    "database edit", session.getUserId(), userIp,
                    "fail");
            throw new PermissionException("No admin permission");
        }
        return "deleted user";
    }

    private String resetPassword(Iterator<String> it, String userIp) throws IOException {
        String sessionId = null;
        String username = null;
        String oldPassword = null;
        String newPassword = null;
        while (it.hasNext()) {
            Map.Entry<String, String> optionAndArg = getNextOptionAndArg(it);
            switch (optionAndArg.getKey()) {
                case "--username" -> username = optionAndArg.getValue();
                case "--new-password" -> newPassword = optionAndArg.getValue();
                case "--old-password" -> oldPassword = optionAndArg.getValue();
                case "--session-id" -> sessionId = optionAndArg.getValue();
                default -> throw new InvalidCommandException("Unknown option: " + optionAndArg.getKey());
            }
        }
        if (sessionId == null || username == null || oldPassword == null || newPassword == null) {
            throw new InvalidCommandException("All arguments are required");
        }
        databaseManager.updatePasswordInDb(sessionId, username, oldPassword, newPassword, userIp);
        return "successfully reset password";
    }

    private String updateUser(Iterator<String> it, String userIp) throws IOException {
        String sessionId = null;
        String username = null;
        String email = null;
        String firstName = null;
        String lastName = null;
        while (it.hasNext()) {
            Map.Entry<String, String> optionAndArg = getNextOptionAndArg(it);
            switch (optionAndArg.getKey()) {
                case "--new-username" -> username = optionAndArg.getValue();
                case "--new-first-name" -> firstName = optionAndArg.getValue();
                case "--new-last-name" -> lastName = optionAndArg.getValue();
                case "--new-email" -> email = optionAndArg.getValue();
                case "--session-id" -> sessionId = optionAndArg.getValue();
                default -> throw new InvalidCommandException("Unknown option: " + optionAndArg.getKey());
            }
        }
        if (username == null && email == null && firstName == null && lastName == null) {
            return "nothing to update";
        }
        databaseManager.updateUserInDb(sessionId, username, email, firstName, lastName, userIp);
        return "successfully edited user";
    }

    private String removeAdminUser(Iterator<String> it, String userIp) throws IOException {
        editAdminUser(it, false, userIp);
        return "removed admin user";
    }

    private String addAdminUser(Iterator<String> it, String userIp) throws IOException {
        editAdminUser(it, true, userIp);
        return "added admin user";
    }

    private void editAdminUser(Iterator<String> it, Boolean admin, String userIp) throws IOException {
        Map.Entry<String, String> sessionIdAndUsername = getSessionIdAndUsername(it);
        String sessionId = sessionIdAndUsername.getKey();
        String username = sessionIdAndUsername.getValue();
        Session session = sessionManager.getSessionBySessionId(sessionId);
        if (!admin && databaseManager.isLastAdmin()) {
            throw new InvalidCommandException("you are the last admin");
        }
        if (session.getAdmin()) {
            auditLog.logCommandStart(LocalDateTime.now(), (admin) ? Command.ADD_ADMIN_USER : Command.REMOVE_ADMIN_USER,
                    "database edit", session.getUserId(), userIp,
                    "set admin status of " + username + " to " + admin);
            try {
                databaseManager.editInDb(false, null, null, null, null, null, null,
                        admin, User.USERNAME_POS, username);
            } catch (Exception e) {
                auditLog.logCommandEnd(LocalDateTime.now(), Command.DELETE_USER, "database edit", session.getUserId(),
                        userIp, "fail. " + e.getMessage());
                throw new RuntimeException(e);
            }
            auditLog.logCommandEnd(LocalDateTime.now(), (admin) ? Command.ADD_ADMIN_USER : Command.REMOVE_ADMIN_USER,
                    "database edit", session.getUserId(), userIp, "success");
        } else {
            auditLog.logCommandEnd(LocalDateTime.now(), (admin) ? Command.ADD_ADMIN_USER : Command.REMOVE_ADMIN_USER,
                    "database edit", session.getUserId(), userIp, "fail. No admin permissions");
            throw new PermissionException("No admin permissions");
        }
        sessionManager.updateSession(username, admin);
    }

    private Map.Entry<String, String> getSessionIdAndUsername(Iterator<String> it) {
        String sessionId = null;
        String username = null;

        while (it.hasNext()) {
            Map.Entry<String, String> optionAndArg = getNextOptionAndArg(it);
            switch (optionAndArg.getKey()) {
                case "--username" -> username = optionAndArg.getValue();
                case "--session-id" -> sessionId = optionAndArg.getValue();
                default -> throw new InvalidCommandException("Unknown option: " + optionAndArg.getKey());
            }
        }

        if (sessionId == null || username == null) {
            throw new InvalidCommandException("not enough arguments");
        }

        return Map.entry(sessionId, username);
    }

    private Map.Entry<String, String> getNextOptionAndArg(Iterator<String> it) {
        String option = it.next();
        if (!it.hasNext()) {
            throw new InvalidCommandException("Missing value for option or no such option: " + option);
        }
        String arg = it.next();
        if (User.FIELDS.contains(arg)) {
            throw new InvalidCommandException("Invalid " + option.substring(2) + ": " + arg);
        }
        return Map.entry(option, arg);
    }

    private void checkBanned(String userIp) {
        if (bannedIps.containsKey(userIp)) {
            if (bannedIps.get(userIp).isBefore(LocalDateTime.now())) {
                bannedIps.remove(userIp);
            } else {
                throw new TemporaryLockException("too many attempts, try again in "
                        + LocalDateTime.now().until(bannedIps.get(userIp), ChronoUnit.MINUTES));
            }
        }
    }
}
