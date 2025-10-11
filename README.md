# DesktopActions

A lightweight Java utility library for performing common desktop actions such as opening URLs in browsers and managing file system operations.

## Features

- Open URLs in the default web browser
- Open and highlight files in the system file explorer
- Open directories in the system file explorer
- Simple, straightforward API

## Installation

### Maven

Add the GitHub Packages repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/SundenJaeger/DesktopActions</url>
    </repository>
</repositories>
```

Then add the dependency:

```xml
<dependency>
    <groupId>com.rentoki</groupId>
    <artifactId>desktop-actions</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### Opening URLs in Browser

```java
import com.rentoki.desktopactions.DesktopActions;
import com.rentoki.desktopactions.DesktopActionException;

try {
    // Using String URL
    DesktopActions.browse("https://www.google.com");
    
    // Using URI object
    URI uri = new URI("https://www.example.com");
    DesktopActions.browse(uri);
} catch (DesktopActionException e) {
    System.err.println("Failed to open URL: " + e.getMessage());
}
```

### Opening File Location

Highlights a specific file in the system file explorer:

```java
try {
    // Using file path string
    DesktopActions.openFileLocation("C:/Users/Documents/example.txt");
    
    // Using File object
    File file = new File("C:/Users/Documents/example.txt");
    DesktopActions.openFileLocation(file);
} catch (DesktopActionException e) {
    System.err.println("Failed to open file location: " + e.getMessage());
}
```

**Note:** This feature currently uses Windows-specific commands and may have limited cross-platform support.

### Opening Directories

Opens a directory in the system file explorer:

```java
try {
    // Using directory path string
    DesktopActions.openFileDirectory("C:/Users/Documents");
    
    // Using File object
    File directory = new File("C:/Users/Documents");
    DesktopActions.openFileDirectory(directory);
} catch (DesktopActionException e) {
    System.err.println("Failed to open directory: " + e.getMessage());
}
```

## Platform Support

| Feature | Windows | macOS | Linux |
|---------|---------|-------|-------|
| Browse URL | ✅ | ✅ | ✅ |
| Open Directory | ✅ | ✅ | ✅ |
| Open File Location | ✅ | ⚠️ | ⚠️ |

⚠️ = Limited support (uses Windows-specific commands)

## Contributing

- Fork the repo
- Create a feature branch
- Commit with clear messages
- Open a PR with a concise description and screenshots/gifs if UI is affected