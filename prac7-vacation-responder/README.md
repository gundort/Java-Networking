### Prac 7 – Vacation Responder (`prac7-vacation-responder/README.md`)

```markdown
# Vacation Responder (POP3 + SMTP)

An automated email vacation responder that checks a POP3 mailbox for messages with a specific subject keyword, filters out mailing list messages, and sends an out-of-office reply to individual senders – without sending duplicate replies. The system uses raw sockets for both POP3 and SMTP communication (no external email libraries).

## Features

- POP3 mailbox checking – connects to a POP3 server, authenticates, and retrieves message headers.
- Subject filtering – only processes messages whose Subject: header contains the keyword "prac7".
- Mailing list detection – skips messages with a List-Id header or Precedence: list, preventing replies to mailing lists.
- Duplicate prevention – keeps a record of replied senders in `sent_senders.txt` to avoid sending multiple replies to the same person.
- SMTP reply sending – sends a vacation email to the original sender.
- Mock servers included – a minimal POP3 server and SMTP server are provided for testing without a real email infrastructure.

## Technologies

- Java (JDK 8+)
- Sockets (TCP/IP)
- POP3 protocol – for retrieving email headers
- SMTP protocol – for sending vacation replies
- Plain text file I/O – for persistent sender tracking

## Prerequisites

- Java Runtime Environment (JRE) or JDK installed.
- A POP3 server accessible on `localhost:1110` (or the provided `MockPop3Server`).
- An SMTP server accessible on `localhost:2525` (or the provided `MockSmtpServer`).
- The POP3 server must contain messages with a Subject: containing "prac7" for the responder to trigger.

## Setup & Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/vacation-responder.git
   cd vacation-responder
Compile all Java sources:

bash
javac VacationResponder.java MockPop3Server.java MockSmtpServer.java
Usage

1. Start the Mock Servers

First, start the mock POP3 and SMTP servers in two separate terminals:

Terminal 1 – POP3 Server:

bash
java MockPop3Server
You should see:

text
Mock POP3 server started on port 1110
Username: testuser, Password: testpass
Pre-loaded 2 messages:
  1. Normal message with subject 'Test prac7 message'
  2. Mailing list message with List-Id header
Terminal 2 – SMTP Server:

bash
java MockSmtpServer
You should see:

text
Mock SMTP server listening on port 2525
2. Run the Vacation Responder

In a third terminal, run the vacation responder:

bash
java VacationResponder
The program will:

Connect to the POP3 server and authenticate (testuser / testpass).
Retrieve headers for each message using the TOP command.
For each message:

Extract the From, Subject, List-Id, and Precedence headers.
Skip if it's a mailing list (has List-Id or Precedence: list).
Skip if the Subject does not contain "prac7".
Skip if the sender has already received a reply (checked against sent_senders.txt).
Otherwise, send a vacation reply via SMTP and save the sender to sent_senders.txt.
Example output:

text
Found 2 message(s) in mailbox.
Checking message 1 from: sender@example.com
  -> Sending vacation reply to sender@example.com
  -> Reply sent successfully.
Checking message 2 from: list@example.com
  -> Mailing list detected, skipping.
3. View the Email

The SMTP server will print the sent email to its console:

text
--- START EMAIL ---
From: vacation@example.com
To: sender@example.com
Subject: Out of office reply

I am currently away on vacation and will have limited access to email.
I will reply to your message when I return.

Thank you for your understanding.
--- END EMAIL ---
4. Stop the Servers

Press Ctrl+C in each mock server terminal when done.

File Structure

text
vacation-responder/
├── VacationResponder.java   # Main application
├── MockPop3Server.java      # Mock POP3 server for testing
├── MockSmtpServer.java      # Mock SMTP server for testing
├── sent_senders.txt         # (auto-generated) list of replied senders
└── README.md                # This file
Notes / Caveats

The POP3 server uses hard-coded credentials: testuser / testpass. If you use a real POP3 server, update the POP3_USER and POP3_PASS constants in VacationResponder.java.
The SMTP server does not actually deliver emails – it only logs them. This is intentional for testing.
The program does not delete messages from the POP3 server; it only reads them.
The sent_senders.txt file persists across runs, preventing duplicate replies even if the program is restarted.
The keyword filter ("prac7") is case-insensitive and matches anywhere in the subject line.
All communication uses raw sockets – no javax.mail or other libraries are used, meeting the assignment requirements.
