### Prac 8 – FTP Backup (`prac8-ftp-backup/README.md`)

```markdown
# FTP Backup

A directory monitoring and FTP backup utility. The program watches a local directory for file changes (create, modify, delete) and automatically synchronises changes with an FTP server. New or modified files are uploaded; deleted files are removed from the remote server.

## Features

- Directory monitoring – uses Java's `WatchService` to detect file changes in real time.
- FTP upload – automatically uploads new or modified files to a remote FTP server.
- Remote deletion – when a local file is deleted, the corresponding remote file is also deleted (extra feature).
- Raw FTP protocol – implements FTP commands directly over sockets (no external libraries).
- Passive mode – supports FTP passive mode for data connections.
- Binary transfer – ensures files are uploaded in binary mode (preserves integrity).
- Automatic directory creation – creates the watch directory if it doesn't exist.

## Technologies

- Java (JDK 8+)
- Sockets (TCP/IP)
- FTP protocol – raw implementation (no libraries)
- WatchService API – for file system monitoring

## Prerequisites

- Java Runtime Environment (JRE) or JDK installed.
- An FTP server accessible on `localhost:21` (or customisable in the code).
- FTP server credentials configured in the source code.
- The FTP server must support passive mode (`PASV` command).
- The FTP user must have write permissions in the remote directory.

## Setup & Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/ftp-backup.git
   cd ftp-backup
4. Compile the Java source: bash  javac FTPBackup.java 
6. Configure the FTP server – ensure an FTP server is running on localhost with port 21 and the credentials match those in the code: java  private static final String FTP_USER = "ftpuser";
8. private static final String FTP_PASS = "password"; 
10. Create the watch directory (optional – the program will create it if it doesn't exist): bash  mkdir -p ./watchdir 
Usage
1. Start the FTP Server
Make sure your FTP server is running. For testing, you can use a simple FTP server like vsftpd, proftpd, or the built-in FTP server on your OS.
2. Run the FTP Backup
bash
java FTPBackup
The program will display:
text
Created watch directory: /path/to/watchdir
Watching directory: /path/to/watchdir
3. Test the Backup
* Create a file in the watchdir folder: bash  echo "Hello, world!" > watchdir/test.txt  The program will detect the creation and upload it to the FTP server: text  Detected: CREATE test.txt
* Upload successful: test.txt 
* Modify the file: bash  echo "Updated content" > watchdir/test.txt  The program will detect the modification and re-upload the file.
* Delete the file: bash  rm watchdir/test.txt  The program will detect the deletion and remove the file from the FTP server: text  Detected: DELETE test.txt
* Deleted remote file: /test.txt 
4. Stop the Program
Press Ctrl+C in the terminal where the program is running.
File Structure
text
ftp-backup/
├── FTPBackup.java      # Main application
├── watchdir/           # (auto-created) directory to monitor
└── README.md           # This file
Notes / Caveats
* The FTP server credentials are hard-coded in FTPBackup.java. For production use, consider reading them from a configuration file or environment variables.
* The remote directory is set to / (root of the FTP user's home). You can change the REMOTE_DIRconstant to a subdirectory (e.g., /backup).
* The program uses passive mode (PASV). If your FTP server requires active mode, you would need to modify the implementation.
* The watch service does not monitor subdirectories recursively – only files in the watchdir folder are tracked. For recursive monitoring, additional logic would be needed.
* The program runs continuously until stopped. It is intended as a background service.
* Large file uploads may take time; the program is single-threaded and will process files sequentially.
* If the FTP server is unavailable, uploads will fail and the error will be logged – the program does not retry failed uploads.
