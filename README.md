STRONGBAG
=========
a cryptographically strengthened bagit clone.

Description: Currently Strongbag is a utility to create bagit-like packages that creates SHA256withRSA digital signatures for any file placed in a (strong)bag. The application, at present, can creates a keystore and generates a 2048 byte RSA keypair, with alias 'rsa', that is used for signing and verify files in the strongbag. Strongbag uses the same passphrase (which the user enters) for both the keystore and the alias.  

to install:

1. mvn clean compile assembly:single
2. mv application.conf target
3. cd target

<b>to create a strongbag</b><br />
java -jar java -jar StrongBag-0.1-SNAPSHOT-jar-with-dependencies.jar -create [bag location] [source directories]

<b>to verify a strongbag</b><br />
java -jar java -jar StrongBag-0.1-SNAPSHOT-jar-with-dependencies.jar -verifyvalid [bag location]

<br />
notes:
______
1. i lack the mvn-fu to move the application.conf file into the target directory (maybe this should be configurable via the command line?)<br />
2. there is no zip writer/reader, but the application will support multiple compression formats
3. encryption of the entire bag, the files in the bag, and both will be supported, just not yet
