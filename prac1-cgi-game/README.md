# Larger Number Game (CGI)

A simple web-based game that presents two random numbers. The player must click the larger one. Correct answers lead to a success page, wrong answers to a failure page. Built with Java as a CGI script and served via Apache.

## How It Works

- The Java program generates two random integers (1–100).
- It dynamically generates an HTML page with both numbers as clickable links.
- The larger number links to `right.html`, the smaller to `wrong.html`.
- The game uses no caching to always show fresh numbers.

## Technologies

- Java (JDK 8+)
- Apache HTTP Server with CGI enabled
- HTML5 + CSS3 (retro "Press Start 2P" font)

## Setup & Deployment

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/larger-number-game.git
   cd larger-number-game
