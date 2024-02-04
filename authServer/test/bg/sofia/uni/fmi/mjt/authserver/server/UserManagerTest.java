package bg.sofia.uni.fmi.mjt.authserver.server;

import bg.sofia.uni.fmi.mjt.authserver.ban.BanManager;
import bg.sofia.uni.fmi.mjt.authserver.database.DatabaseManager;
import bg.sofia.uni.fmi.mjt.authserver.exception.DatabaseException;
import bg.sofia.uni.fmi.mjt.authserver.exception.InvalidSessionException;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.authserver.log.AuditLog;
import bg.sofia.uni.fmi.mjt.authserver.response.Response;
import bg.sofia.uni.fmi.mjt.authserver.session.Session;
import bg.sofia.uni.fmi.mjt.authserver.session.SessionManager;
import bg.sofia.uni.fmi.mjt.authserver.user.User;
import bg.sofia.uni.fmi.mjt.authserver.user.UserManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class UserManagerTest {

    @Mock
    private AuditLog auditLog;

    @Mock
    private DatabaseManager databaseManager;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private BanManager banManager;

    @InjectMocks
    private UserManager userManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testParseCommandRegister() throws IOException {

        String validRegisterCommand = "register mm";
        String userIp = "1.1.1.1";


        Response response = userManager.parseCommand(validRegisterCommand, userIp);
        System.out.println(response.message());

        assertFalse(response.success());
        assertNotNull(response.message());
    }

    @Test
    void testParseCommand_Exceptions() throws IOException {

        String invalidCommand = "invalid-command";
        String userIp = "127.0.0.1";


        Response response = userManager.parseCommand(invalidCommand, userIp);


        assertFalse(response.success());
        assertNotNull(response.message());
        verifyNoInteractions(auditLog);  // No interactions with auditLog expected
        verifyNoInteractions(databaseManager);  // No interactions with databaseManager expected
        verifyNoInteractions(sessionManager);  // No interactions with sessionManager expected


        String nullCommand = null;


        response = userManager.parseCommand(nullCommand, userIp);


        assertFalse(response.success());
        assertNotNull(response.message());
        verifyNoInteractions(auditLog);
        verifyNoInteractions(databaseManager);
        verifyNoInteractions(sessionManager);


    }

    @Test
    void testParseCommandBanned() throws IOException {

        String invalidCommand = "login --session-id dasd";
        String userIp = "127.0.0.1";


        when(banManager.checkBanned(userIp)).thenReturn(true);
        Response response = userManager.parseCommand(invalidCommand, userIp);


        assertFalse(response.success());
        assertNotNull(response.message());
        verifyNoInteractions(auditLog);  // No interactions with auditLog expected
        verifyNoInteractions(databaseManager);  // No interactions with databaseManager expected
        verifyNoInteractions(sessionManager);  // No interactions with sessionManager expected
    }

    @Test
    void testRegisterSuccess() throws IOException {

        String validRegisterCommand = "register --username testUser --password testPass " +
                "--first-name John --last-name Doe --email john@example.com";
        String userIp = "127.0.0.1";

        Response response = userManager.parseCommand(validRegisterCommand, userIp);


        assertTrue(response.success());
        assertNotNull(response.message());
        verify(databaseManager).addUser(any(User.class));
        verify(sessionManager).add(any());
        verifyNoMoreInteractions(databaseManager);
        verifyNoMoreInteractions(sessionManager);
        verify(auditLog).logLogin(any(), any(), any());
        verifyNoMoreInteractions(auditLog);
    }

    @Test
    void testRegisterException() throws IOException {

        String invalidRegisterCommand = "register --username testUser --password testPass " +
                "--first-name John --last-name Doe --email";
        String userIp = "1.1.1.1";


        Response response = userManager.parseCommand(invalidRegisterCommand, userIp);


        assertFalse(response.success());
        assertNotNull(response.message());
        verifyNoInteractions(databaseManager);
        verifyNoInteractions(sessionManager);


    }

    @Test
    void testLoginSuccess() throws IOException {

        String validLoginCommand = "login --username testUser --password testPass";
        String userIp = "1.1.1.1";
        when(databaseManager.findUserInDatabase("testUser")).thenReturn(new User("testUser", "testPass", "John", "Doe", "asd", false));

        Response response = userManager.parseCommand(validLoginCommand, userIp);


        assertTrue(response.success());
        assertNotNull(response.message());
        verify(databaseManager).findUserInDatabase("testUser");
        verify(sessionManager).add(any());
        verify(sessionManager).removeByUserId(any());
        verify(auditLog).logLogin(any(), any(), any());
        verifyNoMoreInteractions(sessionManager);


    }

    @Test
    void testLoginUserNotFound() throws IOException {

        String validLoginCommand = "login --username nonExistentUser --password testPass";
        String userIp = "1.1.1.1";

        when(databaseManager.findUserInDatabase("nonExistentUser")).thenReturn(null);


        Response response = userManager.parseCommand(validLoginCommand, userIp);


        assertFalse(response.success());
        assertNotNull(response.message());
        verify(databaseManager).findUserInDatabase("nonExistentUser");
        verify(auditLog).logUnsuccessfulLogin(any(), any(), any());

        verifyNoMoreInteractions(databaseManager);
        verifyNoMoreInteractions(sessionManager);
    }

    @Test
    void testLoginIncorrectPassword() throws IOException {

        String validLoginCommand = "login --username testUser --password incorrectPass";
        String userIp = "1.1.1.1";

        when(databaseManager.findUserInDatabase("testUser")).thenReturn(new User("testUser", "correctPass", "John", "Doe", "asd", false));


        Response response = userManager.parseCommand(validLoginCommand, userIp);


        assertFalse(response.success());
        assertNotNull(response.message());
        verify(databaseManager).findUserInDatabase("testUser");
        verify(auditLog).logUnsuccessfulLogin(any(), any(), any());
        verifyNoMoreInteractions(databaseManager);
        verifyNoMoreInteractions(sessionManager);
    }

    @Test
    void testLoginWithSessionIdSuccess() throws IOException {

        String validLoginCommand = "login --session-id testSessionId";
        String userIp = "1.1.1.1";
        String sessionId = "testSessionId";


        when(sessionManager.getSessionBySessionId(sessionId)).thenReturn(new Session("testUserId", "testUser", false));


        Response response = userManager.parseCommand(validLoginCommand, userIp);


        assertTrue(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId(sessionId);
        verify(auditLog).logLogin(any(), any(), any());
        verifyNoMoreInteractions(sessionManager);
    }

    @Test
    void testLoginWithSessionIdInvalidUser() throws IOException {

        String validLoginCommand = "login --session-id testSessionId";
        String userIp = "1.1.1.1";
        String sessionId = "testSessionId";


        when(sessionManager.getSessionBySessionId(sessionId)).thenThrow(new InvalidSessionException("Invalid session"));


        Response response = userManager.parseCommand(validLoginCommand, userIp);


        assertFalse(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId(sessionId);
        verify(auditLog).logUnsuccessfulLogin(any(), any(), any());
        verifyNoMoreInteractions(databaseManager);
        verifyNoMoreInteractions(sessionManager);
    }


    @Test
    void testLogoutSuccess() throws IOException {

        String validLogoutCommand = "logout --session-id testSessionId";
        String userIp = "1.1.1.1";


        when(sessionManager.getSessionBySessionId("testSessionId")).thenReturn(new Session("testUserId", "testUser", false));


        Response response = userManager.parseCommand(validLogoutCommand, userIp);


        assertTrue(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId("testSessionId");
        verify(sessionManager).removeBySessionId("testSessionId");
        verify(auditLog).logLogout(any(), any(), any());
        verifyNoMoreInteractions(auditLog);
        verifyNoMoreInteractions(sessionManager);
    }

    @Test
    void testLogoutInvalidSessionId() throws IOException {

        String invalidLogoutCommand = "logout --session-id invalidSessionId";
        String userIp = "1.1.1.1";

        when(sessionManager.getSessionBySessionId("invalidSessionId")).thenThrow(new InvalidSessionException("Invalid session"));


        Response response = userManager.parseCommand(invalidLogoutCommand, userIp);


        assertFalse(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId("invalidSessionId");
        verifyNoInteractions(auditLog);
        verifyNoMoreInteractions(sessionManager);
    }

    @Test
    void testLogoutMissingSessionId() throws IOException {

        String invalidLogoutCommand = "logout";
        String userIp = "1.1.1.1";


        Response response = userManager.parseCommand(invalidLogoutCommand, userIp);


        assertFalse(response.success());
        assertNotNull(response.message());
        verifyNoInteractions(sessionManager);
    }

    @Test
    void testLogoutException() throws IOException {

        String validLogoutCommand = "logout --session-id validSessionId";
        String userIp = "1.1.1.1";

        when(sessionManager.getSessionBySessionId("validSessionId")).thenThrow(new InvalidSessionException("invalid session"));


        Response response = userManager.parseCommand(validLogoutCommand, userIp);


        assertFalse(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId("validSessionId");
        verifyNoMoreInteractions(sessionManager, databaseManager, auditLog);
    }

    @Test
    void testAddAdminUser() throws IOException {

        String validCommand = "add-admin-user --session-id ses --username testUser";
        String userIp = "1.1.1.1";

        when(sessionManager.getSessionBySessionId(any())).thenReturn(new Session("testUserId", "adminUser", true));
        when(databaseManager.findUserInDatabase(any())).thenReturn(new User("testUser", "password", "John", "Doe", "test@example.com", false));
        when(databaseManager.findUserInDatabase(any())).thenReturn(new User("testUser", "password", "John", "Doe", "", false));

        Response response = userManager.parseCommand(validCommand, userIp);


        assertTrue(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId(any());
        verify(sessionManager).updateSessionAdmin(any(), any());
        verify(databaseManager).findUserInDatabase(any());
        verify(databaseManager).editUser(any(), any(), any(), any(), any(), any());
        verify(auditLog).logCommandStart(any(), any(), any(), any(), any(), any());
        verify(auditLog).logCommandEnd(any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(sessionManager, databaseManager, auditLog);
    }

    @Test
    void testRemoveAdminUser() throws IOException {

        String validCommand = "remove-admin-user --session-id ses --username testUser";
        String userIp = "1.1.1.1";

        when(sessionManager.getSessionBySessionId(any())).thenReturn(new Session("testUserId", "adminUser", true));
        when(databaseManager.findUserInDatabase(any())).thenReturn(new User("testUser", "password", "John", "Doe", "test@example.com", false));
        when(databaseManager.isLastAdmin()).thenReturn(false);

        Response response = userManager.parseCommand(validCommand, userIp);


        assertTrue(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId(any());
        verify(sessionManager).updateSessionAdmin(any(), any());
        verify(databaseManager).findUserInDatabase(any());
        verify(databaseManager).editUser(any(), any(), any(), any(), any(), any());
        verify(databaseManager).isLastAdmin();
        verify(auditLog).logCommandStart(any(), any(), any(), any(), any(), any());
        verify(auditLog).logCommandEnd(any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(sessionManager, databaseManager, auditLog);
    }

    @Test
    void testRemoveAdminUserLastAdmin() throws IOException {

        String validCommand = "remove-admin-user --session-id ses --username testUser";
        String userIp = "1.1.1.1";

        when(sessionManager.getSessionBySessionId(any())).thenReturn(new Session("testUserId", "adminUser", true));
        when(databaseManager.isLastAdmin()).thenReturn(true);

        Response response = userManager.parseCommand(validCommand, userIp);


        assertFalse(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId(any());
        verify(databaseManager).isLastAdmin();
        verifyNoMoreInteractions(sessionManager, databaseManager, auditLog);
    }

    @Test
    void testUpdateUser() throws IOException {

        String validCommand = "update-user --session-id ses --new-username username --new-first-name NewFirstName --new-last-name NewLastName --new-email new@example.com";
        String userIp = "1.1.1.1";


        when(sessionManager.getSessionBySessionId(any())).thenReturn(new Session("testUserId", "testUser", false));


        Response response = userManager.parseCommand(validCommand, userIp);


        assertTrue(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId(any());
        verify(databaseManager).editUser(any(), any(), any(), any(), any(), any());
        verify(sessionManager).updateSessionUsername(any(), any());
        verify(auditLog).logCommandStart(any(), any(), any(), any(), any(), any());
        verify(auditLog).logCommandEnd(any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(sessionManager, databaseManager, auditLog);
    }

    @Test
    void testUpdateUserDBException() throws IOException {

        String validCommand = "update-user --session-id ses --new-username username --new-first-name NewFirstName --new-last-name NewLastName --new-email new@example.com";
        String userIp = "1.1.1.1";

        when(sessionManager.getSessionBySessionId(any())).thenReturn(new Session("testUserId", "testUser", false));
        doThrow(new DatabaseException("")).when(databaseManager).editUser(any(), any(), any(), any(), any(), any());

        Response response = userManager.parseCommand(validCommand, userIp);
        System.out.println(response.message());

        assertFalse(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId(any());
        verify(databaseManager).editUser(any(), any(), any(), any(), any(), any());
        verify(auditLog).logCommandStart(any(), any(), any(), any(), any(), any());
        verify(auditLog).logCommandEnd(any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(sessionManager, databaseManager, auditLog);
    }

    @Test
    void testEditAdminUserDBException() throws IOException {

        String validCommand = "add-admin-user --session-id ses --username testUser";
        String userIp = "1.1.1.1";

        when(sessionManager.getSessionBySessionId(any())).thenReturn(new Session("testUserId", "testUser", true));
        doThrow(new DatabaseException("")).when(databaseManager).editUser(any(), any(), any(), any(), any(), any());
        when(databaseManager.findUserInDatabase(any())).thenReturn(new User("testUser", "password", "John", "Doe", "", false));

        Response response = userManager.parseCommand(validCommand, userIp);

        System.out.println(response.message());

        assertFalse(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId(any());
        verify(databaseManager).findUserInDatabase(any());
        verify(databaseManager).editUser(any(), any(), any(), any(), any(), any());
        verify(auditLog).logCommandStart(any(), any(), any(), any(), any(), any());
        verify(auditLog).logCommandEnd(any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(sessionManager, databaseManager, auditLog);
    }

    @Test
    void testResetPassword() throws IOException {

        String validCommand = "reset-password --session-id ses --username testUser --new-password newPassword --old-password oldPassword";
        String userIp = "1.1.1.1";

        when(sessionManager.getSessionBySessionId(any())).thenReturn(new Session("ses", "testUser", true));
        when(databaseManager.findUserInDatabase("testUser")).thenReturn(new User("testUser", "oldPassword", "John", "Doe", "test@example.com", false));


        Response response = userManager.parseCommand(validCommand, userIp);


        assertTrue(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId(any());
        verify(databaseManager).editPassword(any(), any(), any());
        verify(auditLog).logCommandStart(any(), any(), any(), any(), any(), any());
        verify(auditLog).logCommandEnd(any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(sessionManager, databaseManager, auditLog);
    }

    @Test
    void testResetPasswordInvalidSession() throws IOException {

        String invalidSessionResetPasswordCommand = "reset-password --username testUser --old-password oldPass --new-password newPass --session-id invalidSessionId";
        String userIp = "1.1.1.1";

        when(sessionManager.getSessionBySessionId("invalidSessionId")).thenThrow(new InvalidSessionException("Invalid session"));


        Response response = userManager.parseCommand(invalidSessionResetPasswordCommand, userIp);
        System.out.println(response.message());


        assertFalse(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId("invalidSessionId");
        verifyNoMoreInteractions(sessionManager, databaseManager, auditLog);
    }

    @Test
    void testResetPasswordNotAllArguments() throws IOException {

        String userNotFoundResetPasswordCommand = "reset-password --old-password oldPass --new-password newPass --session-id testSessionId";
        String userIp = "1.1.1.1";


        Session session = new Session("testSessionId", "adminUser", true);
        when(sessionManager.getSessionBySessionId("testSessionId")).thenReturn(session);
        when(databaseManager.findUserInDatabase("nonExistentUser")).thenReturn(null);


        Response response = userManager.parseCommand(userNotFoundResetPasswordCommand, userIp);
        System.out.println(response.message());


        assertFalse(response.success());
        assertNotNull(response.message());
        verifyNoMoreInteractions(sessionManager, databaseManager, auditLog);
    }

    @Test
    void testResetPasswordDbException() throws IOException {

        String command = "reset-password --username testUser --old-password oldPass --new-password newPass --session-id ses";
        String userIp = "1.1.1.1";

        Session session = new Session("testSessionId", "testUser", true);
        when(sessionManager.getSessionBySessionId("ses")).thenReturn(session);
        doThrow(new DatabaseException("")).when(databaseManager).editPassword(any(), any(), any());

        Response response = userManager.parseCommand(command, userIp);
        System.out.println(response.message());


        assertFalse(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId("ses");
        verify(databaseManager).editPassword(any(), any(), any());
        verify(auditLog).logCommandStart(any(), any(), any(), any(), any(), any());
        verify(auditLog).logCommandEnd(any(), any(), any(), any(), any(), any());
        verify(auditLog).logError(any(), any());
        verifyNoMoreInteractions(sessionManager, databaseManager, auditLog);
    }

    @Test
    void testDeleteUserSuccess() throws IOException {

        String deleteUserCommand = "delete-user --session-id testSessionId --username testUser";
        String userIp = "1.1.1.1";


        Session session = new Session("testSessionId", "adminUser", true);
        when(sessionManager.getSessionBySessionId("testSessionId")).thenReturn(session);
        when(databaseManager.findUserInDatabase("testUser")).thenReturn(new User("testUser", "testPass", "John", "Doe", "asd", false));


        Response response = userManager.parseCommand(deleteUserCommand, userIp);
        System.out.println(response.message());

        assertTrue(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId("testSessionId");
        verify(databaseManager).findUserInDatabase("testUser");
        verify(databaseManager).deleteUser(any());
        verify(sessionManager).removeByUsername("testUser");
        verify(auditLog).logCommandStart(any(), any(), any(), any(), any(), any());
        verify(auditLog).logCommandEnd(any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(sessionManager, databaseManager, auditLog);
    }

    @Test
    void testDeleteUserNotFoundException() throws IOException {

        String deleteUserCommand = "delete-user --session-id testSessionId --username testUser";
        String userIp = "1.1.1.1";

        Session session = new Session("testSessionId", "adminUser", true);
        when(sessionManager.getSessionBySessionId("testSessionId")).thenReturn(session);
        when(databaseManager.findUserInDatabase("testUser")).thenThrow(new UserNotFoundException("User not found"));


        Response response = userManager.parseCommand(deleteUserCommand, userIp);
        System.out.println(response.message());


        assertFalse(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId("testSessionId");
        verify(databaseManager).findUserInDatabase("testUser");
        verify(auditLog).logCommandStart(any(), any(), any(), any(), any(), any());
        verify(auditLog).logCommandEnd(any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(sessionManager, databaseManager, auditLog);
    }

    @Test
    void testDeleteUserDbException() throws IOException {

        String deleteUserCommand = "delete-user --session-id testSessionId --username testUser";
        String userIp = "1.1.1.1";

        Session session = new Session("testSessionId", "adminUser", true);
        when(sessionManager.getSessionBySessionId("testSessionId")).thenReturn(session);
        when(databaseManager.findUserInDatabase("testUser")).thenReturn(new User("testUser", "testPass", "John", "Doe", "asd", false));
        doThrow(new DatabaseException("")).when(databaseManager).deleteUser(any());

        Response response = userManager.parseCommand(deleteUserCommand, userIp);
        System.out.println(response.message());


        assertFalse(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId("testSessionId");
        verify(databaseManager).findUserInDatabase("testUser");
        verify(databaseManager).deleteUser(any());
        verify(auditLog).logCommandStart(any(), any(), any(), any(), any(), any());
        verify(auditLog).logCommandEnd(any(), any(), any(), any(), any(), any());
        verify(auditLog).logError(any(), any());
        verifyNoMoreInteractions(sessionManager, databaseManager, auditLog);
    }


    @Test
    void testDeleteUserInvalidSession() throws IOException {

        String invalidSessionDeleteUserCommand = "delete-user --session-id invalidSessionId --username testUser";
        String userIp = "1.1.1.1";


        when(sessionManager.getSessionBySessionId("invalidSessionId")).thenThrow(new InvalidSessionException("Invalid session"));


        Response response = userManager.parseCommand(invalidSessionDeleteUserCommand, userIp);


        assertFalse(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId("invalidSessionId");
        verifyNoMoreInteractions(sessionManager, databaseManager, auditLog);
    }

    @Test
    void testDeleteUserNoAdminPermission() throws IOException {

        String deleteUserCommand = "delete-user --session-id testSessionId --username nonAdminUser";
        String userIp = "1.1.1.1";

        Session session = new Session("testSessionId", "nonAdminUser", false);
        when(sessionManager.getSessionBySessionId("testSessionId")).thenReturn(session);


        Response response = userManager.parseCommand(deleteUserCommand, userIp);


        assertFalse(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId("testSessionId");
        verifyNoMoreInteractions(sessionManager, databaseManager, auditLog);
    }

    @Test
    void testEditAdminUserNoAdminPermission() throws IOException {

        String deleteUserCommand = "add-admin-user --session-id testSessionId --username nonAdminUser";
        String userIp = "1.1.1.1";

        Session session = new Session("testSessionId", "nonAdminUser", false);
        when(sessionManager.getSessionBySessionId("testSessionId")).thenReturn(session);


        Response response = userManager.parseCommand(deleteUserCommand, userIp);


        assertFalse(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId("testSessionId");
        verifyNoMoreInteractions(sessionManager, databaseManager, auditLog);
    }

    @Test
    void testDeleteUserLastAdmin() throws IOException {

        String deleteUserCommand = "delete-user --session-id testSessionId --username adminUser";
        String userIp = "1.1.1.1";

        Session session = new Session("testSessionId", "adminUser", true);
        when(sessionManager.getSessionBySessionId("testSessionId")).thenReturn(session);
        when(databaseManager.isLastAdmin()).thenReturn(true);


        Response response = userManager.parseCommand(deleteUserCommand, userIp);


        assertFalse(response.success());
        assertNotNull(response.message());
        verify(sessionManager).getSessionBySessionId("testSessionId");
        verify(databaseManager).isLastAdmin();
        verifyNoMoreInteractions(sessionManager, databaseManager, auditLog);    }


}
