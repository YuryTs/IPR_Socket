package cvetkov.ipr;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SocketServer {
    private static final Logger log = LoggerFactory.getLogger(SocketServer.class);
    private static final int PORT = 8090;
    private static final int THREAD_POOL_SIZE = 5;

    private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static void main(String[] args) {
        SocketServer server = new SocketServer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
        }));
        server.go();
    }

    private void go() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log.info("Server started on port {}", PORT);
            while (!Thread.currentThread().isInterrupted()) {
                log.info("Waiting for client connection");
                Socket clientSocket = serverSocket.accept();
                log.info("client connected {}", clientSocket.getRemoteSocketAddress());
                executorService.submit(() -> handleClientConnection(clientSocket));
            }
        } catch (IOException ex) {
            log.error("Server error", ex);
        }
    }
    private void handleClientConnection(Socket clientSocket) {
        try (PrintWriter outputStream = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            String request;
            while ((request = inputStream.readLine()) != null && !"stop".equals(request)) {
                log.info("from client: {}", request);
                outputStream.println(request + "-I Can Fly!-");
            }
        } catch (IOException ex) {
            log.error("Error handling client connection", ex);
        } finally {
            log.info("Client disconnected: {}", clientSocket.getRemoteSocketAddress());
        }
    }

    private void shutdown() {
        log.info("Shutting down server...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

