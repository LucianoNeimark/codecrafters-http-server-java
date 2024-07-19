import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

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
            String[] requestParts = request.split(" ");
            String resource = requestParts[1];
            String HTTPVerb = requestParts[0];
            String[] splitResource = resource.split("/"); // /abc/hello/world -> ['', 'abc', 'hello', 'world', '']

            List<String> headers = new ArrayList<>();
            String line;
            while (!(line = reader.readLine()).isEmpty()) {
                headers.add(line);
            }

            String contentLengthHeader = Main.headerValue("content-length", headers);
            int contentLength = contentLengthHeader.isEmpty() ? 0 : Integer.parseInt(contentLengthHeader);

            // Read the body if Content-Length is specified
            StringBuilder body = new StringBuilder();
            byte[] gzipData = null;
            if (contentLength > 0) {
                char[] buffer = new char[contentLength];
                reader.read(buffer, 0, contentLength);
                body.append(buffer);
            }


            String resourceRoot = splitResource.length == 0 ? "" : splitResource[1];

            String statusLine = "HTTP/1.1 404 Not Found";
            String responseHeaders = "";
            String responseBody = "";

            String baseHeader = "";

            String acceptEncodingHeaderValue = Main.headerValue("Accept-Encoding", headers);
            boolean gzip = false;

            if (!acceptEncodingHeaderValue.isEmpty()) {

                String[] encodings = acceptEncodingHeaderValue.split(", ");

                for (String e : encodings) {
                    if (e.equals("gzip")) {
                        gzip = true;
                        baseHeader = "Content-Encoding: " + e + "\r\n";
                    }
                }

            }

            if (HTTPVerb.equals("GET")) {
                switch (resourceRoot) {
                    case (""):
                        statusLine = "HTTP/1.1 200 OK";
                        break;
                    case ("echo"):

                        responseBody = splitResource[2];

                        if (gzip) {
                            ByteArrayOutputStream byteArrayOutputStream =
                                    new ByteArrayOutputStream();
                            try (GZIPOutputStream gzipOutputStream =
                                         new GZIPOutputStream(byteArrayOutputStream)) {
                                gzipOutputStream.write(responseBody.getBytes(StandardCharsets.UTF_8));
                            }
                            gzipData = byteArrayOutputStream.toByteArray();
                            contentLength = gzipData.length;
                            statusLine = "HTTP/1.1 200 OK";
                            responseHeaders = "Content-Type: text/plain\r\n" + "Content-Length: " + contentLength;
                            byte[] firstPart = Main.responseBuilderNoBody(statusLine, responseHeaders);
                            clientSocket.getOutputStream().write(firstPart);
                            clientSocket.getOutputStream().write(gzipData);



                        } else {
                            contentLength = responseBody.length();
                            statusLine = "HTTP/1.1 200 OK";
                            responseHeaders = "Content-Type: text/plain\r\n" + "Content-Length: " + contentLength;
                        }

                        break;

                    case ("user-agent"):
                        String userAgent = Main.headerValue("user-agent", headers);
                        statusLine = "HTTP/1.1 200 OK";
                        responseHeaders = "Content-Type: text/plain\r\n" + "Content-Length: " + userAgent.length();
                        responseBody = userAgent;
                        break;

                    case ("files"):
                        List<String> lines = List.of();
                        String absolutePath = directoryPath + "/" + splitResource[2];

                        try {
                            Path filePath = Paths.get(absolutePath);
                            if (!Files.exists(filePath)) {
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
                        statusLine = "HTTP/1.1 200 OK";
                        responseHeaders = "Content-Type: application/octet-stream\r\n" + "Content-Length: " + fileContent.length();
                        responseBody = fileContent.toString();
                        break;

                }
            }


            if (HTTPVerb.equals("POST")) {
                switch (resourceRoot) {
                    case ("files"):
                        Path directoryPath = Paths.get(this.directoryPath);
                        Path filePath = directoryPath.resolve(splitResource[2]);
                        String content = body.toString();
                        try {
                            // Ensure the directory exists
                            if (Files.notExists(directoryPath)) {
                                Files.createDirectories(directoryPath);
                            }

                            Files.write(filePath, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                            clientSocket.getOutputStream().write(Main.responseBuilder("HTTP/1.1 201 Created", "", ""));

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        break;
                }
            }

                byte[] response = Main.responseBuilder(statusLine, baseHeader + responseHeaders, responseBody);
                clientSocket.getOutputStream().write(response);

        } catch (Exception e) {
            System.out.println(e);
        }

    }
}
