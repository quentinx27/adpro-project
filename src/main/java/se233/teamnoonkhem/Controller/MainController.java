package se233.teamnoonkhem.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {

    @FXML
    private Button startCropButton, confirmCropButton, revertToOriginalButton, zoomInButton, zoomOutButton, resetZoomButton, clearButton, saveButton, previousButton, nextButton, detectEdgesButton, detectAllButton,BatchcropButton;
    @FXML
    private ComboBox<String> edgeAlgorithmChoice, kernelSizeChoice, grayscaleMethod;
    @FXML
    private ListView<String> fileListView;
    @FXML
    private ImageView imageView;
    @FXML
    public ScrollPane imageScrollPane;
    @FXML
    public BorderPane imagePane;
    @FXML
    private Label chooseAndDropLabel, chooseAndDroptxt, kernelSizetxt, strengthtxt, grayscaletxt, pressEtxt;

    // Add Slider and Label for strength
    @FXML
    private Slider strengthSlider;
    @FXML
    private Label strengthValueLabel;

    private Image originalImage;
    static List<File> selectedFiles = new ArrayList<>();
    private static final Map<File, Image> fileImageMap = new HashMap<>(); // Map for storing processed images
    private final ZoomHandler zoomHandler = new ZoomHandler();
    private CropHandler cropHandler;
    private final FileHandler fileHandler = new FileHandler();
    private final EdgeDetectionHandler edgeDetectionHandler = new EdgeDetectionHandler();

    public static int currentIndex = -1;

    @FXML
    private void initialize() {
        // CropHandler can now access originalImage
        cropHandler = new CropHandler(imageView, imagePane, imageScrollPane, chooseAndDropLabel, originalImage, pressEtxt);

        setupButtons();
        setupComboBoxes();
        setupFileListView();
        setUpDragAndDrop();
        setupStrengthSlider();
        setupKeyPress();
        chooseAndDroptxt.setVisible(true);
        previousButton.setDisable(true);
        nextButton.setDisable(true);

        imagePane.minWidthProperty().bind(imageScrollPane.widthProperty());
        imagePane.minHeightProperty().bind(imageScrollPane.heightProperty());

        kernelSizeChoice.setVisible(false);
        kernelSizetxt.setVisible(false);

        strengthSlider.setVisible(false);
        strengthValueLabel.setVisible(false);
        strengthtxt.setVisible(false);

        grayscaleMethod.setVisible(false);
        grayscaletxt.setVisible(false);

        pressEtxt.setVisible(false);
    }

    private void setupButtons() {
        startCropButton.setOnAction(event -> cropHandler.startCrop());
        confirmCropButton.setOnAction(event -> cropHandler.confirmCrop());
        revertToOriginalButton.setOnAction(event -> revertToOriginal());
        saveButton.setOnAction(event -> handleSave());
        zoomInButton.setOnAction(event -> zoomHandler.zoomIn(imageView));
        zoomOutButton.setOnAction(event -> zoomHandler.zoomOut(imageView));
        resetZoomButton.setOnAction(event -> zoomHandler.resetZoom(imageView));
        clearButton.setOnAction(event -> handleClearFiles());
        previousButton.setOnAction(event -> handlePrevious());
        nextButton.setOnAction(event -> handleNext());
        detectEdgesButton.setOnAction(event -> applyEdgeDetection());
        detectAllButton.setOnAction(event -> applyDetectAll()); // New button action to call applyDetectAll()
        BatchcropButton.setOnAction(event -> cropHandler.batchCrop());
    }

    private void setupKeyPress() {
        // Ensure the imagePane is focusable
        imagePane.setFocusTraversable(true);

        // Set up key event handling for the "E" key to cancel crop
        imagePane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.E && cropHandler.isCroppingActive()) {
                cropHandler.cancelCrop();  // Call the cancel crop method when "E" is pressed
            }
        });
    }

    private void setupComboBoxes() {
        edgeAlgorithmChoice.getItems().addAll("Robert-Cross", "Sobel", "Laplacian");
        grayscaleMethod.getItems().addAll("Average", "Luminosity", "Custom");
        grayscaleMethod.setValue("average");
        kernelSizeChoice.getItems().addAll("3*3", "5*5");
        kernelSizeChoice.setValue("3*3"); // Default to 3x3

        edgeAlgorithmChoice.setOnAction(event -> {
            String selectedMethod = edgeAlgorithmChoice.getValue();
            if ("Robert-Cross".equals(selectedMethod)) {
                strengthSlider.setVisible(true);
                strengthValueLabel.setVisible(true);
                strengthtxt.setVisible(true);
            } else {
                strengthSlider.setVisible(false);
                strengthValueLabel.setVisible(false);
                strengthtxt.setVisible(false);
            }

            if ("Laplacian".equals(selectedMethod)) {
                kernelSizeChoice.setVisible(true);
                kernelSizetxt.setVisible(true);
            } else {
                kernelSizeChoice.setVisible(false);
                kernelSizetxt.setVisible(false);
            }

            if ("Sobel".equals(selectedMethod)) {
                grayscaleMethod.setVisible(true);
                grayscaletxt.setVisible(true);
            } else {
                grayscaleMethod.setVisible(false);
                grayscaletxt.setVisible(false);
            }
        });
    }

    private void setupStrengthSlider() {
        strengthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int strengthValue = newValue.intValue();
            strengthValueLabel.setText(String.valueOf(strengthValue));
        });
    }

    private void setupFileListView() {
        fileListView.setOnMouseClicked(event -> handleFileSelection());
    }

    public static void updateImage(File file, Image newImage) {
        int index = selectedFiles.indexOf(file); // Find the correct index based on the file
        if (index >= 0 && index < selectedFiles.size()) {
            synchronized (MainController.class) {
                fileImageMap.put(file, newImage);  // Update the correct image in the map
            }
            System.out.println("Image updated for file: " + file.getName() + " at index: " + index);
        } else {
            System.out.println("Invalid index for updating image.");
        }
    }

    public void revertToOriginal() {
        if (currentIndex >= 0 && currentIndex < selectedFiles.size()) {
            File currentFile = selectedFiles.get(currentIndex);

            // ตรวจสอบว่ารูปภาพต้นฉบับอยู่ใน fileImageMap หรือไม่
            if (fileImageMap.containsKey(currentFile)) {
                // ดึงภาพต้นฉบับจาก fileImageMap
                Image originalImage = new Image(currentFile.toURI().toString());

                // แสดงภาพต้นฉบับใน imageView
                imageView.setImage(originalImage);

                // อัปเดตรูปภาพใน fileImageMap กลับไปเป็นต้นฉบับ
                MainController.updateImage(currentFile, originalImage); // ใช้ File แทน currentIndex

                System.out.println("Reverted to original image for file: " + currentFile.getName());
            } else {
                System.out.println("Original image not found in fileImageMap.");
            }

            chooseAndDropLabel.setVisible(true);
            cropHandler.cancelCrop();  // ยกเลิกการครอบถ้ามีการครอบอยู่
        } else {
            System.out.println("No image available to revert to.");
        }
    }

    private void setUpDragAndDrop() {
        chooseAndDropLabel.setOnDragOver(event -> {
            if (event.getGestureSource() != chooseAndDropLabel && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        chooseAndDropLabel.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                handleDroppedFiles(db.getFiles());
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });

        chooseAndDropLabel.setOnMouseClicked(event -> handleChooseFiles());
    }

    private void applyEdgeDetection() {
        if (currentIndex >= 0 && currentIndex < selectedFiles.size()) {
            File currentFile = selectedFiles.get(currentIndex);  // Get the current file

            // ตรวจสอบว่ามีภาพที่ประมวลผลไว้ใน fileImageMap หรือไม่
            Image originalImage = fileImageMap.containsKey(currentFile)
                    ? fileImageMap.get(currentFile)
                    : new Image(currentFile.toURI().toString());  // ถ้าไม่มีภาพใน fileImageMap ให้ดึงภาพจากไฟล์

            // อัปเดต imageView ด้วยภาพที่ประมวลผลล่าสุดหรือภาพต้นฉบับ
            imageView.setImage(originalImage);

            String selectedMethod = edgeAlgorithmChoice.getValue();
            String selectedKernelSize = kernelSizeChoice.getValue();
            String selectedGrayscaleMethod = grayscaleMethod.getValue();
            int strengthValue = (int) strengthSlider.getValue();

            if (selectedMethod != null && (!"Laplacian".equals(selectedMethod) || selectedKernelSize != null)) {
                int[][] grayscaleArray = ImageConverter.convertToGrayscaleArray(originalImage);
                int[][] edgeDetectedArray = edgeDetectionHandler.applyEdgeDetection(
                        selectedMethod, grayscaleArray, selectedKernelSize, strengthValue, selectedGrayscaleMethod
                );
                Image edgeDetectedImage = ImageConverter.convertToImage(edgeDetectedArray);

                // อัปเดตรูปภาพที่ประมวลผลแล้วกลับไปยัง fileImageMap
                edgeDetectionHandler.saveEdgeDetectedImage(currentFile, edgeDetectedImage);

                // แสดงผลภาพที่ประมวลผลแล้ว
                imageView.setImage(edgeDetectedImage);
            } else {
                System.out.println("No edge detection method or kernel size selected.");
            }
        } else {
            System.out.println("No image loaded for edge detection.");
        }
    }
    


    private void applyDetectAll() {
        if (!selectedFiles.isEmpty()) {
            // Get selected method, kernel size, grayscale method, and strength value
            String selectedMethod = edgeAlgorithmChoice.getValue();
            String selectedKernelSize = kernelSizeChoice.getValue();
            String selectedGrayscaleMethod = grayscaleMethod.getValue();
            int strengthValue = (int) strengthSlider.getValue();

            // Check if valid method and kernel size are selected
            if (selectedMethod != null && (!"Laplacian".equals(selectedMethod) || selectedKernelSize != null)) {
                // Create a Map of files to images from selectedFiles
                Map<File, Image> fileImageMap = new HashMap<>();
                for (File file : selectedFiles) {
                    Image image = new Image(file.toURI().toString());
                    fileImageMap.put(file, image);
                }

                // Call detectAll from EdgeDetectionHandler to process all images
                edgeDetectionHandler.detectAll(fileImageMap, selectedMethod, selectedKernelSize, strengthValue, selectedGrayscaleMethod);

                // After processing all files, update the ImageView with the first image's result
                if (!selectedFiles.isEmpty()) {
                    Image processedImage = fileImageMap.get(selectedFiles.getFirst());
                    if (processedImage != null) {
                        imageView.setImage(processedImage);  // Display the first processed image
                    }
                }
            } else {
                System.out.println("No edge detection method or kernel size selected.");
            }
        } else {
            System.out.println("No images selected.");
        }
    }


    private void handleDroppedFiles(List<File> files) {
        try{
        for (File file : files) {
            if (file.getName().endsWith(".png") || file.getName().endsWith(".jpg")) {
                selectedFiles.add(file);
                fileListView.getItems().add(file.getName());
                originalImage = new Image(file.toURI().toString());
                imageView.setImage(originalImage);
                zoomHandler.resetZoom(imageView);
                chooseAndDroptxt.setVisible(false);

                cropHandler.setOriginalImage(originalImage);
                cropHandler.setOriginalFile(file);
                edgeDetectionHandler.setOriginalFile(file);
            } else if (file.getName().endsWith(".zip")) {
                handleZipFile(file);
            }
        }
        updateNavigationButtons();
        } catch (Exception e) {
            ExceptionHandler.OperationErrorException operationErrorException = new ExceptionHandler.OperationErrorException("Error handling dropped files.", e);
            operationErrorException.printOperationErrorStackTrace();  // แสดง Stack Trace
        }
    }

    private void handleSave() {
        saveButton.setDisable(true);
        //IOExceptions OperationErrorException
        try {
            // รับ Directory ที่ใช้บันทึกไฟล์
            File saveDirectory = fileHandler.getSaveDirectory();
            if (saveDirectory == null) {
                throw new ExceptionHandler.OperationErrorException("Save directory is not selected.");
            }
            SaveHandler saveHandler = new SaveHandler(saveDirectory);

            // สร้าง Map ที่เก็บไฟล์และรูปภาพที่ถูกแก้ไข
            Map<File, Image> fileImageMap = new HashMap<>();

            // ตรวจสอบและเพิ่มไฟล์พร้อมรูปภาพที่ถูกแก้ไขใน Map
            for (File file : selectedFiles) {
                // เรียกใช้ getEditedImageForFile เพื่อตรวจสอบว่าไฟล์ถูกแก้ไขหรือไม่
                Image editedImage = getEditedImageForFile(file);
                if (editedImage != null) {
                    // ถ้ามีการแก้ไขภาพ, ใส่ลงใน Map
                    fileImageMap.put(file, editedImage);
                } else {
                    // ถ้าไม่มีการแก้ไข, ใส่ไฟล์ต้นฉบับ
                    fileImageMap.put(file, new Image(file.toURI().toString()));
                }
            }

            // บันทึกตามจำนวนไฟล์ที่มีอยู่
            if (fileImageMap.size() == 1) {
                saveHandler.saveAsPng(fileImageMap);
            } else if (fileImageMap.size() > 1) {
                saveHandler.saveAsZip(fileImageMap);
            }
        } catch (IOException e) {
            ExceptionHandler.OperationErrorException operationErrorException = new ExceptionHandler.OperationErrorException("Saving failed due to an IO error.", e);
            operationErrorException.printOperationErrorStackTrace();  // แสดง Stack Trace
        } finally {
            saveButton.setDisable(false);
        }
    }

    private Image getEditedImageForFile(File file) {
        // ตรวจสอบว่ามีรูปภาพที่ถูกแก้ไขใน fileImageMap หรือไม่
        if (fileImageMap.containsKey(file)) {
            return fileImageMap.get(file);  // ส่งคืนรูปภาพที่ถูกแก้ไข
        }
        return null;  // ถ้าไม่มีการแก้ไขรูปภาพ ส่งคืน null
    }



    private void handleFileSelection() {
        String selectedFileName = fileListView.getSelectionModel().getSelectedItem();
        if (selectedFileName != null) {
            //Null Pointer Exceptions OperationErrorException
            try{
            for (int i = 0; i < selectedFiles.size(); i++) {
                File file = selectedFiles.get(i);
                if (file.getName().equals(selectedFileName)) {
                    currentIndex = i;
                    loadImage(file);
                    updateNavigationButtons();
                    break;
                }
            }

            } catch (NullPointerException e) {
                ExceptionHandler.OperationErrorException operationErrorException = new ExceptionHandler.OperationErrorException("NullPointerException while selecting the file: " + selectedFileName, e);
                operationErrorException.printOperationErrorStackTrace();  // แสดง Stack Trace
                }
        }
    }

    private void loadImage(File file) {
        //Null Pointer Exceptions
        try {
            Image image;
            if (fileImageMap.containsKey(file)) {
                image = fileImageMap.get(file); // Load the processed image if available
            } else {
                image = new Image(file.toURI().toString()); // Load the original image
            }
            imageView.setImage(image);
            zoomHandler.resetZoom(imageView);
            originalImage = image;
            cropHandler.setOriginalImage(originalImage);
        } catch (NullPointerException e) {
            System.out.println("Error loading image: " + e.getMessage());
        }
    }

    private void handleChooseFiles() {
        fileHandler.chooseFile();
        List<File> chosenFiles = fileHandler.getSelectedFiles();
        if (chosenFiles != null) {
            chooseAndDroptxt.setVisible(false);
            for (File file : chosenFiles) {
                if (file.getName().endsWith(".png") || file.getName().endsWith(".jpg")) {
                    selectedFiles.add(file);
                    fileListView.getItems().add(file.getName());
                    originalImage = new Image(file.toURI().toString());
                    imageView.setImage(originalImage);
                    zoomHandler.resetZoom(imageView);
                    cropHandler.setOriginalImage(originalImage);
                    cropHandler.setOriginalFile(file);
                    edgeDetectionHandler.setOriginalFile(file);
                } else if (file.getName().endsWith(".zip")) {
                    handleZipFile(file);
                }
            }
            updateNavigationButtons();
        }
    }

    private void handleClearFiles() {
        // Clear the list view
        fileListView.getItems().clear();

        // Clear the selected files list
        selectedFiles.clear();

        // Clear the image view
        imageView.setImage(null);

        // Clear the fileImageMap to remove any processed images
        fileImageMap.clear();

        // Reset other related components
        chooseAndDroptxt.setVisible(true);
        currentIndex = -1;

        // Update navigation buttons
        updateNavigationButtons();
        cropHandler.cancelCrop();
    }

    private void handleNext() {
        //ArrayIndexOutOfBoundsException
        try {
        if (currentIndex < selectedFiles.size() - 1) {
            currentIndex++;
            fileListView.getSelectionModel().select(currentIndex);
            loadImage(selectedFiles.get(currentIndex));
            updateNavigationButtons();
        }
        }catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Error: Index out of bounds while navigating to the next file. " + e.getMessage());
        }
    }

    private void handlePrevious() {
        //ArrayIndexOutOfBoundsException
        try {
            if (currentIndex > 0) {
                currentIndex--;
                fileListView.getSelectionModel().select(currentIndex);
                loadImage(selectedFiles.get(currentIndex));
                updateNavigationButtons();
            }
        }catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Error: Index out of bounds while navigating to the previous file. " + e.getMessage());
        }

    }

    private void updateNavigationButtons() {
        previousButton.setDisable(currentIndex <= 0);
        nextButton.setDisable(currentIndex >= selectedFiles.size() - 1);
    }

    private void handleZipFile(File file) {
        try {
            extractZip(file);
        } catch (Exception e) {
            System.out.println("Error extracting ZIP file: " + e.getMessage());
        }
    }

    private void extractZip(File zipFile) {
        try {
            String outputDir = System.getProperty("java.io.tmpdir");
            List<File> extractedFiles = fileHandler.extractZip(zipFile, outputDir);
            for (File extractedFile : extractedFiles) {
                if (extractedFile.getName().endsWith(".png") || extractedFile.getName().endsWith(".jpg")) {
                    selectedFiles.add(extractedFile);
                    fileListView.getItems().add(extractedFile.getName());
                    chooseAndDroptxt.setVisible(false);
                }
            }
            if (!extractedFiles.isEmpty()) {
                loadImage(extractedFiles.getFirst());
                currentIndex = selectedFiles.size() - extractedFiles.size();
                updateNavigationButtons();
            }
        } catch (Exception e) {
            System.out.println("Error extracting ZIP file: " + e.getMessage());
        }
    }
}
