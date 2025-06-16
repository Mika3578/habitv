#!/bin/bash

echo "Building HabiTV executable JAR..."

# Build the executable JAR
mvn clean package -f application/habiTv/pom.xml

if [ $? -ne 0 ]; then
    echo "Failed to build the executable JAR."
    exit 1
fi

echo
echo "Creating target directory if it doesn't exist..."
mkdir -p target

echo
echo "Copying JAR to convenience location..."
cp application/habiTv/target/habiTv-4.1.0-SNAPSHOT.jar target/habiTv.jar

echo
echo "Build completed successfully!"
echo
echo "The executable JAR is available at:"
echo "- Primary location: application/habiTv/target/habiTv-4.1.0-SNAPSHOT.jar"
echo "- Convenience location: target/habiTv.jar"
echo
echo "To run in GUI mode:"
echo "java -jar target/habiTv.jar"
echo
echo "To run in console mode:"
echo "java -jar target/habiTv.jar [arguments]"
echo