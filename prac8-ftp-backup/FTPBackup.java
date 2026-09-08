/**
 * Practical Assignment 8 – FTP Backup
 * 
 * Monitors a local directory and uploads changed files to an FTP server.
 * Uses raw FTP protocol (no libraries). Extra: deletes remote file when local file is deleted.
 */

import java.io.*;
import java.net.*;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;

public class FTPBackup {

    // FTP server configuration
    private static final String FTP_HOST = "localhost";
    private static final int FTP_PORT = 21;
    private static final String FTP_USER = "ftpuser";
    private static final String FTP_PASS = "password";   // change to your password
    private static final String REMOTE_DIR = "/";        // remote directory (e.g., /home/ftpuser/ftp)

    // Local directory to watch
    private static final String WATCH_DIR = "./watchdir";

    public static void main(String[] args) throws Exception {
        // Create the watch directory if it doesn't exist
        Path watchPath = Paths.get(WATCH_DIR);
        if (!Files.exists(watchPath)) {
            Files.createDirectories(watchPath);
            System.out.println("Created watch directory: " + watchPath.toAbsolutePath());
        }

        // Start watching
        System.out.println("Watching directory: " + watchPath.toAbsolutePath());
        WatchService watcher = FileSystems.getDefault().newWatchService();
        watchPath.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

        while (true) {
            WatchKey key = watcher.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == OVERFLOW) continue;

                Path filename = (Path) event.context();
                Path fullPath = watchPath.resolve(filename);
                String remoteFile = REMOTE_DIR + "/" + filename.toString();

                if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
                    System.out.println("Detected: " + kind.name() + " " + filename);
                    uploadFile(fullPath.toFile(), remoteFile);
                } else if (kind == ENTRY_DELETE) {
                    System.out.println("Detected: DELETE " + filename);
                    deleteRemoteFile(remoteFile);
                }
            }
            key.reset();
        }
    }

    // Upload a file to FTP server
    private static void uploadFile(File localFile, String remotePath) {
        try (Socket socket = new Socket(FTP_HOST, FTP_PORT)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // Read welcome message
            String response = reader.readLine();
            if (!response.startsWith("220")) throw new IOException("FTP not ready: " + response);

            // Login
            sendCommand(writer, "USER " + FTP_USER, reader, "331");
            sendCommand(writer, "PASS " + FTP_PASS, reader, "230");

            // Set binary mode
            sendCommand(writer, "TYPE I", reader, "200");

            // Passive mode
            sendCommand(writer, "PASV", reader, "227");
            String pasvResp = reader.readLine();
            String[] hostPort = parsePassive(pasvResp);
            String dataHost = hostPort[0];
            int dataPort = Integer.parseInt(hostPort[1]);

            // Send STOR command
            sendCommand(writer, "STOR " + remotePath, reader, "150");

            // Open data connection and send file
            try (Socket dataSocket = new Socket(dataHost, dataPort);
                 FileInputStream fis = new FileInputStream(localFile);
                 OutputStream dataOut = dataSocket.getOutputStream()) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dataOut.write(buffer, 0, bytesRead);
                }
                dataOut.flush();
                dataSocket.close();
            }

            // Read final response after transfer
            String finalResp = reader.readLine();
            if (finalResp.startsWith("226")) {
                System.out.println("Upload successful: " + localFile.getName());
            } else {
                System.out.println("Upload failed: " + finalResp);
            }

            // Quit
            sendCommand(writer, "QUIT", reader, "221");
        } catch (IOException e) {
            System.err.println("Upload error for " + localFile.getName() + ": " + e.getMessage());
        }
    }

    // Delete remote file
    private static void deleteRemoteFile(String remotePath) {
        try (Socket socket = new Socket(FTP_HOST, FTP_PORT)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            reader.readLine(); // welcome
            sendCommand(writer, "USER " + FTP_USER, reader, "331");
            sendCommand(writer, "PASS " + FTP_PASS, reader, "230");
            sendCommand(writer, "DELE " + remotePath, reader, "250");
            sendCommand(writer, "QUIT", reader, "221");
            System.out.println("Deleted remote file: " + remotePath);
        } catch (IOException e) {
            System.err.println("Deletion error: " + e.getMessage());
        }
    }

    // Helper: send command and check expected response code
    private static void sendCommand(PrintWriter writer, String cmd, BufferedReader reader, String expectedCode) throws IOException {
        writer.println(cmd);
        String resp = reader.readLine();
        if (!resp.startsWith(expectedCode)) {
            throw new IOException("Unexpected response for " + cmd + ": " + resp);
        }
    }

    // Parse PASV response (227 Entering Passive Mode (h1,h2,h3,h4,p1,p2))
    private static String[] parsePassive(String pasvResp) {
        int start = pasvResp.indexOf('(');
        int end = pasvResp.indexOf(')');
        String[] parts = pasvResp.substring(start + 1, end).split(",");
        String host = parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];
        int port = (Integer.parseInt(parts[4]) << 8) + Integer.parseInt(parts[5]);
        return new String[]{host, String.valueOf(port)};
    }
}