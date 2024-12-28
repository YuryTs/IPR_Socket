package cvetkov.ipr;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SocketClient {
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
            System.out.printf("Client %d: connected to server%n", clientId);

            for (int idx = 0; idx < 3; idx++) {
                String msg = String.format("##%d - I Belive (Client %d)", idx, clientId);
                System.out.printf("Client %d: sending to server%n", clientId, msg);
                outputStream.println(msg);

                String response = inputStream.readLine();
                System.out.printf("Client %d: server response: %s%n", clientId, response);
                Thread.sleep(TimeUnit.SECONDS.toMillis(2));
            }

            System.out.printf("Client %d: stopping communication%n", clientId);
            outputStream.println("stop");
        } catch (Exception ex) {
            System.err.printf("Client %d: error occurred: %s%n", clientId, ex);
            ex.printStackTrace();
        }

    }
}
