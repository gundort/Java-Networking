/**
 * Practical Assignment 7 – Vacation Responder
 * 
 * Checks a POP3 mailbox for new messages with subject containing "prac7".
 * Sends a vacation reply via SMTP to the original sender.
 * Avoids mailing lists and sends only once per sender.
 * 
 * Uses raw sockets for POP3 and SMTP – no external libraries.
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class VacationResponder {

    // POP3 server configuration (matches mock server)
    private static final String POP3_HOST = "localhost";
    private static final int POP3_PORT = 1110;
    private static final String POP3_USER = "testuser";
    private static final String POP3_PASS = "testpass";

    // SMTP server configuration (matches mock server)
    private static final String SMTP_HOST = "localhost";
    private static final int SMTP_PORT = 2525;
    private static final String FROM_EMAIL = "vacation@example.com";

    // File to remember replied senders
    private static final String SENT_FILE = "sent_senders.txt";

    // Vacation message
    private static final String VACATION_SUBJECT = "Out of office reply";
    private static final String VACATION_BODY =
        "I am currently away on vacation and will have limited access to email.\n" +
        "I will reply to your message when I return.\n\n" +
        "Thank you for your understanding.";

    public static void main(String[] args) {
        Set<String> repliedSenders = loadRepliedSenders();

        try {
            // Connect to POP3 server
            Socket pop3Socket = new Socket(POP3_HOST, POP3_PORT);
            BufferedReader pop3In = new BufferedReader(new InputStreamReader(pop3Socket.getInputStream()));
            PrintWriter pop3Out = new PrintWriter(pop3Socket.getOutputStream(), true);

            // Read greeting
            String line = pop3In.readLine();
            if (!line.startsWith("+OK")) throw new IOException("POP3 not ready");

            // USER
            pop3Out.println("USER " + POP3_USER);
            line = pop3In.readLine();
            if (!line.startsWith("+OK")) throw new IOException("USER failed");

            // PASS
            pop3Out.println("PASS " + POP3_PASS);
            line = pop3In.readLine();
            if (!line.startsWith("+OK")) throw new IOException("PASS failed");

            // STAT – get number of messages
            pop3Out.println("STAT");
            line = pop3In.readLine();
            if (!line.startsWith("+OK")) throw new IOException("STAT failed");
            String[] parts = line.split(" ");
            int msgCount = Integer.parseInt(parts[1]);
            System.out.println("Found " + msgCount + " message(s) in mailbox.");

            // For each message, retrieve headers and decide
            for (int i = 1; i <= msgCount; i++) {
                // Retrieve the message headers (TOP i 0)
                pop3Out.println("TOP " + i + " 0");
                
                // Read the +OK response line (discard it)
                line = pop3In.readLine();
                if (!line.startsWith("+OK")) {
                    System.err.println("TOP failed for message " + i);
                    continue;
                }
                
                // Now read the message lines until a line with just "."
                StringBuilder headers = new StringBuilder();
                while ((line = pop3In.readLine()) != null) {
                    if (line.equals(".")) break;
                    headers.append(line).append("\n");
                }
                String headerText = headers.toString();

                // Extract From, Subject, List-Id, Precedence
                String from = extractHeader(headerText, "From:");
                String subject = extractHeader(headerText, "Subject:");
                String listId = extractHeader(headerText, "List-Id:");
                String precedence = extractHeader(headerText, "Precedence:");

                if (from == null || subject == null) {
                    System.out.println("Message " + i + " missing From or Subject, skipping.");
                    continue;
                }

                System.out.println("Checking message " + i + " from: " + from);

                // Skip if it's a mailing list
                if (listId != null || (precedence != null && precedence.equalsIgnoreCase("list"))) {
                    System.out.println("  -> Mailing list detected, skipping.");
                    continue;
                }

                // Skip if subject does not contain "prac7"
                if (!subject.toLowerCase().contains("prac7")) {
                    System.out.println("  -> Subject does not contain 'prac7', skipping.");
                    continue;
                }

                // Extract sender email (from: "Name <email>" or just "email")
                String senderEmail = extractEmail(from);
                if (senderEmail == null) {
                    System.out.println("  -> Could not extract sender email, skipping.");
                    continue;
                }

                // Check if we already replied to this sender
                if (repliedSenders.contains(senderEmail)) {
                    System.out.println("  -> Already replied to " + senderEmail + ", skipping.");
                    continue;
                }

                // Send vacation reply
                System.out.println("  -> Sending vacation reply to " + senderEmail);
                if (sendVacationReply(senderEmail)) {
                    repliedSenders.add(senderEmail);
                    saveRepliedSenders(repliedSenders);
                    System.out.println("  -> Reply sent successfully.");
                } else {
                    System.out.println("  -> Failed to send reply.");
                }
            }

            // QUIT
            pop3Out.println("QUIT");
            pop3In.readLine();
            pop3Socket.close();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Extract a header value (first occurrence)
    private static String extractHeader(String headers, String headerName) {
        String[] lines = headers.split("\n");
        for (String line : lines) {
            if (line.startsWith(headerName)) {
                return line.substring(headerName.length()).trim();
            }
        }
        return null;
    }

    // Extract email address from "Name <email>" or "email"
    private static String extractEmail(String from) {
        if (from.contains("<") && from.contains(">")) {
            int start = from.indexOf('<');
            int end = from.indexOf('>');
            return from.substring(start + 1, end);
        }
        return from.trim();
    }

    // Send vacation reply via SMTP
    private static boolean sendVacationReply(String toEmail) {
        try (Socket socket = new Socket(SMTP_HOST, SMTP_PORT)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String line = in.readLine();
            if (!line.startsWith("220")) return false;

            out.println("HELO localhost");
            line = in.readLine();
            if (!line.startsWith("250")) return false;

            out.println("MAIL FROM:<" + FROM_EMAIL + ">");
            line = in.readLine();
            if (!line.startsWith("250")) return false;

            out.println("RCPT TO:<" + toEmail + ">");
            line = in.readLine();
            if (!line.startsWith("250")) return false;

            out.println("DATA");
            line = in.readLine();
            if (!line.startsWith("354")) return false;

            out.println("From: " + FROM_EMAIL);
            out.println("To: " + toEmail);
            out.println("Subject: " + VACATION_SUBJECT);
            out.println("");
            out.println(VACATION_BODY);
            out.println(".");
            out.flush();

            line = in.readLine();
            if (!line.startsWith("250")) return false;

            out.println("QUIT");
            in.readLine();
            return true;

        } catch (IOException e) {
            System.err.println("SMTP error: " + e.getMessage());
            return false;
        }
    }

    // Load list of already‑replied senders from file
    private static Set<String> loadRepliedSenders() {
        Set<String> set = new HashSet<>();
        File f = new File(SENT_FILE);
        if (!f.exists()) return set;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                set.add(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Could not load sent file: " + e.getMessage());
        }
        return set;
    }

    // Save list of replied senders to file
    private static void saveRepliedSenders(Set<String> set) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(SENT_FILE))) {
            for (String s : set) {
                pw.println(s);
            }
        } catch (IOException e) {
            System.err.println("Could not save sent file: " + e.getMessage());
        }
    }
}