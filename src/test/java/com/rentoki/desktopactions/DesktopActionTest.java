package com.rentoki.desktopactions;

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

    @BeforeEach
    void setUp() {
        desktopMock = mockStatic(Desktop.class);
    }

    @AfterEach
    void tearDown() {
        if (desktopMock != null) {
            desktopMock.close();
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
}