import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String directoryPath;

    public ClientHandler(Socket socket, String path) {
        this.clientSocket = socket;
        this.directoryPath = path;
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
                    String responseEcho = Main.responseBuilder("HTTP/1.1 200 OK", "Content-Type: text/plain\r\n" +
                            "Content-Length: " + splitResource[2].length(), splitResource[2]);
                    clientSocket.getOutputStream().write(responseEcho.getBytes());
                    break;

                case ("user-agent"):
                    String userAgent = Main.headerValue("user-agent", headers);
                    String responseUA = Main.responseBuilder("HTTP/1.1 200 OK", "Content-Type: text/plain\r\n" +
                            "Content-Length: " + userAgent.length(), userAgent);
                    clientSocket.getOutputStream().write(responseUA.getBytes());
                    break;

                case ("files"):
                    List<String> lines = List.of();
                    String absolutePath = directoryPath + "/" + splitResource[2];

                    try {
                        Path filePath = Paths.get(absolutePath);
                        if (!Files.exists(filePath)) {
                            clientSocket.getOutputStream().write(Main.responseBuilder("HTTP/1.1 404 Not Found", "", "").getBytes());
                            break;
                        }
                        lines = Files.readAllLines(filePath);
                    } catch (IOException e) {
                        clientSocket.getOutputStream().write("File not found".getBytes());
                    }

                    StringBuilder fileContent = new StringBuilder();
                    for (String fileLine : lines) {
                        fileContent.append(fileLine);
                    }
                    String responseFile = Main.responseBuilder("HTTP/1.1 200 OK", "Content-Type: application/octet-stream\r\n" +
                            "Content-Length: " + fileContent.length(), fileContent.toString());
                    clientSocket.getOutputStream().write(responseFile.getBytes());
                    break;
                default:
                    clientSocket.getOutputStream().write(Main.responseBuilder("HTTP/1.1 404 Not Found", "", "").getBytes());
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }
}
