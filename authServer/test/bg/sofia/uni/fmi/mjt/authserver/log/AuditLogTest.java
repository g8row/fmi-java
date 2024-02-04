package bg.sofia.uni.fmi.mjt.authserver.log;

import bg.sofia.uni.fmi.mjt.authserver.log.AuditLog;
import bg.sofia.uni.fmi.mjt.authserver.server.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditLogTest {

    @TempDir
    Path tempDir;

    private AuditLog auditLog;

    @BeforeEach
    void setUp() throws IOException {
        auditLog = new AuditLog(tempDir.toString());
    }

    @Test
    void testLogLogin() throws IOException {

        LocalDateTime timestamp = LocalDateTime.now();
        String user = "testUser";
        String userIp = "127.0.0.1";


        auditLog.logLogin(timestamp, user, userIp);


        List<String> lines = Files.readAllLines(tempDir.resolve("log.txt"));
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains(timestamp.toString()));
        assertTrue(lines.get(0).contains(user));
        assertTrue(lines.get(0).contains(userIp));
    }

    @Test
    void testLogLogout() throws IOException {

        LocalDateTime timestamp = LocalDateTime.now();
        String user = "testUser";
        String userIp = "127.0.0.1";


        auditLog.logLogout(timestamp, user, userIp);


        List<String> lines = Files.readAllLines(tempDir.resolve("log.txt"));
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains(timestamp.toString()));
        assertTrue(lines.get(0).contains(user));
        assertTrue(lines.get(0).contains(userIp));
    }



    @Test
    void testLogError() throws IOException {

        String text = "Test error";
        Exception exception = new RuntimeException("Test exception");


        auditLog.logError(text, exception);


        List<String> lines = Files.readAllLines(tempDir.resolve(getDateHour() + ".txt"));
        assertEquals(3, lines.size());
        assertTrue(lines.get(0).contains(text));
        assertTrue(lines.get(1).contains(exception.getMessage()));
        assertTrue(lines.get(2).contains(exception.getStackTrace()[0].toString()));
    }

    @Test
    void testLogUnsuccessfulLogin() throws IOException {

        LocalDateTime timestamp = LocalDateTime.now();
        String user = "testUser";
        String userIp = "127.0.0.1";


        auditLog.logUnsuccessfulLogin(timestamp, user, userIp);


        List<String> lines = Files.readAllLines(tempDir.resolve("log.txt"));
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains(timestamp.toString()));
        assertTrue(lines.get(0).contains(user));
        assertTrue(lines.get(0).contains(userIp));
    }

    @Test
    void testLogCommandStart() throws IOException {

        LocalDateTime timestamp = LocalDateTime.now();
        String user = "testUser";
        String userIp = "127.0.0.1";
        String changes = "Changes";


        auditLog.logCommandStart(timestamp, Command.REGISTER, "type", user, userIp, changes);


        List<String> lines = Files.readAllLines(tempDir.resolve("log.txt"));
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains(timestamp.toString()));
        assertTrue(lines.get(0).contains(user));
        assertTrue(lines.get(0).contains(userIp));
        assertTrue(lines.get(0).contains(changes));
    }

    @Test
    void testLogCommandEnd() throws IOException {

        LocalDateTime timestamp = LocalDateTime.now();
        String user = "testUser";
        String userIp = "127.0.0.1";
        String result = "Success";


        auditLog.logCommandEnd(timestamp, Command.REGISTER, "type", user, userIp, result);


        List<String> lines = Files.readAllLines(tempDir.resolve("log.txt"));
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains(timestamp.toString()));
        assertTrue(lines.get(0).contains(user));
        assertTrue(lines.get(0).contains(userIp));
        assertTrue(lines.get(0).contains(result));
    }


    private String getDateHour() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd+HH-mm-ss"));
    }
}
