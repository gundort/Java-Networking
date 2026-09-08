# ☕ Java Networking Projects – COS 332

A comprehensive portfolio of **pure Java** networking assignments from my Computer Networks module (COS 332).  
Every project is built from scratch using **standard Java sockets and I/O** – no external libraries, no frameworks – just raw TCP/IP, protocol implementations, and multithreading.

---

## 📡 Project List (All Java + Networking)

| Practical | Description | Java Networking Concepts |
|-----------|-------------|---------------------------|
| [Prac 1](./prac1-cgi-game/) | CGI web game – click the larger number | Java + CGI, HTTP headers, Apache integration |
| [Prac 2](./prac2-telnet-appointments/) | Telnet-based appointment server with ANSI colours | Raw TCP sockets, Telnet protocol, multithreading |
| [Prac 4](./prac4-http-appointments/) | HTTP appointment server with image uploads | HTTP/1.1, multipart form parsing, MIME types |
| [Prac 5](./prac5-ldap-client/) | LDAP asset speed lookup client | ProcessBuilder, `ldapsearch`, LDAP protocol |
| [Prac 6](./prac6-birthday-reminder/) | Birthday reminder with mock SMTP server | SMTP protocol, socket streams, email formatting |
| [Prac 7](./prac7-vacation-responder/) | Vacation responder (POP3 + SMTP) | POP3 + SMTP, header parsing, state machines |
| [Prac 8](./prac8-ftp-backup/) | Directory watcher + FTP backup | FTP protocol (PASV), WatchService, file I/O |
| [Prac 9](./prac9-smtp-proxy/) | SMTP proxy with Newspeak word substitution | SMTP proxying, regex, logging, multithreading |

---

## 🧰 Common Tech Stack Across All Projects

- **Language:** Java (JDK 8+)
- **Networking:** `java.net.Socket`, `ServerSocket`, `BufferedReader`/`PrintWriter`
- **Concurrency:** `Thread` per client (all servers are multi‑threaded)
- **Protocols implemented:** HTTP, FTP, SMTP, POP3, CGI, LDAP (via system call)
- **Data persistence:** Plain text files (no databases)
- **Testing:** Mock servers for SMTP/POP3 to simulate real environments

---

## 🚀 How to Run Any Project (Generic Steps)

1. Navigate to the project folder:
   ```bash
   cd pracX-project-name
