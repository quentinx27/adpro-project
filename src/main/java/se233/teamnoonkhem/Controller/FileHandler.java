package se233.teamnoonkhem.Controller;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileHandler {
    private List<File> selectedFiles = new ArrayList<>(); // Initialize empty list
    private File saveDirectory;

    // Method for choosing files (.png, .jpg, and .zip)
    public void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        // Allow .png, .jpg, and .zip file types
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"),
                new FileChooser.ExtensionFilter("Zip Files", "*.zip")
        );
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());

        if (files != null) {
            selectedFiles.clear(); // Clear the list before adding new files
            selectedFiles.addAll(files); // Add all selected files to the list
        }
    }


    // Get the list of selected files
    public List<File> getSelectedFiles() {
        return selectedFiles;
    }

    // Get directory to save files
    public File getSaveDirectory() {
        if (saveDirectory == null) {
            chooseSaveDirectory();
        }
        return saveDirectory;
    }

    // Choose the directory for saving files
    public void chooseSaveDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Save Directory");
        saveDirectory = directoryChooser.showDialog(new Stage());
    }

    // Extract a single zip file with progress shown in terminal
    public List<File> extractZip(File zipFile, String outputDir) throws IOException {
        List<File> extractedFiles = new ArrayList<>();

        // Clear the output directory before extracting new files (optional based on your need)
        File outputDirectory = new File(outputDir);
        if (outputDirectory.exists()) {
            for (File file : outputDirectory.listFiles()) {
                file.delete(); // Remove old files
            }
        }

        // Count total entries in the zip file for progress calculation
        int totalEntries = countTotalEntries(zipFile);
        int processedEntries = 0;

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (zipEntry.getName().endsWith(".png") || zipEntry.getName().endsWith(".jpg")) {
                    File outputFile = new File(outputDir, zipEntry.getName());
                    outputFile.getParentFile().mkdirs();

                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                        extractedFiles.add(outputFile);
                    }
                }
                zis.closeEntry();

                // Update progress after extracting each file
                processedEntries++;
                displayProgress(processedEntries, totalEntries);
            }
        }
        return extractedFiles;
    }

    // Count total entries in the zip file for progress calculation
    private int countTotalEntries(File zipFile) throws IOException {
        int totalEntries = 0;
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            while (zis.getNextEntry() != null) {
                totalEntries++;
            }
        }
        return totalEntries;
    }

    // Display progress in the terminal
    private void displayProgress(int processedEntries, int totalEntries) {
        int progressPercentage = (int) ((double) processedEntries / totalEntries * 100);
        int progressBarLength = 40; // Length of the progress bar
        int filledLength = (int) (progressBarLength * ((double) processedEntries / totalEntries));

        StringBuilder progressBar = new StringBuilder();
        for (int i = 0; i < filledLength; i++) {
            progressBar.append("x");
        }
        for (int i = filledLength; i < progressBarLength; i++) {
            progressBar.append(".");
        }

        // Print progress bar and percentage on the same line
        System.out.print("\rProgress: [" + progressBar.toString() + "] " + progressPercentage + "% (" + processedEntries + "/" + totalEntries + ")");

        // Print a new line when the progress is complete
        if (processedEntries == totalEntries) {
            System.out.println(); // Move to the next line after completion
        }
    }

}
