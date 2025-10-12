package com.rentoki.desktopactions;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility class for performing common desktop actions such as opening URLs in browsers
 * and managing file system operations.
 *
 * <p>This class provides cross-platform desktop integration capabilities including:
 * <ul>
 *   <li>Opening URLs in the default web browser</li>
 *   <li>Opening file locations in the system file explorer</li>
 *   <li>Opening directories in the system file explorer</li>
 * </ul>
 *
 * <p>All methods are static and throw {@link DesktopActionException} if the operation fails.
 *
 * @author Rentoki
 */
public final class DesktopActions {

    /**
     * Opens an executable or application using the system's process builder.
     *
     * <p>This method starts a new process for the specified executable path.
     * The executable must be accessible and have proper execution permissions.
     *
     * @param executablePath the path to the executable to run (must not be null or empty)
     * @throws DesktopActionException if the executable path is null/empty or the process cannot be started
     * @example <pre>
     * DesktopActions.open("C:/Program Files/MyApp/myapp.exe");
     * </pre>
     */
    public static void open(String executablePath) throws DesktopActionException {
        if (executablePath == null || executablePath.trim().isEmpty()) {
            throw new DesktopActionException("Executable path cannot be null");
        }

        try {
            new ProcessBuilder(executablePath).start();
        } catch (IOException e) {
            throw new DesktopActionException("Cannot start process.", e);
        }
    }

    /**
     * Opens the specified URL in the system's default web browser.
     *
     * <p>This method converts the string URL to a URI and delegates to {@link #browse(URI)}.
     *
     * @param url the URL to open in the browser (must not be null or empty)
     * @throws DesktopActionException if the URL is null/empty, malformed, the desktop is not supported,
     *                                or the browse action fails
     * @example <pre>
     * DesktopActions.browse("https://www.google.com");
     * </pre>
     * @see #browse(URI)
     */
    public static void browse(String url) throws DesktopActionException {
        if (url == null || url.trim().isEmpty()) {
            throw new DesktopActionException(ErrorMessage.URL_IS_NULL.getMessage());
        }

        try {
            browse(new URI(url));
        } catch (URISyntaxException e) {
            throw new DesktopActionException(ErrorMessage.INVALID_URL.getMessage() + url, e);
        }
    }

    /**
     * Opens the specified URI in the system's default web browser.
     *
     * <p>This method checks if the desktop and browse action are supported before attempting
     * to open the URI. If either is not supported, a {@link DesktopActionException} is thrown.
     *
     * @param uri the URI to open in the browser (must not be null)
     * @throws DesktopActionException if the desktop is not supported, the browse action is not supported,
     *                                or an I/O error occurs during the operation
     * @example <pre>
     * URI uri = new URI("https://www.example.com");
     * DesktopActions.browse(uri);
     * </pre>
     */
    public static void browse(URI uri) throws DesktopActionException {
        if (!isDesktopSupported()) {
            throw new DesktopActionException(ErrorMessage.NOT_SUPPORTED.getMessage());
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            throw new DesktopActionException(ErrorMessage.NOT_SUPPORTED.getMessage());
        }

        try {
            desktop.browse(uri);
        } catch (IOException e) {
            throw new DesktopActionException(ErrorMessage.BROWSE_FAILED.getMessage(), e);
        }
    }

    /**
     * Opens the system file explorer and highlights the specified file.
     *
     * <p>This method converts the string path to a File object and delegates to {@link #openFileLocation(File)}.
     *
     * <p><b>Note:</b> This method currently uses Windows-specific commands (explorer.exe).
     * Cross-platform support may be limited.
     *
     * @param filePath the path to the file to highlight (must not be null or empty)
     * @throws DesktopActionException if the file path is null/empty, the file does not exist,
     *                                or the operation fails
     * @example <pre>
     * DesktopActions.openFileLocation("C:/Users/Documents/example.txt");
     * </pre>
     * @see #openFileLocation(File)
     */
    public static void openFileLocation(String filePath) throws DesktopActionException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new DesktopActionException(ErrorMessage.FILE_PATH_IS_NULL.getMessage());
        }

        openFileLocation(new File(filePath));
    }

    /**
     * Opens the system file explorer and highlights the specified file.
     *
     * <p>This method uses the Windows "explorer /select," command to open the file explorer
     * with the specified file highlighted. The file must exist in the filesystem.
     *
     * <p><b>Note:</b> This method currently uses Windows-specific commands (explorer.exe).
     * Cross-platform support may be limited.
     *
     * @param file the file to highlight in the explorer (must not be null and must exist)
     * @throws DesktopActionException if the file is null, does not exist, or the operation fails
     * @example <pre>
     * File myFile = new File("C:/Users/Documents/example.txt");
     * DesktopActions.openFileLocation(myFile);
     * </pre>
     */
    public static void openFileLocation(File file) throws DesktopActionException {
        if (file == null || !file.exists()) {
            throw new DesktopActionException(ErrorMessage.FILE_IS_NULL.getMessage());
        }

        if (isWindows()) {
            try {
                new ProcessBuilder("explorer", "/select,", file.getAbsolutePath()).start();
            } catch (IOException e) {
                throw new DesktopActionException(ErrorMessage.OPEN_FILE_LOCATION_FAILED.getMessage() + file.getAbsolutePath(), e);
            }
        } else {
            openFileDirectory(file.getParentFile());
        }
    }

    /**
     * Opens the specified directory in the system file explorer.
     *
     * <p>This method converts the string path to a File object and delegates to {@link #openFileDirectory(File)}.
     *
     * @param filePath the path to the directory to open (must not be null or empty)
     * @throws DesktopActionException if the file path is null/empty, the file does not exist,
     *                                is not a directory, or the operation fails
     * @example <pre>
     * DesktopActions.openFileDirectory("C:/Users/Documents");
     * </pre>
     * @see #openFileDirectory(File)
     */
    public static void openFileDirectory(String filePath) throws DesktopActionException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new DesktopActionException(ErrorMessage.FILE_PATH_IS_NULL.getMessage());
        }

        openFileDirectory(new File(filePath));
    }

    /**
     * Opens the specified directory in the system file explorer.
     *
     * <p>This method verifies that the provided file is a directory before attempting to open it.
     * It uses the system's default file manager to display the directory contents.
     *
     * @param file the directory to open (must not be null, must exist, and must be a directory)
     * @throws DesktopActionException if the file is null, does not exist, is not a directory,
     *                                the desktop is not supported, the open action is not supported,
     *                                or an I/O error occurs during the operation
     * @example <pre>
     * File myDirectory = new File("C:/Users/Documents");
     * DesktopActions.openFileDirectory(myDirectory);
     * </pre>
     */
    public static void openFileDirectory(File file) throws DesktopActionException {
        if (file == null || !file.exists()) {
            throw new DesktopActionException(ErrorMessage.FILE_IS_NULL.getMessage());
        }

        if (!file.isDirectory()) {
            throw new DesktopActionException(ErrorMessage.FILE_IS_NOT_DIRECTORY.getMessage());
        }

        if (!isDesktopSupported()) {
            throw new DesktopActionException(ErrorMessage.NOT_SUPPORTED.getMessage());
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.OPEN)) {
            throw new DesktopActionException(ErrorMessage.NOT_SUPPORTED.getMessage());
        }

        try {
            desktop.open(file);
        } catch (IOException e) {
            throw new DesktopActionException(ErrorMessage.OPEN_FILE_DIRECTORY_FAILED.getMessage() + file.getAbsolutePath(), e);
        }
    }

    /**
     * Moves the specified file to the system's trash/recycle bin.
     *
     * <p>This method converts the string path to a File object and delegates to {@link #moveToTrash(File)}.
     *
     * @param filePath the path to the file to move to trash (must not be null or empty)
     * @throws DesktopActionException if the file path is null/empty, the file does not exist,
     *                                the desktop is not supported, or the operation fails
     * @example <pre>
     * DesktopActions.moveToTrash("C:/Users/Documents/old_file.txt");
     * </pre>
     * @see #moveToTrash(File)
     */
    public static void moveToTrash(String filePath) throws DesktopActionException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new DesktopActionException(ErrorMessage.FILE_PATH_IS_NULL.getMessage());
        }

        moveToTrash(new File(filePath));
    }

    /**
     * Moves the specified file to the system's trash/recycle bin.
     *
     * <p>This method uses the Desktop API's MOVE_TO_TRASH action to safely move
     * the file to the system's trash/recycle bin instead of permanently deleting it.
     * The file can typically be restored from the trash if needed.
     *
     * <p>This method checks if the desktop and move to trash action are supported
     * before attempting the operation. The file must exist in the filesystem.
     *
     * @param file the file to move to trash (must not be null and must exist)
     * @throws DesktopActionException if the file is null, does not exist, the desktop is not supported,
     *                                the move to trash action is not supported, or an error occurs during the operation
     * @example <pre>
     * File oldFile = new File("C:/Users/Documents/old_file.txt");
     * DesktopActions.moveToTrash(oldFile);
     * </pre>
     */
    public static void moveToTrash(File file) throws DesktopActionException {
        if (file == null || !file.exists()) {
            throw new DesktopActionException(ErrorMessage.FILE_IS_NULL.getMessage());
        }

        if (!isDesktopSupported()) {
            throw new DesktopActionException(ErrorMessage.NOT_SUPPORTED.getMessage());
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.MOVE_TO_TRASH)) {
            throw new DesktopActionException(ErrorMessage.NOT_SUPPORTED.getMessage());
        }

        desktop.moveToTrash(file);
    }

    /**
     * Checks if the Desktop API is supported on the current platform.
     *
     * <p>This method is a wrapper around {@link Desktop#isDesktopSupported()} and
     * returns whether the Desktop API is available on the current system.</p>
     *
     * <p>The Desktop API may not be supported in certain environments such as:
     * <ul>
     *   <li>Headless systems without graphical displays</li>
     *   <li>Server environments</li>
     *   <li>Some containerized or minimal runtime environments</li>
     * </ul>
     *
     * @return {@code true} if the Desktop API is supported on the current platform,
     * {@code false} otherwise
     * @example <pre>
     * if (DesktopActions.isDesktopSupported()) {
     *     // Perform desktop operations
     *     DesktopActions.browse("https://www.example.com");
     * } else {
     *     // Fallback behavior
     *     System.out.println("Desktop operations not supported");
     * }
     * </pre>
     * @see Desktop#isDesktopSupported()
     */
    public static boolean isDesktopSupported() {
        return Desktop.isDesktopSupported();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
}