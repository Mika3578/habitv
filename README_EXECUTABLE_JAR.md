# HabiTV Executable JAR

This document provides instructions on how to build and use the executable JAR for HabiTV.

## Building the Executable JAR

### Using the Build Script (Recommended)

The easiest way to build the executable JAR is to use the provided build script:

#### Windows:
```
build_jar.bat
```

#### Linux/Unix:
```
chmod +x build_jar.sh
./build_jar.sh
```

This will:
1. Build the executable JAR
2. Copy it to additional locations for convenience
3. Display instructions on how to run it

### Manual Build

Alternatively, you can manually build the JAR using Maven:

1. Navigate to the project root directory
2. Run the following command:

```
mvn clean package -f application\habiTv\pom.xml
```

This will create an executable JAR file at:
```
application\habiTv\target\habiTv-4.1.0-SNAPSHOT.jar
```

For convenience, you can also copy this JAR to the target directory:
```
copy application\habiTv\target\habiTv-4.1.0-SNAPSHOT.jar target\habiTv.jar
```

## Running the Executable JAR

The executable JAR can be run in two modes. You can use either the primary location or the convenience location.

### GUI Mode

To run the application in GUI mode (with a graphical user interface):

```
# Using the primary location:
java -jar application\habiTv\target\habiTv-4.1.0-SNAPSHOT.jar

# OR using the convenience location:
java -jar target\habiTv.jar
```

### Console Mode

To run the application in console mode (command-line interface), provide command-line arguments:

```
# Using the primary location:
java -jar application\habiTv\target\habiTv-4.1.0-SNAPSHOT.jar [arguments]

# OR using the convenience location:
java -jar target\habiTv.jar [arguments]
```

Replace `[arguments]` with the appropriate command-line arguments for your use case.

## Notes

- The executable JAR includes all necessary dependencies
- The main class is `com.dabi.habitv.HabitvLauncher`
- The application will create a user directory at `~/habitv` if it doesn't exist
- Log files will be stored in the user directory
