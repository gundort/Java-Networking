/**
 * Practical Assignment 5 – LDAP Client for Asset Speed Lookup
 * 
 * COS 332 – Computer Networks
 * 
 * This client uses the system ldapsearch command to query the LDAP server.
 * It manually parses the output, satisfying the requirement of no high-level
 * LDAP libraries. The extra feature searches across multiple OUs.
 */

import java.io.*;
import java.util.*;

public class LDAPAssetClient {
    private static final String BASE_DN = "dc=example,dc=com";
    private static final String[] OUS = {"Automobiles", "Planes", "Trains"};

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java LDAPAssetClient <assetName>");
            System.out.println("Example: java LDAPAssetClient Ferrari");
            return;
        }
        String assetName = args[0];

        boolean found = false;
        String foundSpeed = null;
        String foundOU = null;

        for (String ou : OUS) {
            String searchBase = "ou=" + ou + "," + BASE_DN;
            ProcessBuilder pb = new ProcessBuilder(
                "ldapsearch", "-x", "-b", searchBase, "(cn=" + assetName + ")", "description"
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("description:")) {
                    foundSpeed = line.substring("description:".length()).trim();
                    foundOU = ou;
                    found = true;
                }
            }
            p.waitFor();
            if (found) break;
        }

        if (found) {
            System.out.println("\n Asset \"" + assetName + "\" found in " + foundOU + ".");
            System.out.println("  Maximum speed: " + foundSpeed + " km/h");
        } else {
            System.out.println("\n Asset \"" + assetName + "\" not found.");
            System.out.println("  Available categories: " + String.join(", ", OUS));
        }
    }
}
