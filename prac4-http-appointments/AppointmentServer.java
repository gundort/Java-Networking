/**
 * Practical Assignment 4 – Appointment Server with Image Support
 *
 * Requirements (COS 332):
 *   - Server maintains appointments (date, time, description, optional image)
 *   - All interaction via browser (no console after startup)
 *   - Supports insertion, searching, deletion
 *   - Extra challenge: transmit picture from server to browser using HTTP
 *
 * Extra features implemented:
 *   - Image upload
 *   - Images stored on server and served via /image?name=
 *   - Persistent storage (appointments.txt + images/ folder)
 *   - Multi‑threaded
 */

import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AppointmentServer {

    private static final int PORT = 8090;               // server port
    private static final String APPOINTMENTS_FILE = "appointments.txt";
    private static final String IMAGE_DIR = "images";   // folder for uploaded images

    // Thread‑safe appointment list
    private static final List<Appointment> APPOINTMENTS = new CopyOnWriteArrayList<>();
    private static int nextId = 1;                      // simple auto-increment ID

    // Helper to ensure image directory exists
    static {
        new File(IMAGE_DIR).mkdirs();
        loadAppointments();
    }

    // Appointment data class
    static class Appointment {
        final int id;
        final String date;        // yyyy-MM-dd
        final String time;        // HH:mm
        final String description;
        final String imageFile;   // filename (or null)

        Appointment(int id, String date, String time, String description, String imageFile) {
            this.id = id;
            this.date = date;
            this.time = time;
            this.description = description;
            this.imageFile = imageFile;
        }

        String formatDateTime() {
            return date + " " + time;
        }
    }

    // Load appointments from file
    private static void loadAppointments() {
        File f = new File(APPOINTMENTS_FILE);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", 5);
                if (parts.length >= 4) {
                    int id = Integer.parseInt(parts[0]);
                    String date = parts[1];
                    String time = parts[2];
                    String desc = parts[3];
                    String img = parts.length == 5 ? parts[4] : null;
                    APPOINTMENTS.add(new Appointment(id, date, time, desc, img));
                    if (id >= nextId) nextId = id + 1;
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading appointments: " + e.getMessage());
        }
    }

    // Save appointments to file (overwrites)
    private static synchronized void saveAppointments() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(APPOINTMENTS_FILE))) {
            for (Appointment a : APPOINTMENTS) {
                pw.print(a.id + "|" + a.date + "|" + a.time + "|" + a.description);
                if (a.imageFile != null) pw.print("|" + a.imageFile);
                pw.println();
            }
        } catch (IOException e) {
            System.err.println("Error saving appointments: " + e.getMessage());
        }
    }

    // Add a new appointment
    private static synchronized void addAppointment(String date, String time, String desc, String imageFile) {
        int id = nextId++;
        APPOINTMENTS.add(new Appointment(id, date, time, desc, imageFile));
        saveAppointments();
    }

    // Delete by ID, also remove associated image file
    private static synchronized boolean deleteAppointment(int id) {
        Iterator<Appointment> it = APPOINTMENTS.iterator();
        while (it.hasNext()) {
            Appointment a = it.next();
            if (a.id == id) {
                if (a.imageFile != null) {
                    File img = new File(IMAGE_DIR, a.imageFile);
                    if (img.exists()) img.delete();
                }
                it.remove();
                saveAppointments();
                return true;
            }
        }
        return false;
    }

    // Search by description (case‑insensitive substring)
    private static List<Appointment> searchAppointments(String query) {
        List<Appointment> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Appointment a : APPOINTMENTS) {
            if (a.description.toLowerCase().contains(lowerQuery)) {
                results.add(a);
            }
        }
        return results;
    }

    // -----------------------------------------------------------------
    // HTTP Server
    // -----------------------------------------------------------------
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("========================================");
            System.out.println("Appointment Server (with image upload)");
            System.out.println("========================================");
            System.out.println("Server running at: http://localhost:" + PORT);
            System.out.println("Press Ctrl+C to stop\n");

            while (true) {
                Socket client = server.accept();
                new Thread(new ClientHandler(client)).start();
            }
        } catch (BindException e) {
            System.err.println("Port " + PORT + " already in use. Change PORT in code.");
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------
    // Client handler (each request in its own thread)
    // -----------------------------------------------------------------
    static class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String method;
        private String path;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> params = new HashMap<>();

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream());

                // Parse request line
                String requestLine = in.readLine();
                if (requestLine == null) return;
                String[] parts = requestLine.split(" ");
                if (parts.length < 2) return;
                method = parts[0].toUpperCase();
                path = parts[1];

                // Parse headers
                String line;
                while (!(line = in.readLine()).isEmpty()) {
                    int colon = line.indexOf(':');
                    if (colon > 0) {
                        String key = line.substring(0, colon).trim().toLowerCase();
                        String value = line.substring(colon + 1).trim();
                        headers.put(key, value);
                    }
                }

                // Parse query parameters (GET)
                if (method.equals("GET") && path.contains("?")) {
                    String query = path.substring(path.indexOf("?") + 1);
                    path = path.substring(0, path.indexOf("?"));
                    parseQueryString(query, params);
                }

                // Route
                if (path.equals("/")) {
                    serveMainPage();
                } else if (path.equals("/add") && method.equals("POST")) {
                    handleAddPost();
                } else if (path.equals("/delete")) {
                    handleDelete();
                } else if (path.equals("/search")) {
                    handleSearch();
                } else if (path.equals("/image")) {
                    serveImage();
                } else {
                    sendNotFound();
                }

            } catch (IOException e) {
                System.err.println("Client error: " + e.getMessage());
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        private void parseQueryString(String query, Map<String, String> map) {
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    map.put(kv[0], decode(kv[1]));
                } else if (kv.length == 1) {
                    map.put(kv[0], "");
                }
            }
        }

        private String decode(String s) {
            try {
                return java.net.URLDecoder.decode(s, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return s;
            }
        }

        private void sendHtml(String html) {
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/html; charset=UTF-8");
            out.println("Cache-Control: no-cache, no-store, must-revalidate");
            out.println("Pragma: no-cache");
            out.println("Expires: 0");
            out.println();
            out.println(html);
            out.flush();
        }

        private void sendRedirect(String location) {
            out.println("HTTP/1.1 303 See Other");
            out.println("Location: " + location);
            out.println();
            out.flush();
        }

        private void sendNotFound() {
            out.println("HTTP/1.1 404 Not Found");
            out.println("Content-Type: text/plain");
            out.println();
            out.println("404 Not Found");
            out.flush();
        }

        private void sendBadRequest(String msg) {
            out.println("HTTP/1.1 400 Bad Request");
            out.println("Content-Type: text/plain");
            out.println();
            out.println(msg);
            out.flush();
        }

        // --- Main page: list all appointments with forms ---
        private void serveMainPage() {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html lang=\"en\">\n");
            html.append("<head>\n");
            html.append("  <meta charset=\"UTF-8\">\n");
            html.append("  <meta http-equiv=\"Cache-Control\" content=\"no-cache, no-store, must-revalidate\">\n");
            html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            html.append("  <title>Appointment Manager</title>\n");
            html.append("  <style>\n");
            html.append("    * {\n");
            html.append("      margin: 0;\n");
            html.append("      padding: 0;\n");
            html.append("      box-sizing: border-box;\n");
            html.append("    }\n");
            html.append("    body {\n");
            html.append("      background-color: #f5f7fa;\n");
            html.append("      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;\n");
            html.append("      line-height: 1.5;\n");
            html.append("      padding: 2rem 1rem;\n");
            html.append("      color: #1a1a2e;\n");
            html.append("    }\n");
            html.append("    .container {\n");
            html.append("      max-width: 1200px;\n");
            html.append("      margin: 0 auto;\n");
            html.append("    }\n");
            html.append("    h1 {\n");
            html.append("      font-size: 1.8rem;\n");
            html.append("      font-weight: 500;\n");
            html.append("      border-bottom: 2px solid #e2e8f0;\n");
            html.append("      padding-bottom: 0.5rem;\n");
            html.append("      margin-bottom: 2rem;\n");
            html.append("    }\n");
            html.append("    h2 {\n");
            html.append("      font-size: 1.4rem;\n");
            html.append("      font-weight: 500;\n");
            html.append("      margin-bottom: 1rem;\n");
            html.append("    }\n");
            html.append("    .card {\n");
            html.append("      background: white;\n");
            html.append("      border-radius: 12px;\n");
            html.append("      box-shadow: 0 4px 12px rgba(0,0,0,0.05);\n");
            html.append("      padding: 1.5rem;\n");
            html.append("      margin-bottom: 2rem;\n");
            html.append("    }\n");
            html.append("    .form-group {\n");
            html.append("      display: flex;\n");
            html.append("      flex-wrap: wrap;\n");
            html.append("      gap: 1rem;\n");
            html.append("      margin-bottom: 1rem;\n");
            html.append("      align-items: center;\n");
            html.append("    }\n");
            html.append("    .form-group label {\n");
            html.append("      width: 140px;\n");
            html.append("      font-weight: 500;\n");
            html.append("    }\n");
            html.append("    input, textarea, button, input[type=\"submit\"] {\n");
            html.append("      font-family: inherit;\n");
            html.append("      font-size: 0.9rem;\n");
            html.append("      padding: 0.5rem 0.75rem;\n");
            html.append("      border: 1px solid #cbd5e1;\n");
            html.append("      border-radius: 8px;\n");
            html.append("      background: white;\n");
            html.append("    }\n");
            html.append("    input:focus, textarea:focus {\n");
            html.append("      outline: none;\n");
            html.append("      border-color: #3b82f6;\n");
            html.append("      box-shadow: 0 0 0 3px rgba(59,130,246,0.1);\n");
            html.append("    }\n");
            html.append("    button, input[type=\"submit\"] {\n");
            html.append("      background-color: #3b82f6;\n");
            html.append("      color: white;\n");
            html.append("      border: none;\n");
            html.append("      cursor: pointer;\n");
            html.append("      transition: background-color 0.2s;\n");
            html.append("    }\n");
            html.append("    button:hover, input[type=\"submit\"]:hover {\n");
            html.append("      background-color: #2563eb;\n");
            html.append("    }\n");
            html.append("    table {\n");
            html.append("      width: 100%;\n");
            html.append("      border-collapse: collapse;\n");
            html.append("      margin-top: 1rem;\n");
            html.append("    }\n");
            html.append("    th, td {\n");
            html.append("      text-align: left;\n");
            html.append("      padding: 0.75rem;\n");
            html.append("      border-bottom: 1px solid #e2e8f0;\n");
            html.append("    }\n");
            html.append("    th {\n");
            html.append("      background-color: #f8fafc;\n");
            html.append("      font-weight: 600;\n");
            html.append("    }\n");
            html.append("    tr:hover {\n");
            html.append("      background-color: #f1f5f9;\n");
            html.append("    }\n");
            html.append("    .delete-link {\n");
            html.append("      color: #ef4444;\n");
            html.append("      text-decoration: none;\n");
            html.append("      font-size: 0.9rem;\n");
            html.append("    }\n");
            html.append("    .delete-link:hover {\n");
            html.append("      text-decoration: underline;\n");
            html.append("    }\n");
            html.append("    .thumbnail {\n");
            html.append("      max-width: 50px;\n");
            html.append("      max-height: 50px;\n");
            html.append("      border-radius: 6px;\n");
            html.append("      box-shadow: 0 1px 3px rgba(0,0,0,0.1);\n");
            html.append("    }\n");
            html.append("    .search-box {\n");
            html.append("      display: flex;\n");
            html.append("      gap: 0.5rem;\n");
            html.append("      margin-top: 0.5rem;\n");
            html.append("    }\n");
            html.append("    .search-box input {\n");
            html.append("      flex: 1;\n");
            html.append("    }\n");
            html.append("    hr {\n");
            html.append("      margin: 2rem 0;\n");
            html.append("      border: none;\n");
            html.append("      border-top: 1px solid #e2e8f0;\n");
            html.append("    }\n");
            html.append("    .footer {\n");
            html.append("      text-align: center;\n");
            html.append("      font-size: 0.8rem;\n");
            html.append("      color: #94a3b8;\n");
            html.append("      margin-top: 2rem;\n");
            html.append("    }\n");
            html.append("  </style>\n");
            html.append("</head>\n");
            html.append("<body>\n");
            html.append("<div class=\"container\">\n");
            html.append("  <h1>Appointment Manager</h1>\n");

            // Add Appointment Form
            html.append("  <div class=\"card\">\n");
            html.append("    <h2>Add Appointment</h2>\n");
            html.append("    <form action=\"/add\" method=\"post\" enctype=\"multipart/form-data\">\n");
            html.append("      <div class=\"form-group\">\n");
            html.append("        <label>Date (YYYY-MM-DD):</label>\n");
            html.append("        <input type=\"date\" name=\"date\" required>\n");
            html.append("      </div>\n");
            html.append("      <div class=\"form-group\">\n");
            html.append("        <label>Time (HH:MM):</label>\n");
            html.append("        <input type=\"time\" name=\"time\" required>\n");
            html.append("      </div>\n");
            html.append("      <div class=\"form-group\">\n");
            html.append("        <label>Description:</label>\n");
            html.append("        <textarea name=\"description\" rows=\"3\" cols=\"40\" required></textarea>\n");
            html.append("      </div>\n");
            html.append("      <div class=\"form-group\">\n");
            html.append("        <label>Picture (optional):</label>\n");
            html.append("        <input type=\"file\" name=\"picture\" accept=\"image/*\">\n");
            html.append("      </div>\n");
            html.append("      <div class=\"form-group\">\n");
            html.append("        <label></label>\n");
            html.append("        <input type=\"submit\" value=\"Add Appointment\">\n");
            html.append("      </div>\n");
            html.append("    </form>\n");
            html.append("  </div>\n");

            // Search Form
            html.append("  <div class=\"card\">\n");
            html.append("    <h2>Search</h2>\n");
            html.append("    <form action=\"/search\" method=\"get\">\n");
            html.append("      <div class=\"search-box\">\n");
            html.append("        <input type=\"text\" name=\"q\" placeholder=\"Enter keyword to search in descriptions\">\n");
            html.append("        <input type=\"submit\" value=\"Search\">\n");
            html.append("      </div>\n");
            html.append("    </form>\n");
            html.append("  </div>\n");

            // Appointment List
            html.append("  <div class=\"card\">\n");
            html.append("    <h2>All Appointments</h2>\n");
            if (APPOINTMENTS.isEmpty()) {
                html.append("    <p style=\"color: #64748b;\">No appointments yet.</p>\n");
            } else {
                html.append("    <div style=\"overflow-x: auto;\">\n");
                html.append("       <table>\n");
                html.append("        <thead>\n");
                html.append("           <tr>\n");
                html.append("            <th>ID</th><th>Date</th><th>Time</th><th>Description</th><th>Picture</th><th>Action</th>\n");
                html.append("           </tr>\n");
                html.append("        </thead>\n");
                html.append("        <tbody>\n");
                for (Appointment a : APPOINTMENTS) {
                    html.append("           <tr>\n");
                    html.append("             <td>").append(String.valueOf(a.id)).append("</td>\n");
                    html.append("             <td>").append(a.date).append("</td>\n");
                    html.append("             <td>").append(a.time).append("</td>\n");
                    html.append("             <td>").append(escapeHtml(a.description)).append("</td>\n");
                    html.append("             <td>");
                    if (a.imageFile != null) {
                        html.append("<a href=\"/image?name=").append(a.imageFile).append("\">");
                        html.append("<img class=\"thumbnail\" src=\"/image?name=").append(a.imageFile).append("\" alt=\"image\">");
                        html.append("</a>");
                    } else {
                        html.append("—");
                    }
                    html.append("</td>\n");
                    html.append("             <td><a class=\"delete-link\" href=\"/delete?id=").append(String.valueOf(a.id)).append("\">Delete</a></td>\n");
                    html.append("           </tr>\n");
                }
                html.append("        </tbody>\n");
                html.append("       </table>\n");
                html.append("    </div>\n");
            }
            html.append("  </div>\n");

            html.append("  <div class=\"footer\">\n");
            html.append("    Server‑side appointment manager | Image upload supported\n");
            html.append("  </div>\n");
            html.append("</div>\n");
            html.append("</body>\n");
            html.append("</html>");
            sendHtml(html.toString());
        }

        // POST /add : process multipart form data
        private void handleAddPost() throws IOException {
            String contentType = headers.get("content-type");
            if (contentType == null || !contentType.startsWith("multipart/form-data")) {
                sendBadRequest("Invalid content type");
                return;
            }

            // Extract boundary
            String boundary = "--" + contentType.split("boundary=")[1];
            byte[] boundaryBytes = boundary.getBytes();

            // Check Content-Length
            String cl = headers.get("content-length");
            if (cl == null) {
                sendBadRequest("Missing Content-Length");
                return;
            }
            int contentLength = Integer.parseInt(cl);

            // Increase timeout to handle larger uploads
            socket.setSoTimeout(30000); // 30 seconds

            // Read POST data
            byte[] postData = new byte[contentLength];
            int read = 0;
            try {
                InputStream socketIn = socket.getInputStream();
                while (read < contentLength) {
                    int r = socketIn.read(postData, read, contentLength - read);
                    if (r < 0) break;
                    read += r;
                }
                System.out.println("Read " + read + " of " + contentLength + " bytes for POST");
            } catch (SocketTimeoutException e) {
                sendBadRequest("Request timeout");
                return;
            }

            if (read != contentLength) {
                sendBadRequest("Incomplete data received");
                return;
            }

            Map<String, String> fields = new HashMap<>();
            String savedImageName = null;

            int pos = 0;
            while (pos < postData.length) {
                // Find next boundary
                int boundaryStart = indexOf(postData, boundaryBytes, pos);
                if (boundaryStart < 0) break;
                pos = boundaryStart + boundaryBytes.length;
                // Skip optional \r\n
                if (pos < postData.length && postData[pos] == '\r') pos++;
                if (pos < postData.length && postData[pos] == '\n') pos++;

                // Check for end marker (final boundary has "--" after it)
                if (pos + 1 < postData.length && postData[pos] == '-' && postData[pos+1] == '-') {
                    break;
                }

                // Find headers end
                int headersEnd = indexOf(postData, "\r\n\r\n".getBytes(), pos);
                if (headersEnd < 0) break;

                String partHeaders = new String(postData, pos, headersEnd - pos);
                String name = null, filename = null;

                // Parse name
                int nameIdx = partHeaders.indexOf("name=\"");
                if (nameIdx >= 0) {
                    int nameEnd = partHeaders.indexOf("\"", nameIdx + 6);
                    if (nameEnd > 0) name = partHeaders.substring(nameIdx + 6, nameEnd);
                }
                // Parse filename
                int filenameIdx = partHeaders.indexOf("filename=\"");
                if (filenameIdx >= 0) {
                    int filenameEnd = partHeaders.indexOf("\"", filenameIdx + 10);
                    if (filenameEnd > 0) filename = partHeaders.substring(filenameIdx + 10, filenameEnd);
                }

                pos = headersEnd + 4; // after blank line
                int dataStart = pos;

                int nextBoundary = indexOf(postData, boundaryBytes, dataStart);
                if (nextBoundary < 0) break;
                int dataEnd = nextBoundary;
                // Trim trailing \r\n
                if (dataEnd >= 2 && postData[dataEnd-2] == '\r' && postData[dataEnd-1] == '\n')
                    dataEnd -= 2;
                byte[] partData = Arrays.copyOfRange(postData, dataStart, dataEnd);

                if (filename != null && !filename.isEmpty()) {
                    // Save image file
                    String ext = "";
                    int dot = filename.lastIndexOf('.');
                    if (dot >= 0) ext = filename.substring(dot);
                    String savedName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0,8) + ext;
                    File outFile = new File(IMAGE_DIR, savedName);
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        fos.write(partData);
                    }
                    savedImageName = savedName;
                } else if (name != null) {
                    fields.put(name, new String(partData, java.nio.charset.StandardCharsets.UTF_8));
                }

                pos = nextBoundary;
            }

            String date = fields.get("date");
            String time = fields.get("time");
            String desc = fields.get("description");
            if (date == null || time == null || desc == null) {
                sendBadRequest("Missing fields");
                return;
            }
            if (!date.matches("\\d{4}-\\d{2}-\\d{2}") || !time.matches("\\d{2}:\\d{2}")) {
                sendBadRequest("Invalid date/time format");
                return;
            }

            addAppointment(date, time, desc, savedImageName);
            sendRedirect("/");
        }

        // Helper to find byte array pattern
        private int indexOf(byte[] haystack, byte[] needle, int start) {
            outer:
            for (int i = start; i <= haystack.length - needle.length; i++) {
                for (int j = 0; j < needle.length; j++) {
                    if (haystack[i + j] != needle[j]) continue outer;
                }
                return i;
            }
            return -1;
        }

        // --- GET /delete?id=... ---
        private void handleDelete() {
            String idStr = params.get("id");
            if (idStr == null) {
                sendBadRequest("Missing ID");
                return;
            }
            try {
                int id = Integer.parseInt(idStr);
                if (deleteAppointment(id)) {
                    sendRedirect("/");
                } else {
                    sendNotFound();
                }
            } catch (NumberFormatException e) {
                sendBadRequest("Invalid ID");
            }
        }

        // --- GET /search?q=... ---
        private void handleSearch() {
            String query = params.get("q");
            if (query == null || query.trim().isEmpty()) {
                sendRedirect("/");
                return;
            }
            List<Appointment> results = searchAppointments(query);

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html lang=\"en\">\n");
            html.append("<head>\n");
            html.append("  <meta charset=\"UTF-8\">\n");
            html.append("  <title>Search Results</title>\n");
            html.append("  <style>\n");
            html.append("    body { font-family: system-ui, sans-serif; max-width: 900px; margin: 2rem auto; padding: 0 1rem; background: #f5f7fa; }\n");
            html.append("    .card { background: white; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); padding: 1.5rem; }\n");
            html.append("    h1 { font-size: 1.8rem; font-weight: 500; margin-bottom: 1rem; }\n");
            html.append("    table { width: 100%; border-collapse: collapse; margin-top: 1rem; }\n");
            html.append("    th, td { text-align: left; padding: 0.75rem; border-bottom: 1px solid #e2e8f0; }\n");
            html.append("    th { background: #f8fafc; }\n");
            html.append("    .delete-link { color: #ef4444; text-decoration: none; }\n");
            html.append("    .delete-link:hover { text-decoration: underline; }\n");
            html.append("    .back-link { display: inline-block; margin-top: 1.5rem; color: #3b82f6; text-decoration: none; }\n");
            html.append("    .back-link:hover { text-decoration: underline; }\n");
            html.append("  </style>\n");
            html.append("</head>\n");
            html.append("<body>\n");
            html.append("<div class=\"card\">\n");
            html.append("  <h1>Search results for \"").append(escapeHtml(query)).append("\"</h1>\n");
            if (results.isEmpty()) {
                html.append("  <p>No appointments found.</p>\n");
            } else {
                html.append("  <div style=\"overflow-x: auto;\">\n");
                html.append("     <table>\n");
                html.append("      <thead><tr><th>ID</th><th>Date</th><th>Time</th><th>Description</th><th>Picture</th><th>Action</th></tr></thead>\n");
                html.append("      <tbody>\n");
                for (Appointment a : results) {
                    html.append("         <tr>\n");
                    html.append("           <td>").append(String.valueOf(a.id)).append("</td>\n");
                    html.append("           <td>").append(a.date).append("</td>\n");
                    html.append("           <td>").append(a.time).append("</td>\n");
                    html.append("           <td>").append(escapeHtml(a.description)).append("</td>\n");
                    html.append("           <td>");
                    if (a.imageFile != null) {
                        html.append("<a href=\"/image?name=").append(a.imageFile).append("\"><img class=\"thumbnail\" src=\"/image?name=").append(a.imageFile).append("\" width=\"40\" style=\"border-radius:4px\"></a>");
                    } else {
                        html.append("—");
                    }
                    html.append("</td>\n");
                    html.append("           <td><a class=\"delete-link\" href=\"/delete?id=").append(String.valueOf(a.id)).append("\">Delete</a></td>\n");
                    html.append("         </tr>\n");
                }
                html.append("      </tbody>\n");
                html.append("     </table>\n");
                html.append("  </div>\n");
            }
            html.append("  <a class=\"back-link\" href=\"/\">← Back to main page</a>\n");
            html.append("</div>\n");
            html.append("</body>\n");
            html.append("</html>");
            sendHtml(html.toString());
        }

        // --- GET /image?name=filename ---
        private void serveImage() {
            String name = params.get("name");
            if (name == null || name.isEmpty() || name.contains("..") || name.contains("/")) {
                sendNotFound();
                return;
            }
            File img = new File(IMAGE_DIR, name);
            if (!img.exists()) {
                sendNotFound();
                return;
            }
            // Determine MIME type by extension
            String mime = "image/jpeg";
            if (name.endsWith(".png")) mime = "image/png";
            else if (name.endsWith(".gif")) mime = "image/gif";

            try (FileInputStream fis = new FileInputStream(img)) {
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: " + mime);
                out.println("Content-Length: " + img.length());
                out.println();
                out.flush();
                // Write binary to socket output stream
                byte[] buffer = new byte[4096];
                int bytesRead;
                OutputStream socketOut = socket.getOutputStream();
                while ((bytesRead = fis.read(buffer)) != -1) {
                    socketOut.write(buffer, 0, bytesRead);
                }
                socketOut.flush();
            } catch (IOException e) {
                // error already logged, just ignore
            }
        }

        private String escapeHtml(String s) {
            if (s == null) return "";
            return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                    .replace("\"", "&quot;").replace("'", "&#39;");
        }
    }
}