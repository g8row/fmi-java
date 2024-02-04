package bg.sofia.uni.fmi.mjt.authserver.user;

import bg.sofia.uni.fmi.mjt.authserver.ban.BanManager;
import bg.sofia.uni.fmi.mjt.authserver.ban.BanManagerApi;
import bg.sofia.uni.fmi.mjt.authserver.database.DatabaseManagerApi;
import bg.sofia.uni.fmi.mjt.authserver.exception.DatabaseException;
import bg.sofia.uni.fmi.mjt.authserver.exception.InvalidCommandException;
import bg.sofia.uni.fmi.mjt.authserver.exception.InvalidSessionException;
import bg.sofia.uni.fmi.mjt.authserver.exception.PermissionException;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserExistsException;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.authserver.log.AuditLogApi;
import bg.sofia.uni.fmi.mjt.authserver.response.Response;
import bg.sofia.uni.fmi.mjt.authserver.server.Command;
import bg.sofia.uni.fmi.mjt.authserver.session.Session;
import bg.sofia.uni.fmi.mjt.authserver.session.SessionManagerApi;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UserManager implements UserManagerApi {
    private final AuditLogApi auditLog;
    private final DatabaseManagerApi databaseManager;
    private final SessionManagerApi sessionManager;
    private final BanManagerApi banManager;
    public static final List<String> FIELDS = List.of("--username", "--password", "--first-name",
            "--last-name", "--email", "--session-id", "--new-username", "--new-first-name", "--new-last-name",
            "--new-email", "--new-password", "--old-password");

    public UserManager(AuditLogApi auditLog, DatabaseManagerApi databaseManager, SessionManagerApi sessionManager,
                       BanManagerApi banManager) {
        this.auditLog = auditLog;
        this.databaseManager = databaseManager;
        this.sessionManager = sessionManager;
        this.banManager = banManager;
    }

    private Map<String, String> getOptionsAndArgs(Iterator<String> it) {
        Map<String, String> optionsAndArgs = new HashMap<>();
        while (it.hasNext()) {
            String option = it.next();
            if (!it.hasNext()) {
                throw new InvalidCommandException("Missing value for option or no such option: " + option);
            }
            String arg = it.next();
            if (FIELDS.contains(arg)) {
                throw new InvalidCommandException("Invalid " + option.substring(2) + ": " + arg);
            }
            optionsAndArgs.put(option, arg);
        }
        return optionsAndArgs;
    }

    public Response parseCommand(String command, String userIp) throws IOException {
        if (banManager.checkBanned(userIp)) {
            return new Response(false, "temporarily banned");
        }
        if (command == null) {
            return new Response(false, "command is null");
        }
        Iterator<String> it = Arrays.stream(command.split(" +")).iterator();
        Response result;
        try {
            result = getResponse(it, userIp);
        } catch (IllegalArgumentException e) {
            return new Response(false, "invalid input");
        } catch (InvalidCommandException e) {
            return new Response(false, e.getMessage());
        } catch (Exception e) {
            auditLog.logError(userIp, e);
            return new Response(false, "Internal Error");
        }
        return result;
    }

    private Response getResponse(Iterator<String> it, String userIp) throws IOException {
        return switch (Command.valueOf(it.next().replaceAll("-", "_").toUpperCase())) {
            case Command.REGISTER -> register(getOptionsAndArgs(it), userIp);
            case Command.LOGIN -> login(getOptionsAndArgs(it), userIp);
            case Command.LOGOUT -> logout(getOptionsAndArgs(it), userIp);
            case Command.UPDATE_USER -> updateUser(getOptionsAndArgs(it), userIp);
            case Command.RESET_PASSWORD -> resetPassword(getOptionsAndArgs(it), userIp);
            case Command.ADD_ADMIN_USER -> addAdminUser(getOptionsAndArgs(it), userIp);
            case Command.REMOVE_ADMIN_USER -> removeAdminUser(getOptionsAndArgs(it), userIp);
            case Command.DELETE_USER -> deleteUser(getOptionsAndArgs(it), userIp);
        };
    }

    public Response register(Map<String, String> optionsAndArgs, String userIp) throws IOException {
        String username = null;
        String password = null;
        String firstName = null;
        String lastName = null;
        String email = null;
        try {
            for (Map.Entry<String, String> entry : optionsAndArgs.entrySet()) {
                switch (entry.getKey()) {
                    case "--username" -> username = entry.getValue();
                    case "--password" -> password = entry.getValue();
                    case "--first-name" -> firstName = entry.getValue();
                    case "--last-name" -> lastName = entry.getValue();
                    case "--email" -> email = entry.getValue();
                }
            }
            User user = new User(username, password, firstName, lastName, email, false);
            return addUserToDb(user, userIp);
        } catch (InvalidCommandException | UserExistsException e) {
            return new Response(false, e.getMessage());
        }
    }

    private Response addUserToDb(User user, String userIp) throws IOException {
        try {
            databaseManager.addUser(user);
        } catch (InvalidCommandException | UserExistsException e) {
            return new Response(false, e.getMessage());
        } catch (DatabaseException e) {
            auditLog.logError(userIp, e);
            return new Response(false, "Internal Error");
        }
        Session newSession = new Session(user.getUserId(), user.getUsername(), false);
        sessionManager.add(newSession);
        auditLog.logLogin(LocalDateTime.now(), user.getUserId(), userIp);
        return new Response(true, newSession.toString());
    }

    public Response login(Map<String, String> optionsAndArgs, String userIp) throws IOException {
        String sessionId = null;
        String username = null;
        String password = null;

        try {
            for (Map.Entry<String, String> entry : optionsAndArgs.entrySet()) {
                switch (entry.getKey()) {
                    case "--session-id" -> sessionId = entry.getValue();
                    case "--username" -> username = entry.getValue();
                    case "--password" -> password = entry.getValue();
                }
            }
            if (sessionId != null) {
                return loginWithSession(sessionId, userIp);
            } else if (username != null && password != null) {
                return loginWithUsernamePassword(username, password, userIp);
            }
        } catch (InvalidCommandException | InvalidSessionException e) {
            return new Response(false, e.getMessage());
        }
        return new Response(false, "invalid input");
    }

    public Response loginWithSession(String sessionId, String userIp) throws IOException {
        Session session;
        try {
            session = sessionManager.getSessionBySessionId(sessionId);
        } catch (InvalidSessionException e) {
            auditLog.logUnsuccessfulLogin(LocalDateTime.now(), "unknown user", userIp);
            return new Response(false, e.getMessage());
        }
        auditLog.logLogin(LocalDateTime.now(), session.getUserId(), userIp);
        return new Response(true, session.toString());
    }

    public Response loginWithUsernamePassword(String username, String password, String userIp) throws IOException {
        User user;
        try {
            user = databaseManager.findUserInDatabase(username);
        } catch (UserNotFoundException e) {
            auditLog.logUnsuccessfulLogin(LocalDateTime.now(), "", userIp);
            return new Response(false, e.getMessage());
        }
        if (user == null) {
            auditLog.logUnsuccessfulLogin(LocalDateTime.now(), "", userIp);
            return new Response(false, "User does not exist");
        }
        if (user.getPasswordHash().equals(User.getHashedPassword(password, user.getSalt()))) {
            Session session = new Session(user.getUserId(),
                    user.getUsername(), user.isAdmin());
            sessionManager.removeByUserId(user.getUserId());
            sessionManager.add(session);
            auditLog.logLogin(LocalDateTime.now(), user.getUserId(), userIp);
            banManager.clearUserAttempts(userIp);
            return new Response(true, session.getSessionId());
        }
        auditLog.logUnsuccessfulLogin(LocalDateTime.now(), user.getUserId(), userIp);
        banManager.addFailedAttempt(userIp);
        return new Response(false, "wrong password");
    }

    public Response logout(Map<String, String> optionsAndArgs, String userIp) throws IOException {
        if (optionsAndArgs.containsKey("--session-id")) {
            String sessionId = optionsAndArgs.get("--session-id");
            Session session;
            try {
                session = sessionManager.getSessionBySessionId(sessionId);
            } catch (InvalidSessionException e) {
                return new Response(false, e.getMessage());
            }
            auditLog.logLogout(LocalDateTime.now(), session.getUserId(), userIp);
            sessionManager.removeBySessionId(sessionId);
            return new Response(true, "successfully logged out");
        } else {
            return new Response(false, "Invalid command");
        }
    }

    public Response deleteUser(Map<String, String> optionsAndArgs, String userIp) throws IOException {
        String sessionId = optionsAndArgs.get("--session-id");
        String username = optionsAndArgs.get("--username");
        if (sessionId == null || username == null) {
            return new Response(false, "All arguments are required");
        }
        try {
            Session session = sessionManager.getSessionBySessionId(sessionId);
            return deleteUserFromDb(session, username, userIp);
        } catch (InvalidSessionException e) {
            return new Response(false, e.getMessage());
        }
    }

    private Response deleteUserFromDb(Session session, String username, String userIp) throws IOException {
        if (session.getAdmin() && !(session.getUsername().equals(username) && databaseManager.isLastAdmin())) {
            auditLog.logCommandStart(LocalDateTime.now(), Command.DELETE_USER, "database edit", session.getUserId()
                    , userIp, "delete user: " + username);
            String userId;
            try {
                userId = databaseManager.findUserInDatabase(username).getUserId();
            } catch (UserNotFoundException e) {
                auditLog.logCommandEnd(LocalDateTime.now(), Command.DELETE_USER, "database edit", session.getUserId(),
                        userIp, "fail." + " user does not exist. " + e.getMessage());
                return new Response(false, e.getMessage());
            }
            try {
                databaseManager.deleteUser(userId);
            } catch (DatabaseException e) {
                auditLog.logCommandEnd(LocalDateTime.now(), Command.DELETE_USER, "database edit", session.getUserId(),
                        userIp, "fail." + " database error. " + e.getMessage());
                auditLog.logError(userIp, e);
                return new Response(false, e.getMessage());
            }
        } else {
            return new Response(false, "No admin permission or last admin");
        }
        sessionManager.removeByUsername(username);
        auditLog.logCommandEnd(LocalDateTime.now(), Command.DELETE_USER, "database edit", session.getUserId(),
                userIp, "success");
        return new Response(true, "deleted user");
    }

    public Response resetPassword(Map<String, String> optionsAndArgs, String userIp) throws IOException {
        String sessionId = null;
        String username = null;
        String oldPassword = null;
        String newPassword = null;
        for (Map.Entry<String, String> entry : optionsAndArgs.entrySet()) {
            switch (entry.getKey()) {
                case "--username" -> username = entry.getValue();
                case "--old-password" -> oldPassword = entry.getValue();
                case "--new-password" -> newPassword = entry.getValue();
                case "--session-id" -> sessionId = entry.getValue();
            }
        }
        if (sessionId == null || username == null || oldPassword == null || newPassword == null) {
            return new Response(false, "All arguments are required");
        }
        return updatePasswordInDb(sessionId, username, oldPassword, newPassword, userIp);
    }

    private Response updatePasswordInDb(String sessionId, String username,
                                        String oldPassword, String newPassword, String userIp) throws IOException {
        Session session;
        try {
            session = sessionManager.getSessionBySessionId(sessionId);
        } catch (InvalidSessionException e) {
            return new Response(false, e.getMessage());
        }
        if (!session.getUsername().equals(username)) {
            return new Response(false, "username does not match session username");
        }
        auditLog.logCommandStart(LocalDateTime.now(), Command.RESET_PASSWORD,
                "database edit", session.getUserId(), userIp,
                "reset password of" + username);
        try {
            databaseManager.editPassword(session.getUserId(), oldPassword, newPassword);
            auditLog.logCommandEnd(LocalDateTime.now(), Command.RESET_PASSWORD,
                    "database edit", session.getUserId(), userIp,
                    "success");
            return new Response(true, "successfully reset password");
        } catch (DatabaseException e) {
            auditLog.logCommandEnd(LocalDateTime.now(), Command.RESET_PASSWORD,
                    "database edit", session.getUserId(), userIp,
                    "fail. " + e.getMessage());
            auditLog.logError(userIp, e);
            return new Response(false, "Internal error");
        }
    }

    private Response updateUserInDb(String sessionId, String username,
                                    String email, String firstName, String lastName, String userIp) throws IOException {
        Session session = sessionManager.getSessionBySessionId(sessionId);
        auditLog.logCommandStart(LocalDateTime.now(), Command.RESET_PASSWORD,
                "database edit", session.getUserId(), userIp,
                "edit user of" + username);
        try {
            databaseManager.editUser(session.getUserId(), username, firstName, lastName, email, null);
            sessionManager.updateSessionUsername(session.getUsername(), username);
            auditLog.logCommandEnd(LocalDateTime.now(), Command.DELETE_USER,
                    "database edit", session.getUserId(), userIp,
                    "success");
            return new Response(true, "successfully updated user");
        } catch (DatabaseException e) {
            auditLog.logCommandEnd(LocalDateTime.now(), Command.DELETE_USER,
                    "database edit", session.getUserId(), userIp,
                    "fail. " + e.getMessage());
            return new Response(false, "Internal error");
        }
    }

    public Response updateUser(Map<String, String> optionsAndArgs, String userIp) throws IOException {
        String sessionId = null;
        String username = null;
        String email = null;
        String firstName = null;
        String lastName = null;
        for (Map.Entry<String, String> entry : optionsAndArgs.entrySet()) {
            switch (entry.getKey()) {
                case "--new-username" -> username = entry.getValue();
                case "--new-email" -> email = entry.getValue();
                case "--new-first-name" -> firstName = entry.getValue();
                case "--new-last-name" -> lastName = entry.getValue();
                case "--session-id" -> sessionId = entry.getValue();
            }
        }
        if (username == null && email == null && firstName == null && lastName == null) {
            return new Response(false, "nothing to update");
        }
        if ((username != null && username.contains(",")) || (firstName != null && firstName.contains(","))
                || (lastName != null && lastName.contains(",")) || (email != null && email.contains(","))) {
            throw new InvalidCommandException(", is forbidden");
        }
        if ((username != null && username.contains("\n")) || (firstName != null && firstName.contains("\n"))
                || (lastName != null && lastName.contains("\n")) || (email != null && email.contains("\n"))) {
            throw new InvalidCommandException("newLine is forbidden");
        }
        return updateUserInDb(sessionId, username, email, firstName, lastName, userIp);
    }

    public Response removeAdminUser(Map<String, String> optionsAndArgs, String userIp) throws IOException {
        try {
            String sessionId = optionsAndArgs.get("--session-id");
            String username = optionsAndArgs.get("--username");
            if (sessionId == null || username == null) {
                return new Response(false, "All arguments are required");
            }
            editAdminUser(sessionId, username, false, userIp);
            return new Response(true, "removed admin user");
        } catch (InvalidCommandException | DatabaseException | PermissionException e) {
            return new Response(false, e.getMessage());
        }
    }

    public Response addAdminUser(Map<String, String> optionsAndArgs, String userIp) throws IOException {
        try {
            String sessionId = optionsAndArgs.get("--session-id");
            String username = optionsAndArgs.get("--username");
            if (sessionId == null || username == null) {
                return new Response(false, "All arguments are required");
            }
            editAdminUser(sessionId, username, true, userIp);
            return new Response(true, "added admin user");
        } catch (InvalidCommandException | InvalidSessionException | DatabaseException | PermissionException e) {
            return new Response(false, e.getMessage());
        }
    }

    private void editAdminUser(String sessionId, String username, Boolean admin, String userIp) throws IOException {
        Session session = sessionManager.getSessionBySessionId(sessionId);
        if (!admin && databaseManager.isLastAdmin()) {
            throw new InvalidCommandException("you are the last admin");
        }
        if (session.getAdmin()) {
            auditLog.logCommandStart(LocalDateTime.now(), (admin) ? Command.ADD_ADMIN_USER : Command.REMOVE_ADMIN_USER,
                    "database edit", session.getUserId(), userIp,
                    "set admin status of " + username + " to " + admin);
            try {
                databaseManager.editUser(databaseManager.findUserInDatabase(username).getUserId(), null,
                        null, null, null, admin);
                sessionManager.updateSessionAdmin(username, admin);
                auditLog.logCommandEnd(LocalDateTime.now(),
                        (admin) ? Command.ADD_ADMIN_USER : Command.REMOVE_ADMIN_USER,
                        "database edit", session.getUserId(), userIp, "success");
            } catch (DatabaseException e) {
                auditLog.logCommandEnd(LocalDateTime.now(), Command.DELETE_USER, "database edit", session.getUserId(),
                        userIp, "fail. " + e.getMessage());
                throw e;
            }
        } else {
            throw new PermissionException("No admin permissions");
        }
    }
}
