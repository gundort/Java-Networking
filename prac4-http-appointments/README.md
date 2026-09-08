
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

1. Open your browser and navigate to http://localhost:8090.

# Usage
All interaction happens through the web interface.
* Add an appointment – fill in the date, time, description, and optionally select an image. Click Add Appointment.
* Search – type a keyword into the search box and click Search.
* Delete – click the Delete link next to any appointment.
Images appear as thumbnails in the list; clicking a thumbnail opens the full image in a new tab/window.
HTTP Endpoints (for reference)
Endpoint	Method	Description
/	GET	Main page – displays the form and the appointment list.
/add	POST	Accepts multipart/form-datato add a new appointment.
/delete?id=<id>	GET	Deletes the appointment with the specified ID.
/search?q=<keyword>	GET	Shows search results matching the keyword.
/image?name=<filename>	GET	Serves the image file from the images/directory.

File Structure
text
appointment-server-image/
├── AppointmentServer.java      # Main server + inner ClientHandler
├── appointments.txt            # (auto-generated) saved appointment data
├── images/                     # (auto-created) folder for uploaded images
│   └── 1612345678_abc123.jpg   # example uploaded image
└── README.md                   # This file
* appointments.txt format: id|date|time|description|imageFile (one per line). You can manually edit this file while the server is stopped.

Notes / Caveats
* The default port is 8090. If that port is already in use, change the PORT constant in the source.
* Image uploads are read entirely into memory; for very large files (e.g., >10 MB) this may be heavy – but for typical appointment pictures it's fine.
* This is a demonstration server – it does not implement authentication, HTTPS, or comprehensive input sanitisation. Do not expose it to the public internet.
* Uploaded images are renamed to a timestamp + random ID to avoid filename collisions.
* The server does not validate image content – it accepts any file but serves only based on extension.
