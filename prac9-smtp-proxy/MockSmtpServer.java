import java.io.*;
import java.net.*;

public class MockSmtpServer {
    private static final int PORT = 8025;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Mock SMTP server listening on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("220 localhost Mock SMTP Server ready");
            boolean dataMode = false;
            StringBuilder email = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("C: " + line);
                if (dataMode) {
                    if (line.equals(".")) {
                        dataMode = false;
                        out.println("250 OK: email received");
                        System.out.println("\n--- EMAIL ---\n" + email);
                        email.setLength(0);
                    } else {
                        email.append(line).append("\n");
                    }
                } else {
                    if (line.startsWith("HELO") || line.startsWith("EHLO")) {
                        out.println("250 Hello");
                    } else if (line.startsWith("MAIL FROM")) {
                        out.println("250 Sender OK");
                    } else if (line.startsWith("RCPT TO")) {
                        out.println("250 Recipient OK");
                    } else if (line.startsWith("DATA")) {
                        out.println("354 End data with <CR><LF>.<CR><LF>");
                        dataMode = true;
                    } else if (line.startsWith("QUIT")) {
                        out.println("221 Bye");
                        break;
                    } else {
                        out.println("500 Unrecognized command");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
