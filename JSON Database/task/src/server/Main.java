package server;

import com.google.gson.Gson;
import server.json.JsonDatabase;
import server.json.Request;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 34522;
    private static JsonDatabase database;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();


    public static void main(String[] args) throws IOException {
        database = new JsonDatabase();
        System.out.println("Server started!");
        while (true) {
            try (ServerSocket server = new ServerSocket(PORT, 50, InetAddress.getByName(SERVER_ADDRESS))) {
                Socket socket = server.accept();
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());

                String msg = input.readUTF();
                Request request = new Gson().fromJson(msg, Request.class);
                String type = request.getType();
                if (type.equals("exit")) {
                    output.writeUTF(new Gson().toJson(Map.of("response", "OK")));
                    System.exit(0);
                    executor.shutdown();
                }
                executor.submit(() -> {
                    try {
                        String resultFromDb = database.executeJson(msg);
                        output.writeUTF(resultFromDb);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}




