package bg.sofia.uni.fmi.mjt.authserver.log;

import bg.sofia.uni.fmi.mjt.authserver.server.Command;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class AuditLog implements AuditLogApi {
    private final String logDir;
    private final String logPath;
    private static final String LOG_NAME = "/log.txt";

    public AuditLog(String logDir) throws IOException {
        this.logDir = logDir;
        this.logPath = logDir + LOG_NAME;
        createLogDirIfNotExists();
        createLogFileIfNotExists();
    }

    private void createLogDirIfNotExists() throws IOException {
        Path path = Paths.get(logDir);
        if (Files.notExists(path)) {
            Files.createDirectory(path);
        }
    }

    private void createLogFileIfNotExists() throws IOException {
        Path path = Paths.get(logPath);
        if (Files.notExists(path)) {
            Files.createFile(path);
        }
    }

    public void logLogin(LocalDateTime timestamp, String user, String userIp) throws IOException {
        try (FileWriter fileWriter = new FileWriter(Paths.get(logPath).toFile(), true)) {
            fileWriter.write(timestamp +
                    ": successful login to " + user + " from " + userIp + System.lineSeparator());
        }
    }

    public void logLogout(LocalDateTime timestamp, String user, String userIp) throws IOException {
        try (FileWriter fileWriter = new FileWriter(Paths.get(logPath).toFile(), true)) {
            fileWriter.write(timestamp +
                    ": logout of " + user + " from " + userIp + System.lineSeparator());
        }
    }

    public void logUnsuccessfulLogin(LocalDateTime timestamp, String user, String userIp) throws IOException {
        try (FileWriter fileWriter = new FileWriter(Paths.get(logPath).toFile(), true)) {
            fileWriter.write(timestamp + ": unsuccessful login to "
                    + user + " from " + userIp + System.lineSeparator());
        }
    }

    public void logCommandStart(LocalDateTime timestamp, Command command, String type,
                         String user, String userIp, String changes) throws IOException {
        try (FileWriter fileWriter = new FileWriter(Paths.get(logPath).toFile(), true)) {
            fileWriter.write(timestamp + ": " + user + " executed " + command.name() + " (" + type + ") by "
                    + userIp + " - changes were " + changes + System.lineSeparator());
        }
    }

    public void logCommandEnd(LocalDateTime timestamp, Command command, String type,
                       String user, String userIp, String result) throws IOException {
        try (FileWriter fileWriter = new FileWriter(Paths.get(logPath).toFile(), true)) {
            fileWriter.write(timestamp + ": " + user + " executed " + command.name() + " (" + type + ") by "
                    + userIp + " - result: " + result + System.lineSeparator());
        }
    }

    public void logError(String text, Exception e) throws IOException {
        try (FileWriter fileWriter =
                     new FileWriter(Paths.get(logDir + "/" + getDateHour() + ".txt").toFile())) {
            fileWriter.write(text + System.lineSeparator()
                    + e.getMessage() + System.lineSeparator()
                    + Arrays.toString(e.getStackTrace()));
        }
    }

    private String getDateHour() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd+HH-mm-ss")
                .format(LocalDateTime.now());
    }
}
