package se233.teamnoonkhem.Controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SaveHandler {
    private final File saveDirectory;

    public SaveHandler(File saveDirectory) {
        this.saveDirectory = saveDirectory;
    }

    // Method to save images as PNG with batch processing and ProgressBar
    public void saveAsPng(Map<File, Image> fileImageMap) throws IOException {
        int totalFiles = fileImageMap.size();
        int processedFiles = 0;

        for (File file : fileImageMap.keySet()) {
            // Show dialog to get the PNG file name from the user
            TextInputDialog dialog = new TextInputDialog(file.getName().replaceFirst("\\.\\w+$", ""));
            dialog.setTitle("Save PNG File");
            dialog.setHeaderText("Enter the name of the PNG file");
            dialog.setContentText("File name:");

            Optional<String> result = dialog.showAndWait();
            String pngFileName = result.orElse(file.getName().replaceFirst("\\.\\w+$", ""));

            if (!pngFileName.endsWith(".png")) {
                pngFileName += ".png";
            }

            File outputFile = new File(saveDirectory, pngFileName);
            Image imageToSave = fileImageMap.get(file);

            // Save the image as PNG
            if (imageToSave != null) {
                try {
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageToSave, null);
                    ImageIO.write(bufferedImage, "png", outputFile);
                    System.out.println("Saved: " + outputFile.getAbsolutePath());
                } catch (IOException e) {
                    System.out.println("Failed to save image: " + e.getMessage());
                }
            } else {
                System.out.println("No image to save for: " + file.getName());
            }

            // Update and display progress in the terminal
            processedFiles++;
            displayProgress(processedFiles, totalFiles);
        }
    }

    // Method to save images as ZIP with batch processing and ProgressBar
    public void saveAsZip(Map<File, Image> fileImageMap) throws IOException {
        // Show dialog to get the ZIP file name from the user
        TextInputDialog dialog = new TextInputDialog("teamNoonKhem");
        dialog.setTitle("Save Zip File");
        dialog.setHeaderText("Enter the name of the zip file");
        dialog.setContentText("File name:");

        Optional<String> result = dialog.showAndWait();
        String zipFileName = result.orElse("default_name");

        if (!zipFileName.endsWith(".zip")) {
            zipFileName += ".zip";
        }

        // Create the ZIP file
        File zipFile = new File(saveDirectory, zipFileName);
        int totalFiles = fileImageMap.size();
        int processedFiles = 0;

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (File file : fileImageMap.keySet()) {
                String entryName = file.getName().replaceFirst("\\.\\w+$", "") + ".png";
                ZipEntry zipEntry = new ZipEntry(entryName);
                zos.putNextEntry(zipEntry);

                Image imageToSave = fileImageMap.get(file);

                if (imageToSave != null) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageToSave, null);
                    ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
                    byte[] imageData = byteArrayOutputStream.toByteArray();
                    zos.write(imageData);
                }

                zos.closeEntry();
                System.out.println("Added to zip: " + file.getName());

                // Update and display progress in the terminal
                processedFiles++;
                displayProgress(processedFiles, totalFiles);
            }
        }

        System.out.println("Saved zip file: " + zipFile.getAbsolutePath());
    }

    // Display progress in the terminal
    private void displayProgress(int processedFiles, int totalFiles) {
        int progressPercentage = (int) ((double) processedFiles / totalFiles * 100);
        int progressBarLength = 40; // Length of the progress bar
        int filledLength = (int) (progressBarLength * ((double) processedFiles / totalFiles));

        StringBuilder progressBar = new StringBuilder();
        for (int i = 0; i < filledLength; i++) {
            progressBar.append("x");
        }
        for (int i = filledLength; i < progressBarLength; i++) {
            progressBar.append(".");
        }

        // Clear the previous line and print the new progress bar
        System.out.print("\r");
        System.out.println("Saving progress");
        System.out.println(progressBar);
        System.out.println("Percent: " + progressPercentage + "%");
        System.out.println("Processed: " + processedFiles + "/" + totalFiles);

        // Print a new line when the progress is complete
        if (processedFiles == totalFiles) {
            System.out.println("Save completed!");
        }
    }
}
