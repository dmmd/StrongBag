STRONGBAG
=========

to install:

1. mvn clean compile assembly:single
2. mv application.conf target
3. cd target

to create a strongbag
java -jar java -jar StrongBag-1.0-SNAPSHOT-jar-with-dependencies.jar -create <baglocation> <source directory>

to verify a strongbag
java -jar java -jar StrongBag-1.0-SNAPSHOT-jar-with-dependencies.jar -verifyvalid <baglocation>

notes:
______
a. at present strong bag can only bag a single directory.
b. i lack the mvn-fu to move the application.conf file into the target directory (maybe this should be configurable via the command line?)
c. there is no zip writer/reader, but rest assured it will be added soon and encrypted
