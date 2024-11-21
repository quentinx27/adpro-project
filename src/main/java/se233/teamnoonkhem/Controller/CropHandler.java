package se233.teamnoonkhem.Controller;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import se233.teamnoonkhem.model.Cropper.ResizableRectangle;
import javafx.application.Platform;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class CropHandler {

    private final ImageView imageView;
    private final BorderPane imagePane;
    private final ScrollPane imageScrollPane;
    private final Label chooseAndDropLabel;
    private final Label pressEtxt;
    private Image originalImage; // เก็บภาพต้นฉบับ
    private ResizableRectangle selectionRectangle;
    private Rectangle darkArea;
    private boolean isAreaSelected = false;
    private boolean isCroppingActive = false;
    private boolean isBatchCroppingActive = false; // เพิ่มตัวแปรเพื่อตรวจสอบการทำงาน batch crop
    private File originalFile;
    private Bounds cropBounds; // เพิ่มตัวแปรเพื่อเก็บสเกลการครอป
    private ExecutorService batchCropExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final AtomicInteger processedImages = new AtomicInteger(0); // เพื่อการนับจำนวนภาพที่ประมวลผลแล้ว

    public CropHandler(ImageView imageView, BorderPane imagePane, ScrollPane imageScrollPane, Label chooseAndDropLabel, Image originalImage, Label pressEtxt) {
        this.imageView = imageView;
        this.imagePane = imagePane;
        this.imageScrollPane = imageScrollPane;
        this.chooseAndDropLabel = chooseAndDropLabel;
        this.originalImage = originalImage; // ตั้งค่า originalImage
        this.pressEtxt = pressEtxt;
        setupCropArea();
    }

    public void setOriginalFile(File file) {
        this.originalFile = file; // ตั้งค่าไฟล์ต้นฉบับ
    }

    public void setOriginalImage(Image originalImage) {
        this.originalImage = originalImage; // อัพเดต originalImage เมื่อเปลี่ยนภาพ
    }

    private void setupCropArea() {
        darkArea = new Rectangle();
        darkArea.setFill(Color.color(0, 0, 0, 0.5));
        darkArea.setVisible(false);
        imagePane.getChildren().add(darkArea);
    }

    public void startCrop() {
        if (isCroppingActive) {
            System.out.println("Cropping is already in progress.");
            return;
        }

        if (imageView.getImage() == null) {
            System.out.println("No image available to crop.");
            return;
        }

        isCroppingActive = true;
        chooseAndDropLabel.setVisible(false);
        imageScrollPane.setPannable(false);
        removeExistingSelection();

        double imageWidth = imageView.getFitWidth();
        double imageHeight = imageView.getFitHeight();
        double rectWidth = imageWidth / 2;
        double rectHeight = imageHeight / 2;

        double rectX = (imageWidth - rectWidth) / 2;
        double rectY = (imageHeight - rectHeight) / 2;

        selectionRectangle = new ResizableRectangle(rectX, rectY, rectWidth, rectHeight, imagePane, this::updateDarkArea);
        isAreaSelected = true;
        updateDarkArea();
        imagePane.requestFocus();
        pressEtxt.setVisible(true);
    }

    public void confirmCrop() {
        if (isAreaSelected && selectionRectangle != null) {
            imageScrollPane.setPannable(true);
            cropBounds = selectionRectangle.getBoundsInParent(); // เก็บขนาดการครอปไว้ในตัวแปร

            removeExistingSelection();
            selectionRectangle = null;
            isAreaSelected = false;
            darkArea.setVisible(false);
            isCroppingActive = false;
            chooseAndDropLabel.setVisible(true);
            pressEtxt.setVisible(false);

            // เพียงแทนที่ไฟล์ใน selectedFiles แต่ยังไม่เซฟทันที
            WritableImage croppedImage = cropImage(cropBounds);
            if (MainController.currentIndex >= 0) {
                File currentFile = MainController.selectedFiles.get(MainController.currentIndex);
                MainController.updateImage(currentFile, croppedImage); // อัปเดตรูปภาพใน selectedFiles
            }
        }
    }

    private WritableImage cropImage(Bounds bounds) {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight()));

        WritableImage croppedImageWritable = new WritableImage((int) bounds.getWidth(), (int) bounds.getHeight());
        imageView.snapshot(parameters, croppedImageWritable);

        imageView.setImage(croppedImageWritable); // แสดงผลภาพที่ถูกครอบ
        return croppedImageWritable;
    }

    public void cancelCrop() {
        if (isCroppingActive) {
            imageScrollPane.setPannable(true);
            removeExistingSelection();
            selectionRectangle = null;
            isAreaSelected = false;
            darkArea.setVisible(false);
            isCroppingActive = false;
            chooseAndDropLabel.setVisible(true);
            pressEtxt.setVisible(false);
            System.out.println("Cropping operation canceled.");
        }
    }

    private void updateDarkArea() {
        if (selectionRectangle != null) {
            double imageWidth = imageView.getFitWidth();
            double imageHeight = imageView.getFitHeight();
            double rectX = selectionRectangle.getX();
            double rectY = selectionRectangle.getY();
            double rectWidth = selectionRectangle.getWidth();
            double rectHeight = selectionRectangle.getHeight();

            darkArea.setWidth(imageWidth);
            darkArea.setHeight(imageHeight);
            darkArea.setLayoutX(0);
            darkArea.setLayoutY(0);

            Rectangle outerRect = new Rectangle(0, 0, imageWidth, imageHeight);
            Rectangle innerRect = new Rectangle(rectX, rectY, rectWidth, rectHeight);
            Shape clippedArea = Shape.subtract(outerRect, innerRect);

            darkArea.setClip(clippedArea);
            darkArea.setVisible(true);
            // กำหนด cropBounds ทันทีหลังจากที่เริ่มการครอป
            cropBounds = selectionRectangle.getBoundsInParent();
        }
    }

    private void removeExistingSelection() {
        if (selectionRectangle != null) {
            selectionRectangle.removeResizeHandles(imagePane);
            imagePane.getChildren().remove(selectionRectangle);
        }
    }

    public boolean isCroppingActive() {
        return isCroppingActive;
    }

    // Method batchCrop for multithreaded cropping
    // Method batchCrop for multithreaded cropping
    public void batchCrop() {
        if (cropBounds == null) {
            System.out.println("No crop bounds selected. Start cropping first.");
            return;
        }

        if (isBatchCroppingActive) {
            System.out.println("Batch cropping is already in progress.");
            return;
        }

        isBatchCroppingActive = true;

        // ตรวจสอบและรีเซ็ต ExecutorService หากถูกปิด
        if (batchCropExecutor.isShutdown()) {
            batchCropExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }

        int totalImages = MainController.selectedFiles.size();
        int availableCores = Runtime.getRuntime().availableProcessors();
        processedImages.set(0); // รีเซ็ตตัวนับภาพที่ประมวลผลแล้ว
        System.out.println("Available cores: " + availableCores);

        // Execute cropping in multithread using thread pool
        for (File file : MainController.selectedFiles) {
            batchCropExecutor.execute(() -> {
                //IllegalStateException
                try {
                    String threadName = Thread.currentThread().getName();
                    System.out.println(threadName + " is processing: " + file.getName());

                    // Load the image for this file (on background thread)
                    Image image = new Image(file.toURI().toString());

                    // Run image cropping and UI update on the FX Application Thread
                    Platform.runLater(() -> {
                        try {
                            // Set the image to the ImageView for display (must be on JavaFX thread)
                            imageView.setImage(image);

                            // Perform the crop operation with the predefined bounds
                            WritableImage croppedImage = cropImage(cropBounds);

                            // Save the cropped image back to the MainController's map
                            MainController.updateImage(file, croppedImage);

                            // Update the processed images count and display status
                            int processedCount = processedImages.incrementAndGet();
                            System.out.println("Image updated for file: " + file.getName());
                            System.out.println(threadName + " completed: " + file.getName());
                            System.out.println("Cores: " + availableCores);
                            System.out.println("Processed images: " + processedCount + " / " + totalImages);
                            System.out.println("Active threads: " + Thread.activeCount());
                            System.out.println("====================================");

                            // When all images are processed, reset batch cropping state
                            if (processedCount == totalImages) {
                                isBatchCroppingActive = false;
                            }

                        } catch (Exception e) {
                            System.out.println("Error cropping image for file: " + file.getName() + " - " + e.getMessage());
                        }
                    });

                } catch (IllegalStateException e) {
                    System.out.println("Error processing file: " + file.getName() + " - " + e.getMessage());
                }
            });
        }

        cancelCrop();
        System.out.println("Batch cropping started for all images.");
        // Request layout refresh to update the pane
        Platform.runLater(imagePane::requestLayout);
    }
}
