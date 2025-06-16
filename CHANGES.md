# Changes Made to Compile JAR Executable

## Issue Description
The task was to compile a JAR executable for the HabiTV application.

## Changes Made

1. **Updated the maven-shade-plugin in the habiTv module's pom.xml**:
   - Updated the plugin version from 2.2 to 3.5.1
   - Added the ServicesResourceTransformer
   - Set createDependencyReducedPom to false
   - Verified the main class is correctly set to com.dabi.habitv.HabitvLauncher

2. **Added a dependency on the core module to the habiTv module's pom.xml**:
   - This ensures all necessary functionality is included in the executable JAR

3. **Updated the README.md**:
   - Added detailed instructions on how to build the executable JAR
   - Included information on where to find the JAR and how to run it

## Testing
The changes were tested by running the Maven build command:
```
cd application\habiTv
mvn clean package
```

This successfully created an executable JAR at:
`application\habiTv\target\habiTv-4.1.0-SNAPSHOT.jar`

## How to Use
To build the executable JAR:
```
cd application\habiTv
mvn clean package
```

To run the executable JAR:
```
java -jar application\habiTv\target\habiTv-4.1.0-SNAPSHOT.jar
```

This will start the application in GUI mode. If you want to run it in console mode, you can pass command-line arguments:
```
java -jar application\habiTv\target\habiTv-4.1.0-SNAPSHOT.jar [arguments]
```