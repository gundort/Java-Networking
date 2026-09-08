import java.io.*;
import java.net.*;

/**
 * A minimal mock SMTP server that prints received emails to the console.
 * Start this BEFORE running BirthdayReminder.
 */
public class MockServer {
    private static final int PORT = 2525;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Mock SMTP server listening on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("\n=== New SMTP connection ===");
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Mock SMTP server error: " + e.getMessage());
        }
    }

    private static void handleClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Send greeting (220)
            out.println("220 localhost Mock SMTP Server ready");
            System.out.println("S: 220 localhost Mock SMTP Server ready");

            boolean dataMode = false;
            StringBuilder emailData = new StringBuilder();

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("C: " + line);

                if (dataMode) {
                    if (line.equals(".")) {
                        // End of email data
                        dataMode = false;
                        out.println("250 OK: email received");
                        System.out.println("S: 250 OK: email received");
                        // Print the collected email
                        System.out.println("\n--- START EMAIL ---");
                        System.out.print(emailData.toString());
                        System.out.println("--- END EMAIL ---\n");
                        emailData.setLength(0); // clear for next email
                    } else {
                        emailData.append(line).append("\n");
                    }
                } else {
                    if (line.startsWith("HELO")) {
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
            System.err.println("Client handling error: " + e.getMessage());
        }
    }
}