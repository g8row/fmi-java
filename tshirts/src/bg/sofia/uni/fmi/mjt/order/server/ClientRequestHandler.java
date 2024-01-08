package bg.sofia.uni.fmi.mjt.order.server;

import bg.sofia.uni.fmi.mjt.order.server.repository.MJTOrderRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientRequestHandler implements Runnable {
    Socket socket;
    MJTOrderRepository repository;
    ClientRequestHandler(Socket socket, MJTOrderRepository repository) {
        this.socket = socket;
        this.repository = repository;
    }

    private Response parseCommand(String[] args) {
        int i = 0;
        switch (args[i++]) {
            case "request" -> {
                return repository.request(args[i++].split("=")[1],
                        args[i++].split("=")[1], args[i].split("=")[1]);
            }
            case "get" -> {
                switch (args[i++]) {
                    case "all" -> {
                        return repository.getAllOrders();
                    }
                    case "all-successful" -> {
                        return repository.getAllSuccessfulOrders();
                    }
                    case "my-order" -> {
                        return repository.getOrderById(Integer.parseInt(args[i].split("=")[1]));
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Client Request Handler for " + socket.getRemoteSocketAddress());
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // autoflush on
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) { // read the message from the client
                String[] args = inputLine.split("\\s+");
                Response response = null;
                try {
                    response = parseCommand(args);
                } catch (IndexOutOfBoundsException e) {
                    out.println("Unknown command");
                }
                if (response != null) {
                    out.println(response);
                } else {
                    out.println("Unknown command");
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
