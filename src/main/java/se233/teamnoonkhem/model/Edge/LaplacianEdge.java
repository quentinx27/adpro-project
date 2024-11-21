package se233.teamnoonkhem.model.Edge;

public class LaplacianEdge {

    private int[][] edges;
    private final int threshold = 0; // ตั้งค่า threshold เป็น 10
    private final double scale3x3 = 0.10; // สเกลสำหรับ kernel 3x3
    private final double scale5x5 = 0.025; // สเกลคงที่สำหรับ kernel 5x5

    // Convolution kernel สำหรับ Laplacian 3x3
    private final double[][] kernel3x3 = {
            {-1, -1, -1},
            {-1, 8, -1},
            {-1, -1, -1}
    };

    // Convolution kernel สำหรับ Laplacian 5x5
    private final double[][] kernel5x5 = {
            {0, 0, -1, 0, 0},
            {0, -1, -2, -1, 0},
            {-1, -2, 16, -2, -1},
            {0, -1, -2, -1, 0},
            {0, 0, -1, 0, 0}
    };

    // Gaussian kernel สำหรับลด noise (3x3)
    private final double[][] gaussianKernel3x3 = {
            {1, 2, 1},
            {2, 4, 2},
            {1, 2, 1}
    };

    // Gaussian kernel สำหรับลด noise (5x5)
    private final double[][] gaussianKernel5x5 = {
            {1, 4, 6, 4, 1},
            {4, 16, 24, 16, 4},
            {6, 24, 36, 24, 6},
            {4, 16, 24, 16, 4},
            {1, 4, 6, 4, 1}
    };

    // Constructor สำหรับใช้กับภาพที่เป็น array พร้อมกับการเลือกขนาด kernel
    public LaplacianEdge(int[][] image, String kernelSize) {
        image = applyGaussianBlur(image, kernelSize);  // ลด noise ก่อน
        findEdges(image, kernelSize);  // ตรวจจับขอบ
    }

    // ฟังก์ชันแปลงภาพเป็น grayscale (ในกรณีที่ภาพของคุณเป็น RGB)
    private int[][] convertToGrayscale(int[][] image) {
        return image;
    }

    // ฟังก์ชันคอนโวลูชันที่เลือกขนาด kernel
    private int[][] applyConvolution(int[][] image, double[][] kernel, double scale) {
        int rows = image.length;
        int cols = image[0].length;
        int[][] result = new int[rows][cols];

        int kernelSize = kernel.length;
        int offset = kernelSize / 2;

        for (int i = offset; i < rows - offset; i++) {
            for (int j = offset; j < cols - offset; j++) {
                double sum = 0.0;

                // คำนวณการคอนโวลูชัน
                for (int ki = -offset; ki <= offset; ki++) {
                    for (int kj = -offset; kj <= offset; kj++) {
                        sum += image[i + ki][j + kj] * kernel[ki + offset][kj + offset];
                    }
                }
                // เพิ่มการสเกลผลลัพธ์ด้วยค่า scale
                result[i][j] = Math.min(Math.max((int) (sum * scale), 0), 255); // จำกัดค่าระหว่าง 0 ถึง 255
            }
        }
        return result;
    }

    // ฟังก์ชันลด noise โดยใช้ Gaussian Blur และเลือกขนาด kernel
    private int[][] applyGaussianBlur(int[][] image, String kernelSize) {
        // เลือก Gaussian kernel ขนาดที่เหมาะสม
        double[][] kernel = kernelSize.equals("5*5") ? gaussianKernel5x5 : gaussianKernel3x3;
        double scale = kernelSize.equals("5*5") ? scale5x5 : scale3x3; // ใช้ scale ที่เหมาะสม
        return applyConvolution(image, kernel, scale);
    }

    // ฟังก์ชันหาขอบภาพด้วย Laplacian โดยเลือกขนาด kernel
    private void findEdges(int[][] image, String kernelSize) {
        image = convertToGrayscale(image); // แปลงเป็น Grayscale หากจำเป็น

        // เลือก kernel ขนาดที่เหมาะสม
        double[][] kernel = kernelSize.equals("5*5") ? kernel5x5 : kernel3x3;
        double scale = kernelSize.equals("5*5") ? scale5x5 : scale3x3; // เลือกค่า scale ตามขนาด kernel

        // ใช้การคอนโวลูชันกับ Laplacian kernel
        int[][] convolvedImage = applyConvolution(image, kernel, scale);
        int rows = convolvedImage.length;
        int cols = convolvedImage[0].length;

        // ใช้ threshold คงที่ 10 ในการตรวจจับขอบ
        edges = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // เปลี่ยนการตรวจจับ: ขอบเป็นสีดำ (0), พื้นหลังเป็นสีขาว (255)
                edges[i][j] = Math.abs(convolvedImage[i][j]) > threshold ? 0 : 255;
            }
        }
    }

    // Getters สำหรับ edges และ threshold
    public int[][] getEdges() {
        return edges;
    }

    public int getThreshold() {
        return threshold;
    }

    // Main สำหรับทดสอบการทำงาน
    public static void main(String[] args) {
        int[][] image = {
                {0, 0, 0, 0, 0},
                {0, 255, 255, 255, 0},
                {0, 255, 255, 255, 0},
                {0, 255, 255, 255, 0},
                {0, 0, 0, 0, 0}
        };

        LaplacianEdge laplace3x3 = new LaplacianEdge(image, "3*3"); // ใช้ kernel 3x3
        LaplacianEdge laplace5x5 = new LaplacianEdge(image, "5*5"); // ใช้ kernel 5x5

        System.out.println("Results from kernel 3*3:");
        int[][] edges3x3 = laplace3x3.getEdges();
        for (int i = 0; i < edges3x3.length; i++) {
            for (int j = 0; j < edges3x3[i].length; j++) {
                System.out.print(edges3x3[i][j] + " ");
            }
            System.out.println();
        }

        System.out.println("Results from kernel 5*5:");
        int[][] edges5x5 = laplace5x5.getEdges();
        for (int i = 0; i < edges5x5.length; i++) {
            for (int j = 0; j < edges5x5[i].length; j++) {
                System.out.print(edges5x5[i][j] + " ");
            }
            System.out.println();
        }
    }
}
