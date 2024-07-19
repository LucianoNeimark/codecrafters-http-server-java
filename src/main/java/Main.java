import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class Main {
    static String headerValue(String headerName, List<String> headers) {
        for (String header: headers) {
            String[] parts = header.split(":");
            String name = parts[0].toLowerCase();
            String value = parts[1];
            if (name.equals(headerName.toLowerCase())) {
                return value.trim();
            }
        }
        return "";
    }



    static byte[] responseBuilder(String statusLine, String responseHeaders, String responseBody) {
        String response = statusLine + "\r\n" + responseHeaders + "\r\n\r\n" + responseBody;
        return response.getBytes();
    }

    static byte[] responseBuilderNoBody(String statusLine, String responseHeaders) {
        String responseString =  statusLine + "\r\n" + responseHeaders + "\r\n\r\n";
        return  responseString.getBytes();

    }


    public static void main(String[] args) {
        String directory = null;
        if (args.length > 1 && args[0].equals("--directory")) {
            directory = args[1];
        }

        System.out.println("Starting server...");
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New Client!");
                new Thread(new ClientHandler(clientSocket, directory)).start();
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

}
