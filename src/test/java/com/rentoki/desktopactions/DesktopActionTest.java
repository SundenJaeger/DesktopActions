package com.rentoki.desktopactions;

import mslinks.ShellLink;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DesktopActionTest {
    @TempDir
    Path tempDir;

    private MockedStatic<Desktop> desktopMock;
    private String originalUserHome;
    private MockedStatic<ShellLink> shellLinkMock;

    @BeforeEach
    void setUp() {
        desktopMock = mockStatic(Desktop.class);
        shellLinkMock = mockStatic(ShellLink.class);
    }

    @AfterEach
    void tearDown() {
        if (desktopMock != null) {
            desktopMock.close();
        }

        if (originalUserHome != null) {
            System.setProperty("user.home", originalUserHome);
        }

        if (shellLinkMock != null) {
            shellLinkMock.close();
        }
    }

    @Test
    void browse_WithValidUrl_ShouldNotThrowException() throws Exception {
        String validUrl = "https://www.example.com";
        Desktop desktop = mock(Desktop.class);
        when(Desktop.isDesktopSupported()).thenReturn(true);
        when(Desktop.getDesktop()).thenReturn(desktop);
        when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);
        doNothing().when(desktop).browse(any(URI.class));

        assertDoesNotThrow(() -> DesktopActions.browse(validUrl));
    }

    @Test
    void browse_WithNullUrl_ShouldThrowDesktopActionException() {
        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.browse((String) null)
        );
        assertEquals(ErrorMessage.URL_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void browse_WithEmptyUrl_ShouldThrowDesktopActionException() {
        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.browse("   ")
        );
        assertEquals(ErrorMessage.URL_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void browse_WithInvalidUrl_ShouldThrowDesktopActionException() {
        String invalidUrl = "ht tp://invalid url with spaces";

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.browse(invalidUrl)
        );
        assertTrue(exception.getMessage().contains(ErrorMessage.INVALID_URL.getMessage()),
                "Expected '" + ErrorMessage.INVALID_URL.getMessage() + "' but got: " + exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void browse_WithValidUri_ShouldNotThrowException() throws Exception {
        URI validUri = new URI("https://www.example.com");
        Desktop desktop = mock(Desktop.class);
        when(Desktop.isDesktopSupported()).thenReturn(true);
        when(Desktop.getDesktop()).thenReturn(desktop);
        when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);
        doNothing().when(desktop).browse(validUri);

        assertDoesNotThrow(() -> DesktopActions.browse(validUri));
    }

    @Test
    void browse_WhenDesktopNotSupported_ShouldThrowDesktopActionException() {
        URI validUri = URI.create("https://www.example.com");
        when(Desktop.isDesktopSupported()).thenReturn(false);

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.browse(validUri)
        );
        assertEquals(ErrorMessage.NOT_SUPPORTED.getMessage(), exception.getMessage());
    }

    @Test
    void browse_WhenBrowseActionNotSupported_ShouldThrowDesktopActionException() {
        URI validUri = URI.create("https://www.example.com");
        Desktop desktop = mock(Desktop.class);
        when(Desktop.isDesktopSupported()).thenReturn(true);
        when(Desktop.getDesktop()).thenReturn(desktop);
        when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(false);

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.browse(validUri)
        );
        assertEquals(ErrorMessage.NOT_SUPPORTED.getMessage(), exception.getMessage());
    }

    @Test
    void browse_WhenIOExceptionOccurs_ShouldThrowDesktopActionException() throws Exception {
        URI validUri = URI.create("https://www.example.com");
        Desktop desktop = mock(Desktop.class);
        when(Desktop.isDesktopSupported()).thenReturn(true);
        when(Desktop.getDesktop()).thenReturn(desktop);
        when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);
        doThrow(new IOException("Browser error")).when(desktop).browse(validUri);

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.browse(validUri)
        );
        assertEquals(ErrorMessage.BROWSE_FAILED.getMessage(), exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void openFileLocation_WithValidFilePath_ShouldNotThrowException() throws Exception {
        File tempFile = tempDir.resolve("test.txt").toFile();
        tempFile.createNewFile();

        try (MockedConstruction<ProcessBuilder> mock = mockConstruction(ProcessBuilder.class,
                (processBuilder, context) -> {
                    Process process = mock(Process.class);
                    when(processBuilder.start()).thenReturn(process);
                })) {

            assertDoesNotThrow(() -> DesktopActions.openFileLocation(tempFile.getAbsolutePath()));
        }
    }

    @Test
    void openFileLocation_WithNullFilePath_ShouldThrowDesktopActionException() {
        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.openFileLocation((String) null)
        );
        assertEquals(ErrorMessage.FILE_PATH_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void openFileLocation_WithEmptyFilePath_ShouldThrowDesktopActionException() {
        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.openFileLocation("   ")
        );
        assertEquals(ErrorMessage.FILE_PATH_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void openFileLocation_WithNonExistentFile_ShouldThrowDesktopActionException() {
        File nonExistentFile = new File("nonexistent.txt");

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.openFileLocation(nonExistentFile)
        );
        assertEquals(ErrorMessage.FILE_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void openFileLocation_WhenIOExceptionOccurs_ShouldThrowDesktopActionException() throws Exception {
        File tempFile = tempDir.resolve("test.txt").toFile();
        tempFile.createNewFile();

        try (MockedConstruction<ProcessBuilder> mock = mockConstruction(ProcessBuilder.class,
                (processBuilder, context) -> when(processBuilder.start()).thenThrow(new IOException("Process error")))) {

            DesktopActionException exception = assertThrows(
                    DesktopActionException.class,
                    () -> DesktopActions.openFileLocation(tempFile)
            );
            assertTrue(exception.getMessage().contains(ErrorMessage.OPEN_FILE_LOCATION_FAILED.getMessage()));
            assertNotNull(exception.getCause());
        }
    }

    @Test
    void openFileDirectory_WithValidDirectoryPath_ShouldNotThrowException() throws Exception {
        File directory = tempDir.toFile();
        Desktop desktop = mock(Desktop.class);
        when(Desktop.isDesktopSupported()).thenReturn(true);
        when(Desktop.getDesktop()).thenReturn(desktop);
        when(desktop.isSupported(Desktop.Action.OPEN)).thenReturn(true);
        doNothing().when(desktop).open(directory);

        assertDoesNotThrow(() -> DesktopActions.openFileDirectory(directory.getAbsolutePath()));
    }

    @Test
    void openFileDirectory_WithValidDirectoryFile_ShouldNotThrowException() throws Exception {
        File directory = tempDir.toFile();
        Desktop desktop = mock(Desktop.class);
        when(Desktop.isDesktopSupported()).thenReturn(true);
        when(Desktop.getDesktop()).thenReturn(desktop);
        when(desktop.isSupported(Desktop.Action.OPEN)).thenReturn(true);
        doNothing().when(desktop).open(directory);

        assertDoesNotThrow(() -> DesktopActions.openFileDirectory(directory));
    }

    @Test
    void openFileDirectory_WithNullDirectoryPath_ShouldThrowDesktopActionException() {
        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.openFileDirectory((String) null)
        );
        assertEquals(ErrorMessage.FILE_PATH_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void openFileDirectory_WithFileInsteadOfDirectory_ShouldThrowDesktopActionException() throws Exception {
        File tempFile = tempDir.resolve("test.txt").toFile();
        tempFile.createNewFile();

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.openFileDirectory(tempFile)
        );
        assertEquals(ErrorMessage.FILE_IS_NOT_DIRECTORY.getMessage(), exception.getMessage());
    }

    @Test
    void openFileDirectory_WhenDesktopNotSupported_ShouldThrowDesktopActionException() {
        File directory = tempDir.toFile();
        when(Desktop.isDesktopSupported()).thenReturn(false);

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.openFileDirectory(directory)
        );
        assertEquals(ErrorMessage.NOT_SUPPORTED.getMessage(), exception.getMessage());
    }

    @Test
    void openFileDirectory_WhenOpenActionNotSupported_ShouldThrowDesktopActionException() {
        File directory = tempDir.toFile();
        Desktop desktop = mock(Desktop.class);
        when(Desktop.isDesktopSupported()).thenReturn(true);
        when(Desktop.getDesktop()).thenReturn(desktop);
        when(desktop.isSupported(Desktop.Action.OPEN)).thenReturn(false);

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.openFileDirectory(directory)
        );
        assertEquals(ErrorMessage.NOT_SUPPORTED.getMessage(), exception.getMessage());
    }

    @Test
    void openFileDirectory_WhenIOExceptionOccurs_ShouldThrowDesktopActionException() throws Exception {
        File directory = tempDir.toFile();
        Desktop desktop = mock(Desktop.class);
        when(Desktop.isDesktopSupported()).thenReturn(true);
        when(Desktop.getDesktop()).thenReturn(desktop);
        when(desktop.isSupported(Desktop.Action.OPEN)).thenReturn(true);
        doThrow(new IOException("Open error")).when(desktop).open(directory);

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.openFileDirectory(directory)
        );
        assertTrue(exception.getMessage().contains(ErrorMessage.OPEN_FILE_DIRECTORY_FAILED.getMessage()));
        assertNotNull(exception.getCause());
    }

    @Test
    void moveToTrash_WithValidFilePath_ShouldNotThrowException() throws Exception {
        File tempFile = tempDir.resolve("test.txt").toFile();
        tempFile.createNewFile();
        Desktop desktop = mock(Desktop.class);
        when(Desktop.isDesktopSupported()).thenReturn(true);
        when(Desktop.getDesktop()).thenReturn(desktop);
        when(desktop.isSupported(Desktop.Action.MOVE_TO_TRASH)).thenReturn(true);
        when(desktop.moveToTrash(tempFile)).thenReturn(true);

        assertDoesNotThrow(() -> DesktopActions.moveToTrash(tempFile.getAbsolutePath()));
    }

    @Test
    void moveToTrash_WithValidFile_ShouldNotThrowException() throws Exception {
        File tempFile = tempDir.resolve("test.txt").toFile();
        tempFile.createNewFile();
        Desktop desktop = mock(Desktop.class);
        when(Desktop.isDesktopSupported()).thenReturn(true);
        when(Desktop.getDesktop()).thenReturn(desktop);
        when(desktop.isSupported(Desktop.Action.MOVE_TO_TRASH)).thenReturn(true);
        when(desktop.moveToTrash(tempFile)).thenReturn(true);

        assertDoesNotThrow(() -> DesktopActions.moveToTrash(tempFile));
    }

    @Test
    void moveToTrash_WithNullFilePath_ShouldThrowDesktopActionException() {
        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.moveToTrash((String) null)
        );
        assertEquals(ErrorMessage.FILE_PATH_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void moveToTrash_WithEmptyFilePath_ShouldThrowDesktopActionException() {
        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.moveToTrash("   ")
        );
        assertEquals(ErrorMessage.FILE_PATH_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void moveToTrash_WithNullFile_ShouldThrowDesktopActionException() {
        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.moveToTrash((File) null)
        );
        assertEquals(ErrorMessage.FILE_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void moveToTrash_WithNonExistentFile_ShouldThrowDesktopActionException() {
        File nonExistentFile = new File("nonexistent.txt");

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.moveToTrash(nonExistentFile)
        );
        assertEquals(ErrorMessage.FILE_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void moveToTrash_WhenDesktopNotSupported_ShouldThrowDesktopActionException() throws Exception {
        File tempFile = tempDir.resolve("test.txt").toFile();
        tempFile.createNewFile();
        when(Desktop.isDesktopSupported()).thenReturn(false);

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.moveToTrash(tempFile)
        );
        assertEquals(ErrorMessage.NOT_SUPPORTED.getMessage(), exception.getMessage());
    }

    @Test
    void moveToTrash_WhenMoveToTrashActionNotSupported_ShouldThrowDesktopActionException() throws Exception {
        File tempFile = tempDir.resolve("test.txt").toFile();
        tempFile.createNewFile();
        Desktop desktop = mock(Desktop.class);
        when(Desktop.isDesktopSupported()).thenReturn(true);
        when(Desktop.getDesktop()).thenReturn(desktop);
        when(desktop.isSupported(Desktop.Action.MOVE_TO_TRASH)).thenReturn(false);

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.moveToTrash(tempFile)
        );
        assertEquals(ErrorMessage.NOT_SUPPORTED.getMessage(), exception.getMessage());
    }

    @Test
    void openFileLocation_OnWindows_UsesExplorerCommand() throws Exception {
        File tempFile = tempDir.resolve("test.txt").toFile();
        tempFile.createNewFile();

        try (MockedConstruction<ProcessBuilder> mock = mockConstruction(ProcessBuilder.class,
                (processBuilder, context) -> {
                    Process process = mock(Process.class);
                    when(processBuilder.start()).thenReturn(process);
                })) {
            assertDoesNotThrow(() -> DesktopActions.openFileLocation(tempFile));
            assertEquals(1, mock.constructed().size());
        }
    }

    @Test
    void openFileLocation_FileParameter_ShouldHandleBothWindowsAndNonWindows() throws Exception {
        File tempFile = tempDir.resolve("test.txt").toFile();
        tempFile.createNewFile();

        try (MockedConstruction<ProcessBuilder> mock = mockConstruction(ProcessBuilder.class,
                (processBuilder, context) -> {
                    Process process = mock(Process.class);
                    when(processBuilder.start()).thenReturn(process);
                })) {

            assertDoesNotThrow(() -> DesktopActions.openFileLocation(tempFile));
        }
    }

    @Test
    void openFileLocation_StringParameter_ShouldHandleBothWindowsAndNonWindows() throws Exception {
        File tempFile = tempDir.resolve("test.txt").toFile();
        tempFile.createNewFile();

        try (MockedConstruction<ProcessBuilder> mock = mockConstruction(ProcessBuilder.class,
                (processBuilder, context) -> {
                    Process process = mock(Process.class);
                    when(processBuilder.start()).thenReturn(process);
                })) {

            assertDoesNotThrow(() -> DesktopActions.openFileLocation(tempFile.getAbsolutePath()));
        }
    }

    @Test
    void openFileDirectory_WithValidDirectory_ShouldNotThrowException() throws Exception {
        File directory = tempDir.toFile();
        Desktop desktop = mock(Desktop.class);
        when(Desktop.isDesktopSupported()).thenReturn(true);
        when(Desktop.getDesktop()).thenReturn(desktop);
        when(desktop.isSupported(Desktop.Action.OPEN)).thenReturn(true);
        doNothing().when(desktop).open(directory);

        assertDoesNotThrow(() -> DesktopActions.openFileDirectory(directory));
    }

    @Test
    void open_WithValidExecutablePath_ShouldNotThrowException() {
        String validExecutablePath = "C:/Program Files/MyApp/myapp.exe";

        try (MockedConstruction<ProcessBuilder> mock = mockConstruction(ProcessBuilder.class,
                (processBuilder, context) -> {
                    Process process = mock(Process.class);
                    when(processBuilder.start()).thenReturn(process);
                })) {

            assertDoesNotThrow(() -> DesktopActions.open(validExecutablePath));
            assertEquals(1, mock.constructed().size());
        }
    }

    @Test
    void open_WithNullExecutablePath_ShouldThrowDesktopActionException() {
        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.open(null)
        );
        assertEquals(ErrorMessage.EXECUTABLE_PATH_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void open_WithEmptyExecutablePath_ShouldThrowDesktopActionException() {
        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.open("   ")
        );
        assertEquals(ErrorMessage.EXECUTABLE_PATH_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void open_WhenIOExceptionOccurs_ShouldThrowDesktopActionException() {
        String executablePath = "C:/Program Files/MyApp/myapp.exe";

        try (MockedConstruction<ProcessBuilder> mock = mockConstruction(ProcessBuilder.class,
                (processBuilder, context) -> when(processBuilder.start()).thenThrow(new IOException("Process error")))) {

            DesktopActionException exception = assertThrows(
                    DesktopActionException.class,
                    () -> DesktopActions.open(executablePath)
            );
            assertEquals(ErrorMessage.CANNOT_START_PROCESS.getMessage(), exception.getMessage());
            assertNotNull(exception.getCause());
            assertInstanceOf(IOException.class, exception.getCause());
        }
    }

    @Test
    void open_WithSpacesInPath_ShouldHandleCorrectly() {
        String executablePathWithSpaces = "C:/Program Files/My App/myapp.exe";

        try (MockedConstruction<ProcessBuilder> mock = mockConstruction(ProcessBuilder.class,
                (processBuilder, context) -> {
                    Process process = mock(Process.class);
                    when(processBuilder.start()).thenReturn(process);
                })) {

            assertDoesNotThrow(() -> DesktopActions.open(executablePathWithSpaces));
            assertEquals(1, mock.constructed().size());
        }
    }


    @Test
    void testCreateShortcut_WithDefaultLocation_Success() throws DesktopActionException {
        String targetPath = "C:/Program Files/MyApp/myapp.exe";
        Path desktopPath = Path.of(System.getProperty("user.home"), "Desktop");
        String expectedLinkPath = desktopPath.toAbsolutePath() + "myapp.lnk";

        shellLinkMock.when(() -> ShellLink.createLink(targetPath, expectedLinkPath))
                .thenAnswer(invocation -> null);

        DesktopActions.createShortcut(targetPath);

        shellLinkMock.verify(() -> ShellLink.createLink(targetPath, expectedLinkPath), times(1));
    }

    @Test
    void testCreateShortcut_WithDefaultLocation_FileWithoutExtension() throws DesktopActionException {
        String targetPath = "C:/Program Files/MyApp/myapp";
        Path desktopPath = Path.of(System.getProperty("user.home"), "Desktop");
        String expectedLinkPath = desktopPath.toAbsolutePath() + "myapp.lnk";

        shellLinkMock.when(() -> ShellLink.createLink(targetPath, expectedLinkPath))
                .thenAnswer(invocation -> null);

        DesktopActions.createShortcut(targetPath);

        shellLinkMock.verify(() -> ShellLink.createLink(targetPath, expectedLinkPath), times(1));
    }

    @Test
    void testCreateShortcut_WithDefaultLocation_DirectoryPath() throws DesktopActionException {
        String targetPath = "C:/Projects/MyProject";
        Path desktopPath = Path.of(System.getProperty("user.home"), "Desktop");
        String expectedLinkPath = desktopPath.toAbsolutePath() + "MyProject.lnk";

        shellLinkMock.when(() -> ShellLink.createLink(targetPath, expectedLinkPath))
                .thenAnswer(invocation -> null);

        DesktopActions.createShortcut(targetPath);

        shellLinkMock.verify(() -> ShellLink.createLink(targetPath, expectedLinkPath), times(1));
    }

    @Test
    void testCreateShortcut_WithCustomLocation_Success() throws DesktopActionException {
        String targetPath = "C:/Program Files/MyApp/app.exe";
        String customLinkPath = "C:/Users/John/Shortcuts/app.lnk";

        shellLinkMock.when(() -> ShellLink.createLink(targetPath, customLinkPath))
                .thenAnswer(invocation -> null);

        DesktopActions.createShortcut(targetPath, customLinkPath);

        shellLinkMock.verify(() -> ShellLink.createLink(targetPath, customLinkPath), times(1));
    }

    @Test
    void testCreateShortcut_WithDefaultLocation_ThrowsException() {
        String targetPath = "C:/Program Files/MyApp/myapp.exe";
        Path desktopPath = Path.of(System.getProperty("user.home"), "Desktop");
        String expectedLinkPath = desktopPath.toAbsolutePath() + "myapp.lnk";
        IOException ioException = new IOException("Unable to write shortcut");

        shellLinkMock.when(() -> ShellLink.createLink(targetPath, expectedLinkPath))
                .thenThrow(ioException);

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.createShortcut(targetPath)
        );

        assertEquals("Unable to create desktop shortcut.", exception.getMessage());
        assertEquals(ioException, exception.getCause());
    }

    @Test
    void testCreateShortcut_WithCustomLocation_ThrowsException() {
        String targetPath = "C:/Program Files/MyApp/app.exe";
        String customLinkPath = "/invalid/path/app.lnk";
        IOException ioException = new IOException("Path does not exist");

        shellLinkMock.when(() -> ShellLink.createLink(targetPath, customLinkPath))
                .thenThrow(ioException);

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.createShortcut(targetPath, customLinkPath)
        );

        assertEquals("Unable to create desktop shortcut.", exception.getMessage());
        assertEquals(ioException, exception.getCause());
    }

    @Test
    void testCreateShortcut_WithNullTargetPath_DefaultLocation() {
        String targetPath = null;

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.createShortcut(targetPath)
        );

        assertEquals(ErrorMessage.TARGET_PATH_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void testCreateShortcut_WithEmptyTargetPath_DefaultLocation() {
        String targetPath = "";

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.createShortcut(targetPath)
        );

        assertEquals(ErrorMessage.TARGET_PATH_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void testCreateShortcut_WithWhitespaceTargetPath_DefaultLocation() {
        String targetPath = "   ";

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.createShortcut(targetPath)
        );

        assertEquals(ErrorMessage.TARGET_PATH_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void testCreateShortcut_WithNullTargetPath_CustomLocation() {
        String linkPath = "C:/Shortcuts/app.lnk";

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.createShortcut(null, linkPath)
        );

        assertEquals(ErrorMessage.TARGET_PATH_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void testCreateShortcut_WithValidTargetPath_NullLinkPath() {
        String targetPath = "C:/Program Files/MyApp/app.exe";
        String linkPath = null;

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.createShortcut(targetPath, linkPath)
        );

        assertEquals(ErrorMessage.LINK_PATH_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void testCreateShortcut_WithValidTargetPath_EmptyLinkPath() {
        String targetPath = "C:/Program Files/MyApp/app.exe";
        String linkPath = "";

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.createShortcut(targetPath, linkPath)
        );

        assertEquals(ErrorMessage.LINK_PATH_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void testCreateShortcut_WithValidTargetPath_WhitespaceLinkPath() {
        String targetPath = "C:/Program Files/MyApp/app.exe";
        String linkPath = "   ";

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.createShortcut(targetPath, linkPath)
        );

        assertEquals(ErrorMessage.LINK_PATH_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void testCreateShortcut_WithRelativePath() throws DesktopActionException {
        String targetPath = "./myapp.exe";
        String customLinkPath = "./shortcuts/myapp.lnk";

        shellLinkMock.when(() -> ShellLink.createLink(targetPath, customLinkPath))
                .thenAnswer(invocation -> null);

        DesktopActions.createShortcut(targetPath, customLinkPath);

        shellLinkMock.verify(() -> ShellLink.createLink(targetPath, customLinkPath), times(1));
    }

    @Test
    void testCreateShortcut_VerifiesUserHomeProperty() throws DesktopActionException {
        String customHome = tempDir.toString();
        System.setProperty("user.home", customHome);

        String targetPath = "C:/Program Files/MyApp/myapp.exe";
        Path desktopPath = Path.of(customHome, "Desktop");
        String expectedLinkPath = desktopPath.toAbsolutePath() + "myapp.lnk";

        shellLinkMock.when(() -> ShellLink.createLink(targetPath, expectedLinkPath))
                .thenAnswer(invocation -> null);

        DesktopActions.createShortcut(targetPath);

        shellLinkMock.verify(() -> ShellLink.createLink(targetPath, expectedLinkPath), times(1));
    }

    @Test
    void testCreateShortcut_WithSpecialCharactersInPath() throws DesktopActionException {
        String targetPath = "C:/Program Files/My App (2024)/app v1.0.exe";
        String customLinkPath = "C:/Shortcuts/My App v1.0.lnk";

        shellLinkMock.when(() -> ShellLink.createLink(targetPath, customLinkPath))
                .thenAnswer(invocation -> null);

        DesktopActions.createShortcut(targetPath, customLinkPath);

        shellLinkMock.verify(() -> ShellLink.createLink(targetPath, customLinkPath), times(1));
    }

    @Test
    void testCreateShortcut_WithLongPath() throws DesktopActionException {
        String targetPath = "C:/Very/Long/Path/That/Goes/Deep/Into/The/Directory/Structure/app.exe";
        String customLinkPath = "C:/Shortcuts/app.lnk";

        shellLinkMock.when(() -> ShellLink.createLink(targetPath, customLinkPath))
                .thenAnswer(invocation -> null);

        DesktopActions.createShortcut(targetPath, customLinkPath);

        shellLinkMock.verify(() -> ShellLink.createLink(targetPath, customLinkPath), times(1));
    }

    @Test
    void testCreateShortcut_WithBothNullPaths() {
        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.createShortcut(null, null)
        );

        assertEquals(ErrorMessage.TARGET_PATH_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void testCreateShortcut_WithBothEmptyPaths() {
        String targetPath = "";
        String linkPath = "";

        DesktopActionException exception = assertThrows(
                DesktopActionException.class,
                () -> DesktopActions.createShortcut(targetPath, linkPath)
        );

        assertEquals(ErrorMessage.TARGET_PATH_IS_NULL.getMessage(), exception.getMessage());
    }

    @Test
    void testCreateShortcut_ExtensionStripping_SingleDot() throws DesktopActionException {
        String targetPath = "C:/Program Files/MyApp/myapp.exe";
        Path desktopPath = Path.of(System.getProperty("user.home"), "Desktop");
        String expectedLinkPath = desktopPath.toAbsolutePath() + "myapp.lnk";

        shellLinkMock.when(() -> ShellLink.createLink(targetPath, expectedLinkPath))
                .thenAnswer(invocation -> null);

        DesktopActions.createShortcut(targetPath);

        shellLinkMock.verify(() -> ShellLink.createLink(targetPath, expectedLinkPath), times(1));
    }

    @Test
    void testCreateShortcut_ExtensionStripping_MultipleDots() throws DesktopActionException {
        String targetPath = "C:/Program Files/MyApp/my.app.v1.0.exe";
        Path desktopPath = Path.of(System.getProperty("user.home"), "Desktop");
        String expectedLinkPath = desktopPath.toAbsolutePath() + "my.app.v1.0.lnk";

        shellLinkMock.when(() -> ShellLink.createLink(targetPath, expectedLinkPath))
                .thenAnswer(invocation -> null);

        DesktopActions.createShortcut(targetPath);

        shellLinkMock.verify(() -> ShellLink.createLink(targetPath, expectedLinkPath), times(1));
    }

    @Test
    void testCreateShortcut_WithDotInDirectoryName() throws DesktopActionException {
        String targetPath = "C:/Program.Files/MyApp/myapp.exe";
        Path desktopPath = Path.of(System.getProperty("user.home"), "Desktop");
        String expectedLinkPath = desktopPath.toAbsolutePath() + "myapp.lnk";

        shellLinkMock.when(() -> ShellLink.createLink(targetPath, expectedLinkPath))
                .thenAnswer(invocation -> null);

        DesktopActions.createShortcut(targetPath);

        shellLinkMock.verify(() -> ShellLink.createLink(targetPath, expectedLinkPath), times(1));
    }
}