### Prac 6 – Birthday Reminder (`prac6-birthday-reminder/README.md`)

```markdown
# Birthday Reminder with Mock SMTP Server

A lightweight email-based birthday reminder system. The BirthdayReminder application reads events from a text file, checks which events occur today (or on a given date), and sends email notifications via SMTP. The MockServer is a dummy SMTP server that captures and prints outgoing emails to the console, allowing you to test the reminder system without a real mail server.

## Features

- Event management – reads events from `events.txt` (date, name, email, etc.)
- Email reminders – sends email notifications for events matching the current date.
- Mock SMTP server – a minimal server that logs all email traffic for debugging.
- Multi-threaded – the mock server handles multiple connections concurrently.
- Self-contained – no external libraries required (uses Java's built-in networking).

## Technologies

- Java (JDK 8+)
- Sockets (TCP/IP)
- SMTP protocol – the client (BirthdayReminder) communicates with the mock server using standard SMTP commands.
- Plain text file I/O – for reading event data.

## Prerequisites

- Java Runtime Environment (JRE) or JDK installed.
- Both the `BirthdayReminder` and `MockServer` must be compiled and run on the same machine (or the server must be reachable on the network).

## Setup & Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/birthday-reminder.git
   cd birthday-reminder
1. Compile both Java sources: bash  javac MockServer.java BirthdayReminder.java 
2. Prepare the events file – create events.txt with one event per line in the format: text  dd/MM/yyyy|recipient@example.com|Name|Message  Example: text  25/12/2025|john@doe.com|John Doe|Happy Birthday John!
3. 01/01/2026|alice@wonderland.com|Alice|Happy New Year! 
Usage
1. Start the Mock SMTP Server
First, start the mock server in one terminal:
bash
java MockServer
You should see:
text
Mock SMTP server listening on port 2525
The server will print all received SMTP commands and the full email content.
2. Run the Birthday Reminder
In another terminal, run the birthday reminder (you may need to pass a date or it may default to today's date):
bash
java BirthdayReminder
If the program reads events.txt, it will connect to the mock server at localhost:2525 and send emails for any matching events. The mock server will print the email content to its console.
Example output (MockServer):
text
=== New SMTP connection ===
C: HELO localhost
S: 250 Hello
C: MAIL FROM:<reminder@example.com>
S: 250 Sender OK
C: RCPT TO:<john@doe.com>
S: 250 Recipient OK
C: DATA
S: 354 End data with <CR><LF>.<CR><LF>
C: From: reminder@example.com
C: To: john@doe.com
C: Subject: Happy Birthday!
C: 
C: Happy Birthday John!
C: .
S: 250 OK: email received

--- START EMAIL ---
From: reminder@example.com
To: john@doe.com
Subject: Happy Birthday!

Happy Birthday John!
--- END EMAIL ---
3. Stop the server
Press Ctrl+C in the mock server terminal when done.
File Structure
text
birthday-reminder/
├── BirthdayReminder.java   # Main reminder client (assumed)
├── MockServer.java         # Mock SMTP server
├── events.txt              # (example) event data file
└── README.md               # This file

Notes / Caveats
* The mock server uses port 2525 by default. If you change the port, update the PORT constant in both the server and the client.
* The mock server is for development only – it does not deliver emails, only logs them.
* The BirthdayReminder client is not included in this repository (you need to write it or obtain it separately). This project focuses on the mock server for testing.
* If your events file uses a different format, adjust the parsing logic in the client accordingly.
* The server handles multiple connections in separate threads, but does not persist any data.
