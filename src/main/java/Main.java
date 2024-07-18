import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static String headerValue(String headerName, List<String> headers) {
        for (String header: headers) {
            String[] parts = header.split(":");
            String name = parts[0].toLowerCase();
            String value = parts[1];
            if (name.equals(headerName)) {
                return value.trim();
            }
        }
        return "";
    }

    static String responseBuilder(String statusLine, String responseHeaders, String responseBody) {
        return statusLine + "\r\n" + responseHeaders + "\r\n\r\n" + responseBody;
    }

    public static void main(String[] args) {
        System.out.println("Starting server...");
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New Client!");
                new Thread(new ClientHandler(clientSocket)).start();
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }


}
