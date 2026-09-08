
---

### Prac 2 - README.md
*(Place in `prac2-telnet-appointments/`)*

```markdown
# Appointment Server (Telnet)

A multi-user appointment management server. Clients connect via telnet and use simple text commands to add, delete, search, and list appointments. The server handles multiple simultaneous users with thread-per-client design and persists data to a plain text file.

## Features

- Add appointments (date, time, description)
- Delete appointments by index
- Search descriptions for keywords
- List all appointments with indices
- Persistent storage – appointments are saved to `appointments.txt` automatically
- ANSI colour output for a better terminal experience
- Concurrent – supports multiple clients at once

## Technologies

- Java (JDK 8+)
- Sockets (TCP/IP)
- Telnet for client connections
- ANSI escape codes for coloured terminal output

## Prerequisites

- Java Runtime Environment (JRE) or JDK installed.
- A terminal with telnet client (or any raw TCP client).

## Setup & Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/appointment-server.git
   cd appointment-server

4. Compile the Java source: bash  javac AppointmentServer.java 
6. Run the server: bash  java AppointmentServer  The server will start on port 8088 (default) and display: text  Appointment Server started on port 8088
7. Connect using: telnet localhost 8088 
Usage
1. Connect from another terminal:  telnet localhost 8088 
You'll see a welcome banner and a > prompt.
Type commands (case-insensitive) and press Enter.
Command	Example	Description
ADD <date> <time> <description>	ADD 25/12/2025 14:30 Christmas party	Adds a new appointment.
DELETE <index>	DELETE 2	Removes the appointment with the given index (from LIST).
SEARCH <keyword>	SEARCH party	Finds appointments whose description contains the keyword.
LIST	LIST	Shows all appointments with their indices.
HELP	HELP	Displays command reference.
EXIT	EXIT	Disconnects the client.
1. All changes are automatically saved to appointments.txt in the server's working directory.
2. Multiple clients can connect simultaneously; each gets its own thread.

File Structure

appointment-server/
├── AppointmentServer.java      # Main server + inner ClientHandler
├── appointments.txt            # (auto-generated) saved data
└── README.md                   # This file


Notes / Caveats
* The server uses ANSI escape codes for colours. If your terminal does not support them, you may see strange characters – you can safely ignore them or modify the code to disable ANSI.
* Backspace handling works for most terminals, but may behave differently on Windows (use a terminal like Git Bash or WSL for best results).
* The appointments.txt format is: date|time|description (one per line). You can edit it manually while the server is stopped.
* The default port is 8088; you can change it by editing the port variable in main().
