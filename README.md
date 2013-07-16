STRONGBAG
=========
a cryptographically strengthened baagit clone

to install:

1. mvn clean compile assembly:single
2. mv application.conf target
3. cd target

<b>to create a strongbag</b><br />
java -jar java -jar StrongBag-0.1-SNAPSHOT-jar-with-dependencies.jar -create <baglocation> <source directory>

<b>to verify a strongbag</b><br />
java -jar java -jar StrongBag-0.1-SNAPSHOT-jar-with-dependencies.jar -verifyvalid <baglocation>

notes:
______
1. i lack the mvn-fu to move the application.conf file into the target directory (maybe this should be configurable via the command line?)<br />
2. there is no zip writer/reader, but the application will support multiple compression formats
3. encryption of the entire bag, the files in the bag, and both will be supported
