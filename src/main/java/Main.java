import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static String headerValue(String headerName, List<String> headers) {
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

    private static String responseBuilder(String statusLine, String responseHeaders, String responseBody) {
        return statusLine + "\r\n" + responseHeaders + "\r\n\r\n" + responseBody;
    }

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

            List<String> headers = new ArrayList<>();
            String line;
            while (!(line = reader.readLine()).isEmpty()) {
                headers.add(line);
            }

            String root = pathParts.length == 0 ? "" : pathParts[1];

            switch (root) {
                case (""):
                    String responseRoot = responseBuilder("HTTP/1.1 200 OK", "", "");
                    clientSocket.getOutputStream().write(responseRoot.getBytes());
                    break;
                case ("echo"):
                    String responseEcho = responseBuilder("HTTP/1.1 200 OK","Content-Type: text/plain\n" +
                                    "Content-Length: "  + pathParts[2].length(), pathParts[2]);
                    clientSocket.getOutputStream().write(responseEcho.getBytes());
                    break;

                case ("user-agent"):
                    String userAgent = headerValue("user-agent", headers);
                    String responseUA = responseBuilder("HTTP/1.1 200 OK", "Content-Type: text/plain\n" +
                            "Content-Length: " + userAgent.length(), userAgent);
                    clientSocket.getOutputStream().write(responseUA.getBytes());
                    break;
                default:
                    clientSocket.getOutputStream().write(responseBuilder("HTTP/1.1 404 Not Found", "", "").getBytes());
            }



        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }


}
