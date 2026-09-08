/**
 * Mock POP3 Server for Practical 7
 * 
 * Simulates a minimal POP3 server with two preloaded messages:
 *   1) Normal message with subject containing "prac7"
 *   2) Mailing list message with List-Id header
 * 
 * Username: testuser, Password: testpass
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class MockPop3Server {
    private static final int PORT = 1110;
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass";
    
    // Normal message – subject contains "prac7"
    private static final String NORMAL_MESSAGE = 
        "From: sender@example.com\r\n" +
        "To: testuser@localhost\r\n" +
        "Subject: Test prac7 message\r\n" +
        "Date: Thu, 1 Jan 2026 12:00:00 +0200\r\n" +
        "Message-ID: <12345@example.com>\r\n" +
        "\r\n" +
        "This is a normal test message body.\r\n";
    
    // Mailing list message – contains List-Id header
    private static final String MAILING_LIST_MESSAGE = 
        "From: list@example.com\r\n" +
        "To: testuser@localhost\r\n" +
        "Subject: Mailing list test (prac7)\r\n" +
        "List-Id: <test-list.example.com>\r\n" +
        "Date: Thu, 1 Jan 2026 12:00:00 +0200\r\n" +
        "Message-ID: <67890@example.com>\r\n" +
        "\r\n" +
        "This is a mailing list message.\r\n";

    private static List<String> messages = new ArrayList<>();
    private static Set<Integer> deleted = new HashSet<>();

    public static void main(String[] args) {
        messages.add(NORMAL_MESSAGE);
        messages.add(MAILING_LIST_MESSAGE);
        System.out.println("Mock POP3 server started on port " + PORT);
        System.out.println("Username: " + USERNAME + ", Password: " + PASSWORD);
        System.out.println("Pre‑loaded 2 messages:");
        System.out.println("  1. Normal message with subject 'Test prac7 message'");
        System.out.println("  2. Mailing list message with List-Id header");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
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
            
            out.println("+OK Mock POP3 server ready");
            boolean authenticated = false;
            boolean quit = false;
            String user = null;
            
            while (!quit) {
                String line = in.readLine();
                if (line == null) break;
                System.out.println("POP3 C: " + line);
                
                if (line.toUpperCase().startsWith("USER")) {
                    user = line.substring(5).trim();
                    out.println("+OK");
                } 
                else if (line.toUpperCase().startsWith("PASS")) {
                    String pass = line.substring(5).trim();
                    if (USERNAME.equals(user) && PASSWORD.equals(pass)) {
                        authenticated = true;
                        out.println("+OK Logged in");
                    } else {
                        out.println("-ERR Invalid credentials");
                    }
                }
                else if (!authenticated) {
                    out.println("-ERR Authentication required");
                }
                else if (line.toUpperCase().equals("STAT")) {
                    int total = messages.size() - deleted.size();
                    out.println("+OK " + total + " 0");
                }
                else if (line.toUpperCase().equals("LIST")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("+OK ");
                    for (int i = 1; i <= messages.size(); i++) {
                        if (!deleted.contains(i)) {
                            sb.append(i).append(" ").append(messages.get(i-1).length()).append("\r\n");
                        }
                    }
                    sb.append(".\r\n");
                    out.print(sb.toString());
                    out.flush();
                }
                else if (line.toUpperCase().startsWith("TOP")) {
                    String[] parts = line.split(" ");
                    if (parts.length < 3) {
                        out.println("-ERR usage: TOP msg n");
                        continue;
                    }
                    int msgNum = Integer.parseInt(parts[1]);
                    int numLines = Integer.parseInt(parts[2]);
                    if (deleted.contains(msgNum)) {
                        out.println("-ERR Message already deleted");
                    } else if (msgNum >= 1 && msgNum <= messages.size()) {
                        out.println("+OK");
                        String msg = messages.get(msgNum-1);
                        String[] msgLines = msg.split("\r\n");
                        int linesToSend = (numLines == 0) ? msgLines.length : Math.min(numLines, msgLines.length);
                        for (int j = 0; j < linesToSend; j++) {
                            out.println(msgLines[j]);
                        }
                        out.println(".");
                        out.flush();
                    } else {
                        out.println("-ERR No such message");
                    }
                }
                else if (line.toUpperCase().startsWith("RETR")) {
                    int msgNum = Integer.parseInt(line.substring(5).trim());
                    if (deleted.contains(msgNum)) {
                        out.println("-ERR Message already deleted");
                    } else if (msgNum >= 1 && msgNum <= messages.size()) {
                        out.println("+OK " + messages.get(msgNum-1).length() + " octets");
                        out.print(messages.get(msgNum-1));
                        out.print("\r\n.\r\n");
                        out.flush();
                    } else {
                        out.println("-ERR No such message");
                    }
                }
                else if (line.toUpperCase().startsWith("DELE")) {
                    int msgNum = Integer.parseInt(line.substring(5).trim());
                    if (msgNum >= 1 && msgNum <= messages.size()) {
                        deleted.add(msgNum);
                        out.println("+OK Message deleted");
                    } else {
                        out.println("-ERR No such message");
                    }
                }
                else if (line.toUpperCase().equals("QUIT")) {
                    out.println("+OK Bye");
                    quit = true;
                }
                else {
                    out.println("-ERR Unknown command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}