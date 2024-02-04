import bg.sofia.uni.fmi.mjt.authserver.server.Server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class MainServer {
    public static void main(String[] args) {
        Server server;
        try {
            server = new Server(7777, "data/db.txt",
                    "data/log", 5, TimeUnit.MINUTES.toMillis(1));
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

//login --username seshse --password seshse
//register --username tf --password tf --first-name tf --last-name tf --email tf
