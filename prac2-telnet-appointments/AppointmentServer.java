/**
 * Practical Assignment 2: Telnet Appointment Server
 *
 */

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class AppointmentServer {

    private static List<Appointment> appointments = new CopyOnWriteArrayList<>();
    private static final String DATA_FILE = "appointments.txt";

    public static void main(String[] args) {
        int port = 8088;
        loadAppointments();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Appointment Server started on port " + port);
            System.out.println("Connect using: telnet localhost " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Each client gets its own thread, simultaneous users
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    // Load appointments from a text file
    private static void loadAppointments() {
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", 3);
                if (parts.length == 3) {
                    appointments.add(new Appointment(parts[0], parts[1], parts[2]));
                }
            }
        } catch (FileNotFoundException e) {
            // File will be created when first appointment is added
        } catch (IOException e) {
            System.err.println("Error loading appointments: " + e.getMessage());
        }
    }

    // Save all appointments to the file
    private static synchronized void saveAppointments() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (Appointment a : appointments) {
                pw.println(a.date + "|" + a.time + "|" + a.description);
            }
        } catch (IOException e) {
            System.err.println("Error saving appointments: " + e.getMessage());
        }
    }

    // Appointment record
    static class Appointment {
        final String date;
        final String time;
        final String description;

        Appointment(String date, String time, String description) {
            this.date = date;
            this.time = time;
            this.description = description;
        }

        @Override
        public String toString() {
            return date + " " + time + " – " + description;
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private InputStream in;
        private OutputStream out;
        private boolean running = true;

        private static final String ESC = "\u001B";
        private static final String CLEAR_SCREEN = ESC + "[2J";
        private static final String HOME = ESC + "[1;1H";
        private static final String RESET = ESC + "[0m";
        private static final String GREEN = ESC + "[32m";
        private static final String YELLOW = ESC + "[33m";
        private static final String CYAN = ESC + "[36m";
        private static final String RED = ESC + "[31m";

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();

                // Welcome screen
                sendAnsi(CLEAR_SCREEN + HOME);
                sendAnsi(GREEN);
                sendLine("   APPOINTMENT SERVER: COS 332");
                sendAnsi(RESET);
                sendLine("Type HELP for commands.\n");

                StringBuilder lineBuffer = new StringBuilder();
                while (running) {
                    int b = in.read();      
                    if (b == -1) break;     

                    if (b == 8 || b == 127) {
                        if (lineBuffer.length() > 0) {
                            lineBuffer.deleteCharAt(lineBuffer.length() - 1);
                            sendAnsi(ESC + "[D" + ESC + "[P");
                        }
                        continue;
                    }

                    out.write(b);
                    out.flush();

                    if (b == '\n' || b == '\r') {
                        String line = lineBuffer.toString().trim();
                        if (!line.isEmpty()) {
                            processCommand(line);
                        }

                        sendAnsi(CYAN);
                        send("> ");
                        sendAnsi(RESET);
                        lineBuffer.setLength(0);   
                    } else {

                        if (b != '\r' && b != '\n') {
                            lineBuffer.append((char) b);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Client error: " + e.getMessage());
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        private void processCommand(String cmdLine) {
            String[] parts = cmdLine.split(" ", 2);
            String command = parts[0].toUpperCase();

            switch (command) {
                case "ADD":
                    if (parts.length < 2) {
                        sendLine("Usage: ADD <date> <time> <description>");
                        break;
                    }

                    String[] args = parts[1].split(" ", 3);
                    if (args.length < 3) {
                        sendLine("Invalid format. Use: ADD dd/mm/yyyy hh:mm description");
                    } else {
                        String date = args[0];
                        String time = args[1];
                        String desc = args[2];
                        appointments.add(new Appointment(date, time, desc));
                        saveAppointments();     
                        sendLine(GREEN + "Appointment added." + RESET);
                    }
                    break;

                case "DELETE":
                    if (parts.length < 2) {
                        sendLine("Usage: DELETE <index> (use LIST to see indices)");
                        break;
                    }
                    try {
                        int index = Integer.parseInt(parts[1]) - 1; 
                        if (index >= 0 && index < appointments.size()) {
                            appointments.remove(index);
                            saveAppointments();
                            sendLine(GREEN + "Appointment deleted." + RESET);
                        } else {
                            sendLine(RED + "Invalid index." + RESET);
                        }
                    } catch (NumberFormatException e) {
                        sendLine("Please provide a valid number.");
                    }
                    break;

                case "SEARCH":
                    if (parts.length < 2) {
                        sendLine("Usage: SEARCH <keyword>");
                        break;
                    }
                    String keyword = parts[1].toLowerCase();
                    List<Appointment> results = new ArrayList<>();
                    for (Appointment a : appointments) {
                        if (a.description.toLowerCase().contains(keyword)) {
                            results.add(a);
                        }
                    }
                    if (results.isEmpty()) {
                        sendLine("No matching appointments.");
                    } else {
                        sendLine(YELLOW + "Matching appointments:" + RESET);
                        for (int i = 0; i < results.size(); i++) {
                            sendLine((i + 1) + ". " + results.get(i).toString());
                        }
                    }
                    break;

                case "LIST":
                    if (appointments.isEmpty()) {
                        sendLine("No appointments scheduled.");
                    } else {
                        sendLine(YELLOW + "Your appointments:" + RESET);
                        for (int i = 0; i < appointments.size(); i++) {
                            sendLine((i + 1) + ". " + appointments.get(i).toString());
                        }
                    }
                    break;

                case "HELP":
                    sendLine(CYAN + "Available commands:" + RESET);
                    sendLine(" ADD <date> <time> <description>   – e.g., ADD 25/12/2025 14:30 Christmas party");
                    sendLine(" DELETE <index>                     – delete by index (from LIST)");
                    sendLine(" SEARCH <keyword>                   – search in descriptions");
                    sendLine(" LIST                                – show all appointments");
                    sendLine(" HELP                                – this help");
                    sendLine(" EXIT                                – disconnect");
                    break;

                case "EXIT":
                    sendLine("Goodbye!");
                    running = false;
                    break;

                default:
                    sendLine("Unknown command. Type HELP.");
            }
        }


        private void send(String s) {
            try {
                out.write(s.getBytes());
                out.flush();
            } catch (IOException e) {
                running = false;
            }
        }

        private void sendLine(String s) {
            send(s + "\r\n");
        }

        private void sendAnsi(String code) {
            send(code);
        }
    }
}