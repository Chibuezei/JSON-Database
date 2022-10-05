package client;


import com.beust.jcommander.JCommander;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    private static ClientArgs jArgs;
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 34522;

    public static void main(String[] args) {
        jArgs = new ClientArgs();
        JCommander helloCmd = JCommander.newBuilder()
                .addObject(jArgs)
                .build();
        helloCmd.parse(args);
        run();
    }

    public static void run() {
        try (
                Socket socket = new Socket(InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            System.out.println("Client started!");

            String argsJson = getInput();
            output.writeUTF(argsJson); // sending message to the server
            String receivedMsg = input.readUTF(); // response message

            System.out.printf("Sent: %s\n" +
                    "Received: %s", argsJson, receivedMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getInput() {
        String textToRead = null;
        if (jArgs.getInput() == null) {
            textToRead = new Gson().toJson(jArgs);
        } else {
            String path = System.getProperty("user.dir") + "/src/client/data/" + jArgs.getInput();
//            String path = System.getProperty("user.dir") + "/JSON Database/task/src/client/data/" + jArgs.getInput();
            try {
                textToRead = Files.readString(Paths.get(path));
            } catch (IOException ex) {
                System.out.printf("save exception occurred %s", ex.getMessage());
            }
        }


        return textToRead;
    }
}
//use "src/client/data/" as a client data path, and "./src/server/data/" as a sd path.