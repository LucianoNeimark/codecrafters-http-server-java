import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream input = clientSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String request = reader.readLine();
            String resource = request.split(" ")[1];
            String[] splitResource = resource.split("/"); // /abc/hello/world -> ['', 'abc', 'hello', 'world', '']

            List<String> headers = new ArrayList<>();
            String line;
            while (!(line = reader.readLine()).isEmpty()) {
                headers.add(line);
            }

            String resourceRoot = splitResource.length == 0 ? "" : splitResource[1];

            switch (resourceRoot) {
                case (""):
                    String responseRoot = Main.responseBuilder("HTTP/1.1 200 OK", "", "");
                    clientSocket.getOutputStream().write(responseRoot.getBytes());
                    break;
                case ("echo"):
                    String responseEcho = Main.responseBuilder("HTTP/1.1 200 OK","Content-Type: text/plain\r\n" +
                            "Content-Length: "  + splitResource[2].length(), splitResource[2]);
                    clientSocket.getOutputStream().write(responseEcho.getBytes());
                    break;

                case ("user-agent"):
                    String userAgent = Main.headerValue("user-agent", headers);
                    String responseUA = Main.responseBuilder("HTTP/1.1 200 OK", "Content-Type: text/plain\r\n" +
                            "Content-Length: " + userAgent.length(), userAgent);
                    clientSocket.getOutputStream().write(responseUA.getBytes());
                    break;
                default:
                    clientSocket.getOutputStream().write(Main.responseBuilder("HTTP/1.1 404 Not Found", "", "").getBytes());
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }
}
