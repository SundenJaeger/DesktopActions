# DesktopActions

A lightweight Java utility library for performing common desktop actions.

## Features

- Open URLs in the default web browser
- Open and highlight files in the system file explorer
- Open directories in the system file explorer
- Open executables
- Move files to system trash/recycle bin
- Create desktop shortcuts
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
    <version>1.1.0</version>
</dependency>
```

## Usage

### Opening Executables

Start an executable or application:

```java
import com.rentoki.desktopactions.DesktopActions;
import com.rentoki.desktopactions.DesktopActionException;

try {
    DesktopActions.open("C:/Program Files/MyApp/myapp.exe");
} catch (DesktopActionException e) {
    System.err.println("Failed to open executable: " + e.getMessage());
}
```

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

### Moving Files to Trash

Safely move files to the system trash/recycle bin instead of permanently deleting them:

```java
try {
    // Using file path string
    DesktopActions.moveToTrash("C:/Users/Documents/old_file.txt");
    
    // Using File object
    File oldFile = new File("C:/Users/Documents/old_file.txt");
    DesktopActions.moveToTrash(oldFile);
} catch (DesktopActionException e) {
    System.err.println("Failed to move file to trash: " + e.getMessage());
}
```

### Creating Shortcuts

Create shortcuts to files, applications, or directories:

```java
try {
    // Create a shortcut on the Desktop (default location)
    DesktopActions.createShortcut("C:/Program Files/MyApp/myapp.exe");
    
    // Create a shortcut in a custom location
    DesktopActions.createShortcut(
        "C:/Program Files/MyApp/myapp.exe",
        "C:/Users/Documents/Shortcuts"
    );
    
    // Create a shortcut for a folder
    DesktopActions.createShortcut(
        "C:/Projects/MyProject",
        System.getProperty("user.home") + "/Desktop"
    );
} catch (DesktopActionException e) {
    System.err.println("Failed to create shortcut: " + e.getMessage());
}
```

**Note:** Shortcut creation is designed for Windows systems using .lnk files.

### Checking Desktop Support

Check if the Desktop API is supported on the current platform:

```java
if (DesktopActions.isDesktopSupported()) {
    // Perform desktop operations
    DesktopActions.browse("https://www.example.com");
} else {
    // Fallback behavior
    System.out.println("Desktop operations not supported");
}
```

## Platform Support

| Feature | Windows | macOS | Linux |
|---------|---------|-------|-------|
| Browse URL | ✅ | ✅ | ✅ |
| Open Executable | ✅ | ✅ | ✅ |
| Open Directory | ✅ | ✅ | ✅ |
| Open File Location | ✅ | ⚠️ | ⚠️ |
| Move to Trash | ✅ | ✅ | ✅ |
| Create Shortcut | ✅ | ⚠️ | ⚠️ |
 
⚠️ = Limited support (uses Windows-specific commands)

## Contributing

- Fork the repo
- Create a feature branch
- Commit with clear messages
- Open a PR with a concise description and screenshots/gifs if UI is affected

## Third-party libraries used in this project
- [**mslinks**](https://github.com/DmitriiShamrikov/mslinks) by Dmitrii Shamrikov under [**WTFPL License**](https://github.com/DmitriiShamrikov/mslinks/blob/master/LICENSE)