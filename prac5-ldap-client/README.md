# LDAP Asset Client

A command-line client that queries an OpenLDAP server to look up the maximum speed of an asset (automobile, plane, or train). It searches across three organisational units (OUs) and stops when the asset is found. The client uses the system `ldapsearch` command and parses its output – no external LDAP libraries are required.

## Features

- Search by asset name – looks for an entry with a matching `cn` (common name).
- Searches multiple OUs – Automobiles, Planes, and Trains (configurable).
- Displays the asset's OU and speed – if found, shows the category and the `description` attribute (maximum speed in km/h).
- User feedback – if the asset is not found, it lists the available categories.
- Lightweight – uses the system's built-in `ldapsearch` tool; no extra dependencies.

## Technologies

- Java (JDK 8+)
- OpenLDAP server (slapd) – must be running locally.
- `ldapsearch` command – assumed to be in the system PATH.
- Plain text parsing – reads and processes the output of `ldapsearch`.

## Prerequisites

- Java Runtime Environment (JRE) or JDK installed.
- OpenLDAP server installed and running on `localhost:389`.
- The LDAP directory must contain entries under `dc=example,dc=com` with OUs `Automobiles`, `Planes`, and `Trains`.
- The server must allow anonymous `-x` binds (or the default credentials must be correct).
- The `ldapsearch` command must be available in your system's PATH (typically installed with OpenLDAP).

## Setup & Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/ldap-asset-client.git
   cd ldap-asset-client
Compile the Java source:

bash
javac LDAPAssetClient.java
Ensure the LDAP server is running (on localhost:389):

bash
sudo service slapd start   # or systemctl start slapd
Populate the LDAP directory with your asset data (use the provided LDIF files or create your own).
Usage

Run the client with the asset name as an argument:

bash
java LDAPAssetClient <assetName>
Example:

bash
java LDAPAssetClient Ferrari
Expected output (if found):

text
Asset "Ferrari" found in Automobiles.
  Maximum speed: 350 km/h
If not found:

text
Asset "Ferrari" not found.
  Available categories: Automobiles, Planes, Trains
The client searches in the following order: Automobiles, Planes, Trains. It stops at the first match.

File Structure

text
ldap-asset-client/
├── LDAPAssetClient.java      # Main client source
├── LDAPAssetClient.class     # (compiled) bytecode
├── README.md                 # This file
└── assets.ldif               # (optional) sample LDIF data for import
Notes / Caveats

The client relies on the ldapsearch command being present and correctly configured. If the command is not in your PATH, the program will throw an IOException.
The search base is hard-coded to dc=example,dc=com. If your LDAP server uses a different base DN, you must modify the BASE_DN constant in the source.
The description attribute is expected to contain the maximum speed. The output is parsed as description: followed by the value.
The client performs an anonymous bind (-x). If your server requires authentication, you would need to add credentials to the ldapsearch command (e.g., -D cn=admin,dc=example,dc=com -w password).
This is a demonstration tool for educational purposes – it does not handle LDAP referrals, pagination, or advanced search filters.
