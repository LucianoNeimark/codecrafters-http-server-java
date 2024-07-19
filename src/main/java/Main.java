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



    static String responseBuilder(String statusLine, String responseHeaders, String responseBody) {
        return statusLine + "\r\n" + responseHeaders + "\r\n\r\n" + responseBody;
    }

    public static String compressString(String data) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(data.getBytes("UTF-8"));
        }
        return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
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


    public static String responseBuilderBytes(String statusLine, String responseHeaders, byte[] gzipData) {
        return statusLine + "\r\n" + responseHeaders + "\r\n\r\n" + gzipData;

    }
}
