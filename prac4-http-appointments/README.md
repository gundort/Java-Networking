
---

### Prac 4 - README.md
*(Place in `prac4-http-appointments/`)*

```markdown
# Appointment Server (with Image Support)

A multi-user appointment management server accessible via a web browser. Users can add, search, and delete appointments, and optionally upload an image for each entry. The server handles HTTP requests concurrently and persists all data to a plain text file and an images folder.

## Features

- Add appointments (date, time, description, optional image)
- List all appointments in a clean HTML table
- Search descriptions for keywords
- Delete appointments (associated image files are removed automatically)
- Image upload – supports common image formats (`.jpg`, `.png`, `.gif`)
- Image serving – images are accessible via `/image?name=...`
- Persistent storage – appointments saved to `appointments.txt`, images stored in `images/` folder
- Concurrent – each HTTP request is handled in its own thread

## Technologies

- Java (JDK 8+)
- HTTP/1.1 (built-in `java.net` sockets)
- HTML5 + CSS3 (responsive design)
- Multipart form data – manual parsing (no external libraries)

## Prerequisites

- Java Runtime Environment (JRE) or JDK installed.
- A modern web browser (Chrome, Firefox, Edge, etc.).

## Setup & Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/appointment-server-image.git
   cd appointment-server-image
