package bg.sofia.uni.fmi.mjt.order.server;

import bg.sofia.uni.fmi.mjt.order.server.repository.MJTOrderRepository;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderServer {

    private static final int SERVER_PORT = 4444;
    private static final int MAX_EXECUTOR_THREADS = 10;

    public static void main(String[] args) {
        MJTOrderRepository repository = new MJTOrderRepository();
        try (ExecutorService executor = Executors.newFixedThreadPool(MAX_EXECUTOR_THREADS)) {
            Thread.currentThread().setName("Order Server Thread");
            try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
                System.out.println("Server started and listening for connect requests");
                Socket clientSocket;
                while (true) {
                    clientSocket = serverSocket.accept();

                    System.out.println("Accepted connection request from client " + clientSocket.getInetAddress());
                    ClientRequestHandler clientHandler = new ClientRequestHandler(clientSocket, repository);

                    executor.execute(clientHandler); // use a thread pool to launch a thread
                }

            } catch (IOException e) {
                throw new RuntimeException("There is a problem with the server socket", e);
            }
        }
    }

}
