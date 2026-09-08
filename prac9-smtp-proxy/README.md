### Prac 9 – SMTP Proxy 

# SMTP Proxy (Newspeak Word Substitution)

An SMTP proxy that sits between an email client and an SMTP server. It intercepts email content, applies a set of word substitutions (in the spirit of Orwell's Newspeak), appends a disclaimer, and logs all changes. If the email contains the word "Illuminati", the entire message is replaced with "Hello world". The proxy is multi-threaded, handles multiple clients, and keeps statistics.

## Features

- Word substitution – replaces common English words with Newspeak equivalents (e.g., "fast" -> "speedful", "bad" -> "ungood").
- Disclaimer – appends a fixed disclaimer to every email body.
- Illuminati detection – if the word "Illuminati" appears anywhere in the email, the entire body is replaced with "Hello world" (case-insensitive).
- Logging – saves the original and modified versions of each email to `proxy.log` with timestamps.
- Statistics – tracks total emails processed, Illuminati hits, and total word substitutions (displayed on shutdown).
- Multi-threaded – handles multiple client connections concurrently.
- Transparent proxying – forwards all SMTP commands between client and server, only interfering during the data phase.

## Technologies

- Java (JDK 8+)
- Sockets (TCP/IP)
- SMTP protocol – raw implementation (no external libraries)
- Regular expressions – for word boundary matching and Illuminati detection

## Prerequisites

- Java Runtime Environment (JRE) or JDK installed.
- An SMTP server accessible on `localhost:8025` (or the provided `MockSmtpServer`).
- The proxy listens on port `55555` – ensure this port is free.

## Setup & Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/smtp-proxy.git
   cd smtp-proxy
4. Compile all Java sources: bash  javac SmtpProxy.java MockSmtpServer.java 
6. Start the mock SMTP server (or your real server) on port 8025: bash  java MockSmtpServer  You should see: text  Mock SMTP server listening on port 8025 
8. Start the proxy in another terminal: bash  java SmtpProxy  You should see: text  SMTP Proxy listening on port 55555
9. Forwarding to localhost:8025 
Usage
Using Telnet as a Test Client
You can simulate an email client by connecting directly to the proxy via telnet:
1. Connect to the proxy: bash  telnet localhost 55555 
2. Follow the SMTP conversation (example): text  HELO localhost
3. MAIL FROM:<sender@example.com>
4. RCPT TO:<recipient@example.com>
5. DATA
6. 354 End data with <CR><LF>.<CR><LF>
7. Subject: Test
8. This is a fast and bad email.
9. . 
10. The proxy will:
    * Forward all commands to the real SMTP server.
    * During the data phase, it collects the email body.
    * After the client sends . (end of data), it applies the substitutions.
    * Sends the modified email to the server and logs the changes.
11. The mock SMTP server will print the final email it receives.
Example output on the mock SMTP server:
text
--- EMAIL ---
Subject: Test
This is a speedful and ungood email.
Please do not take anything in this email seriously!
Using an Email Client
Configure your email client to use localhost:55555 as the SMTP server (instead of the real server). The proxy will transparently handle the communication.
Stopping the Proxy
Press Ctrl+C in the proxy terminal – the statistics will be printed:
text
=== Proxy Statistics ===
Total emails processed: 5
Illuminati replacements: 1
Total word substitutions: 12
========================

# File Structure

smtp-proxy/
├── SmtpProxy.java          # Main proxy implementation
├── MockSmtpServer.java     # Mock SMTP server for testing
├── proxy.log               # (auto-generated) log of modifications
└── README.md               # This file

# Notes / Caveats
* The proxy only modifies the email body (headers and commands are forwarded unchanged).
* The word substitutions are case-insensitive and respect word boundaries (e.g., "fastest" is not changed).
* The "Illuminati" check overrides all other substitutions – if found, the entire body becomes "Hello world\r\n".
* The mock SMTP server is provided for testing; you can replace it with a real SMTP server (e.g., Postfix, Sendmail) by changing the SMTP_HOSTand SMTP_PORT constants.
* The proxy does not handle authentication – it assumes the upstream SMTP server does not require authentication (or that the client handles it and the proxy simply forwards).
* The proxy does not modify SMTP commands other than the data phase; it only intercepts the email content.
* The proxy.log file grows indefinitely; you may want to rotate or clear it periodically.
