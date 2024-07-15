import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

        // Uncomment this block to pass the first stage
        //
        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            serverSocket = new ServerSocket(4221);
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);
            clientSocket = serverSocket.accept();
            System.out.println("accepted new connection");
            InputStream input = clientSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String request = reader.readLine();
            String[] requestParts = request.split("\r\n");
            String requestLine = requestParts[0];
            String requestLinePath = requestLine.split(" ")[1];
            String[] pathParts = requestLinePath.split("/"); // /abc/hello/world -> ['', 'abc', 'hello', 'world', '']

            if (pathParts.length == 0) {
                clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
            }

            if (pathParts[1].equals("echo")) {
                clientSocket.getOutputStream().write(
                        ("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " +
                                pathParts[2].length() + "\r\n\r\n" + pathParts[2]).getBytes());
            }



            clientSocket.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());


        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
