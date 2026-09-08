/**
 * Practical Assignment 9 – SMTP Proxy with Newspeak Word Substitution
 * 
 * Listens on port 55555, forwards all traffic to a real SMTP server (port 8025).
 * Performs word substitutions (Newspeak) and appends a disclaimer.
 * If "Illuminati" is found, replaces the entire email with "Hello world".
 * 
 * Extra features:
 *   - Logging of all modifications to proxy.log
 *   - Statistics (emails processed, Illuminati hits, total substitutions)
 *   - Multi‑threaded client handling
 *   - Handles disconnections gracefully
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.*;

public class SmtpProxy {

    private static final int PROXY_PORT = 55555;
    private static final String SMTP_HOST = "localhost";
    private static final int SMTP_PORT = 8025;

    private static final Map<String, String> SUBSTITUTIONS = new LinkedHashMap<>();
    static {
        SUBSTITUTIONS.put("fast", "speedful");
        SUBSTITUTIONS.put("rapid", "speedful");
        SUBSTITUTIONS.put("quick", "speedful");
        SUBSTITUTIONS.put("slow", "unspeedful");
        SUBSTITUTIONS.put("ran", "runned");
        SUBSTITUTIONS.put("stole", "stealed");
        SUBSTITUTIONS.put("better", "gooder");
        SUBSTITUTIONS.put("best", "goodest");
        SUBSTITUTIONS.put("very good", "plusgood");
        SUBSTITUTIONS.put("very fast", "plusfast");
        SUBSTITUTIONS.put("very bad", "plusungood");
        SUBSTITUTIONS.put("bad", "ungood");
        SUBSTITUTIONS.put("warm", "uncold");
    }

    private static final String DISCLAIMER = "Please do not take anything in this email seriously!";
    private static final Pattern ILLUMINATI_PATTERN = Pattern.compile("\\bIlluminati\\b", Pattern.CASE_INSENSITIVE);
    private static final String LOG_FILE = "proxy.log";

    private static final AtomicInteger totalEmails = new AtomicInteger(0);
    private static final AtomicInteger illuminatiCount = new AtomicInteger(0);
    private static final AtomicInteger totalSubstitutions = new AtomicInteger(0);

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n=== Proxy Statistics ===");
            System.out.println("Total emails processed: " + totalEmails.get());
            System.out.println("Illuminati replacements: " + illuminatiCount.get());
            System.out.println("Total word substitutions: " + totalSubstitutions.get());
            System.out.println("========================\n");
        }));

        try (ServerSocket serverSocket = new ServerSocket(PROXY_PORT)) {
            System.out.println("SMTP Proxy listening on port " + PROXY_PORT);
            System.out.println("Forwarding to " + SMTP_HOST + ":" + SMTP_PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Proxy server error: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
            BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
            Socket serverSocket = new Socket(SMTP_HOST, SMTP_PORT);
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            PrintWriter serverOut = new PrintWriter(serverSocket.getOutputStream(), true)
        ) {
            // Read server greeting
            String serverGreeting = serverIn.readLine();
            if (serverGreeting == null) throw new IOException("Server closed connection");
            clientOut.println(serverGreeting);
            System.out.println("S: " + serverGreeting);

            boolean dataMode = false;
            StringBuilder emailBody = new StringBuilder();

            String line;
            while ((line = clientIn.readLine()) != null) {
                System.out.println("C: " + line);
                if (!dataMode) {
                    serverOut.println(line);
                    String serverResponse = serverIn.readLine();
                    if (serverResponse == null) break;
                    System.out.println("S: " + serverResponse);
                    clientOut.println(serverResponse);

                    if (line.toUpperCase().startsWith("DATA") && serverResponse.startsWith("354")) {
                        dataMode = true;
                        emailBody.setLength(0);
                    }
                } else {
                    if (line.equals(".")) {
                        String original = emailBody.toString();
                        String modified = processEmailBody(original);
                        serverOut.print(modified);
                        serverOut.flush();
                        serverOut.println(".");
                        String serverResponse = serverIn.readLine();
                        if (serverResponse == null) break;
                        System.out.println("S: " + serverResponse);
                        clientOut.println(serverResponse);
                        dataMode = false;
                    } else {
                        emailBody.append(line).append("\r\n");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Client handler error: " + e.getMessage());
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }

    private static String processEmailBody(String original) {
        totalEmails.incrementAndGet();

        Matcher m = ILLUMINATI_PATTERN.matcher(original);
        if (m.find()) {
            illuminatiCount.incrementAndGet();
            System.out.println("Illuminati detected – replacing email with 'Hello world'");
            String modified = "Hello world\r\n";
            logModification(original, modified);
            return modified;
        }

        // Count substitutions
        int subs = 0;
        for (String orig : SUBSTITUTIONS.keySet()) {
            Pattern p = Pattern.compile("\\b" + Pattern.quote(orig) + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = p.matcher(original);
            while (matcher.find()) subs++;
        }
        totalSubstitutions.addAndGet(subs);

        String modified = substituteWords(original);
        if (!modified.endsWith("\r\n")) modified += "\r\n";
        modified += DISCLAIMER + "\r\n";

        logModification(original, modified);
        return modified;
    }

    private static String substituteWords(String text) {
        String result = text;
        for (Map.Entry<String, String> entry : SUBSTITUTIONS.entrySet()) {
            String regex = "\\b" + Pattern.quote(entry.getKey()) + "\\b";
            Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            result = p.matcher(result).replaceAll(entry.getValue());
        }
        return result;
    }

    private static void logModification(String original, String modified) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println("=== " + new Date() + " ===");
            pw.println("ORIGINAL:\n" + original);
            pw.println("MODIFIED:\n" + modified);
            pw.println();
        } catch (IOException e) {
            System.err.println("Logging error: " + e.getMessage());
        }
    }
}