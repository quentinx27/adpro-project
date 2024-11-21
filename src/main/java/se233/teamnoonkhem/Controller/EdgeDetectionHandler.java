package se233.teamnoonkhem.Controller;

import javafx.scene.image.Image;
import se233.teamnoonkhem.model.Edge.LaplacianEdge;
import se233.teamnoonkhem.model.Edge.RobertsCrossEdge;
import se233.teamnoonkhem.model.Edge.SobelEdge;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EdgeDetectionHandler {

    private File originalFile;

    public void setOriginalFile(File file) {
        this.originalFile = file;
    }

    public int[][] applyEdgeDetection(String method, int[][] source, String kernelSize, int strength, String grayscaleMethod) {
        switch (method) {
            case "Robert-Cross":
                RobertsCrossEdge robertsCross = new RobertsCrossEdge(source, strength);
                return robertsCross.getEdges();
            case "Sobel":
                SobelEdge sobel = new SobelEdge(source, grayscaleMethod);
                return sobel.getEdges();
            case "Laplacian":
                LaplacianEdge laplace = new LaplacianEdge(source, kernelSize);
                return laplace.getEdges();
            default:
                // IllegalArgumentException
                throw new IllegalArgumentException("Invalid edge detection method: " + method);
        }
    }

    /**
     * บันทึกรูปภาพที่ผ่านการตรวจจับขอบ
     * @param edgeDetectedImage รูปภาพที่ผ่านการตรวจจับขอบ
     */
    public void saveEdgeDetectedImage(File file, Image edgeDetectedImage) {
        if (file != null) {
            // ใช้ synchronized เพื่อให้มั่นใจว่าไม่มีการเขียนไฟล์พร้อมกันจากหลาย thread
            synchronized (EdgeDetectionHandler.class) {
                // แทนที่ภาพใน selectedFiles
                MainController.updateImage(file, edgeDetectedImage);
                System.out.println("Edge detected image updated for file: " + file.getName());
            }
        } else {
            System.out.println("File is null, unable to update.");
        }
    }



    public void detectAll(Map<File, Image> fileImageMap, String method, String kernelSize, int strength, String grayscaleMethod) {
        int totalImages = fileImageMap.size();
        final int[] processedImages = {0};

        // Get the number of available cores
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("Available cores: " + cores);

        // Create a thread pool with available cores
        ExecutorService executorService = Executors.newFixedThreadPool(cores);

        // Process each image using a thread
        for (File file : fileImageMap.keySet()) {
            //IllegalStateException
            try {
                if (executorService.isShutdown()) {
                    throw new IllegalStateException("Executor service has already been shut down. Cannot submit new tasks.");
                }

            executorService.submit(() -> {
                System.out.println("Thread " + Thread.currentThread().getName() + " is processing: " + file.getName());

                // Get the image
                Image image = fileImageMap.get(file);
                int[][] grayscaleArray = ImageConverter.convertToGrayscaleArray(image);
                int[][] edgeDetectedArray = applyEdgeDetection(method, grayscaleArray, kernelSize, strength, grayscaleMethod);
                Image edgeDetectedImage = ImageConverter.convertToImage(edgeDetectedArray);

                // Save the processed image based on the file
                synchronized (EdgeDetectionHandler.class) {
                    MainController.updateImage(file, edgeDetectedImage);
                    processedImages[0]++;
                    System.out.println("Thread " + Thread.currentThread().getName() + " completed: " + file.getName());
                    displayThreadPoolInfo(cores, processedImages[0], totalImages);
                }
            });
            } catch (IllegalStateException e) {
                System.err.println("Error: " + e.getMessage());
                // อาจจะทำการแจ้งเตือนหรือจัดการเมื่อมีข้อยกเว้นเกิดขึ้น เช่น การหยุดโปรแกรมหรือสร้าง ExecutorService ใหม่
            }
        }

        // Shutdown the executor service after all tasks are completed
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println("Task interrupted: " + e.getMessage());
        }
    }



    // Display thread pool and core usage information
    private void displayThreadPoolInfo(int cores, int processedImages, int totalImages) {
        System.out.println("Cores: " + cores);
        System.out.println("Processed images: " + processedImages + " / " + totalImages);
        System.out.println("Active threads: " + Thread.activeCount());
        System.out.println("====================================");
    }
}
