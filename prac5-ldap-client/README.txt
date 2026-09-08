Practical Assignment 5: LDAP Client for Asset Speed Lookup
COS 332 Computer Networks

Full Name: Gundo Tshavhungwe
Student Number: 26855412

Files included

- LDAPAssetClient.java   
- LDAPAssetClient.class 
- README.txt             (this file)

How to run the program

1. Ensure the OpenLDAP server is running (slapd) on localhost:389
2. Compile:  javac LDAPAssetClient.java
3. Run:      java LDAPAssetClient <assetName>

Example:    java LDAPAssetClient Ferrari

What the program does:

- Connects to LDAP server on port 389.
- Performs a simple bind using cn=admin,dc=example,dc=com (password: gundo).
- Searches for an asset by its common name (cn) in three OUs:
     Automobiles, Planes, Trains.
- If found, displays the asset's OU and its maximum speed (km/h).
- If not found, lists the available categories.

Extra feature added:

The client automatically searches across all three OUs (Automobiles,
Planes, Trains) and stops when the asset is found. If the asset is
not found, it prints the list of available OUs. This demonstrates
understanding of LDAP's hierarchical structure without altering the
core functionality.

MD5 hash of executable (LDAPAssetClient.class): 2c65af18f6c2a74a048ac561cc1f0e2d

