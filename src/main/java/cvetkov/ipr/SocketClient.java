package cvetkov.ipr;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SocketClient {
    private static final Logger log = LoggerFactory.getLogger(SocketClient.class);
    public static void main(String[] args) {
        int numberOfClients = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfClients);

        for (int i = 0; i < numberOfClients; i++) {
            final int clientId = i;
            executor.submit(() -> runClient(clientId));
        }

        executor.shutdown();

        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }


    private static void runClient(int clientId) {
        try (Socket socket = new Socket("localhost", 8090);
             PrintWriter outputStream = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            log.info("Client {}: connected to server", clientId);

            for (int idx = 0; idx < 3; idx++) {
                String msg = String.format("## %d (Client %d response) -I Belive", idx, clientId);
                log.info(msg);
                outputStream.println(msg);

                String response = inputStream.readLine();
                log.info("Client {}: server response: {}", clientId, response);
                Thread.sleep(TimeUnit.SECONDS.toMillis(2));
            }

            log.info("Client {}: stopped communication", clientId);
            outputStream.println("stop");
        } catch (Exception ex) {
            log.info("Client {}: error occurred: {}", clientId, ex.getMessage());
            ex.printStackTrace();
        }

    }
}
