package server;

import client.ClientArgs;
import com.google.gson.Gson;

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
    private static final Database database = new Database();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();


    public static void main(String[] args) {
        System.out.println("Server started!");
        while (true) {
            try (ServerSocket server = new ServerSocket(PORT, 50, InetAddress.getByName(SERVER_ADDRESS))) {
                Socket socket = server.accept();
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());

                String msg = input.readUTF();
                ClientArgs clientArgs = new Gson().fromJson(msg, ClientArgs.class);//deserialize the message
                if (clientArgs.getType().equals("exit")) {
                    output.writeUTF(new Gson().toJson(Map.of("response", "OK")));
                    System.exit(0);
                    executor.shutdown();

                }

                executor.submit(() -> {
                    try {
                        String reply = "";

                        switch (clientArgs.getType()) {
                            case "set" -> reply = database.setValue(clientArgs.getKey(), clientArgs.getValue());
                            case "get" -> reply = database.getValue(clientArgs.getKey());
                            case "delete" -> reply = database.deleteValue(clientArgs.getKey());

                        }
                        output.writeUTF(reply);

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




