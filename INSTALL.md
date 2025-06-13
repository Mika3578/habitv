# HabiTv Installation Guide (Java 21 Modernization)

## Prerequisites
- Java 21 (LTS) JDK (https://adoptium.net or https://jdk.java.net/21/)
- Maven 3.9+
- Internet connection for dependency download

## Build Instructions
1. Clone the repository:
   ```
   git clone <repo-url>
   cd habitv
   ```
2. Build the project:
   ```
   mvn clean package
   ```
   - This will compile all modules and run tests.
   - If you encounter test failures due to network or external dependencies, you can skip tests:
     ```
     mvn clean package -DskipTests
     ```

## Running
- JavaFX modules require JavaFX 21.0.2. Maven will handle dependencies for you.
- To run a specific module, use the appropriate main class (see README or module documentation).

## Notes
- All modules are now configured for Java 21. Legacy Java 1.7/1.8 is no longer supported.
- If you encounter duplicate class errors, ensure generated sources do not overlap with manual sources.

## Troubleshooting
- Ensure JAVA_HOME points to a Java 21 JDK.
- Use `mvn -version` to confirm Maven and Java versions.
- For further help, see README.md or open an issue. 