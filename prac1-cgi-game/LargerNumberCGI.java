import java.util.Random;

public class LargerNumberCGI {
    public static void main(String[] args) {
        // HTTP headers – must be printed first, then a blank line
        System.out.println("Content-Type: text/html; charset=UTF-8");
        System.out.println("Cache-Control: no-cache, no-store, must-revalidate");
        System.out.println("Pragma: no-cache");
        System.out.println("Expires: 0");
        System.out.println();

        Random rand = new Random();
        int num1 = rand.nextInt(100) + 1;
        int num2 = rand.nextInt(100) + 1;

        // Decide which number gets the "right" link (the larger one)
        String link1, link2;
        if (num1 > num2) {
            link1 = "/prac1/right.html";
            link2 = "/prac1/wrong.html";
        } else {
            link1 = "/prac1/wrong.html";
            link2 = "/prac1/right.html";
        }

        // ---- Print the HTML page with your exact styling ----
        System.out.println("<!DOCTYPE html>");
        System.out.println("<html>");
        System.out.println("<head>");
        System.out.println("    <meta charset=\"UTF-8\">");
        System.out.println("    <meta http-equiv=\"Cache-Control\" content=\"no-cache, no-store, must-revalidate\">");
        System.out.println("    <title>Number Game</title>");
        System.out.println("    <link href=\"https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap\" rel=\"stylesheet\">");
        System.out.println("    <style>");
        System.out.println("        body {");
        System.out.println("            background-color: #1a1a1a;");
        System.out.println("            font-family: 'Press Start 2P', cursive;");
        System.out.println("            color: #fff;");
        System.out.println("            text-align: center;");
        System.out.println("            margin: 0;");
        System.out.println("            padding: 20px;");
        System.out.println("            image-rendering: pixelated;");
        System.out.println("        }");
        System.out.println("        h1 {");
        System.out.println("            color: #ffcc00;");
        System.out.println("            text-shadow: 4px 4px 0 #aa6600;");
        System.out.println("            font-size: 24px;");
        System.out.println("            margin-top: 50px;");
        System.out.println("        }");
        System.out.println("        .numbers {");
        System.out.println("            margin: 60px 0;");
        System.out.println("        }");
        System.out.println("        .numbers a {");
        System.out.println("            display: inline-block;");
        System.out.println("            margin: 20px;");
        System.out.println("            padding: 20px 40px;");
        System.out.println("            background-color: #4caf50;");
        System.out.println("            color: white;");
        System.out.println("            text-decoration: none;");
        System.out.println("            font-size: 48px;");
        System.out.println("            border: 4px solid #2e7d32;");
        System.out.println("            box-shadow: 0 8px 0 #1b5e20, 0 10px 10px rgba(0,0,0,0.5);");
        System.out.println("            transition: all 0.1s ease;");
        System.out.println("            border-radius: 0;");
        System.out.println("        }");
        System.out.println("        .numbers a:hover {");
        System.out.println("            background-color: #66bb6a;");
        System.out.println("            transform: translateY(4px);");
        System.out.println("            box-shadow: 0 4px 0 #1b5e20, 0 8px 8px rgba(0,0,0,0.5);");
        System.out.println("        }");
        System.out.println("        .numbers a:active {");
        System.out.println("            transform: translateY(8px);");
        System.out.println("            box-shadow: 0 0 0 #1b5e20, 0 5px 5px rgba(0,0,0,0.5);");
        System.out.println("        }");
        System.out.println("        footer {");
        System.out.println("            margin-top: 80px;");
        System.out.println("            color: #888;");
        System.out.println("            font-size: 10px;");
        System.out.println("        }");
        System.out.println("    </style>");
        System.out.println("</head>");
        System.out.println("<body>");
        System.out.println("    <h1> CLICK THE LARGER NUMBER </h1>");
        System.out.println("    <div class=\"numbers\">");
        System.out.printf("        <a href=\"%s\">%d</a>\n", link1, num1);
        System.out.printf("        <a href=\"%s\">%d</a>\n", link2, num2);
        System.out.println("    </div>");
        System.out.println("    <footer>Choose wisely ...</footer>");
        System.out.println("</body>");
        System.out.println("</html>");
    }
}