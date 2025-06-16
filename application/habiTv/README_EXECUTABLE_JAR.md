# HabiTV Executable JAR

This document provides instructions on how to build and use the executable JAR for HabiTV.

## Building the Executable JAR

The project is already configured to create an executable JAR using the Maven Shade Plugin. To build the JAR:

1. Navigate to the project root directory
2. Run the following command:

```
mvn clean package -f application\habiTv\pom.xml
```

This will create an executable JAR file at:
```
application\habiTv\target\habiTv-4.1.0-SNAPSHOT.jar
```

## Running the Executable JAR

The executable JAR can be run in two modes:

### GUI Mode

To run the application in GUI mode (with a graphical user interface):

```
java -jar application\habiTv\target\habiTv-4.1.0-SNAPSHOT.jar
```

### Console Mode

To run the application in console mode (command-line interface), provide command-line arguments:

```
java -jar application\habiTv\target\habiTv-4.1.0-SNAPSHOT.jar [arguments]
```

Replace `[arguments]` with the appropriate command-line arguments for your use case.

## Notes

- The executable JAR includes all necessary dependencies
- The main class is `com.dabi.habitv.HabitvLauncher`
- The application will create a user directory at `~/habitv` if it doesn't exist
- Log files will be stored in the user directory