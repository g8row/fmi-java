import bg.sofia.uni.fmi.mjt.authserver.server.Server;

import java.io.IOException;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        Server server;
        try {
            server = new Server("data/db.txt",
                    "data/log");
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}