import bg.sofia.uni.fmi.mjt.authserver.client.Client;

import java.io.IOException;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class MainClient {
    public static void main(String[] args) throws IOException {
        Client client = new Client(7777, "data/clientLogs");
        client.start();
    }
}

//login --username seshse --password seshse
//register --username tf --password tf --first-name tf --last-name tf --email tf
