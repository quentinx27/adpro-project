package se233.teamnoonkhem.model.Edge;

public class RobertsCrossEdge {

    private final static double[][] X_kernel = {{1, 0}, {0, -1}};
    private final static double[][] Y_kernel = {{0, -1}, {1, 0}};
    private int[][] edges;
    private int threshold = 10; // ค่าปกติของ threshold
    private int strength; // เก็บค่า strength

    /**
     * Constructor รับภาพที่เป็น array 2D สำหรับการตรวจจับขอบ พร้อมทั้งกำหนดความเข้มของขอบ
     *
     * @param image ภาพในรูปแบบ array 2D ของพิกเซลแบบ grayscale
     * @param strength ความเข้มของขอบ (1-100)
     */
    public RobertsCrossEdge(int[][] image, int strength) {
        this.strength = Math.max(1, Math.min(strength, 100)); // จำกัดค่า strength ให้อยู่ระหว่าง 1 ถึง 100
        image = applyGaussianBlur(image); // ลด noise ก่อนการตรวจจับขอบ
        findEdges(image);
    }

    /**
     * ฟังก์ชัน Gaussian blur เพื่อลด noise
     */
    private int[][] applyGaussianBlur(int[][] image) {
        double[][] gaussianKernel = {
                {1, 2, 1},
                {2, 4, 2},
                {1, 2, 1}
        };
        return applyConvolution(image, gaussianKernel);
    }

    /**
     * ฟังก์ชันสำหรับทำ Convolution
     */
    private int[][] applyConvolution(int[][] image, double[][] kernel) {
        int rows = image.length;
        int cols = image[0].length;
        int[][] result = new int[rows][cols];

        int kernelSize = kernel.length;
        int offset = kernelSize / 2;

        for (int i = offset; i < rows - offset; i++) {
            for (int j = offset; j < cols - offset; j++) {
                double sum = 0.0;

                for (int ki = -offset; ki <= offset; ki++) {
                    for (int kj = -offset; kj <= offset; kj++) {
                        sum += image[i + ki][j + kj] * kernel[ki + offset][kj + offset];
                    }
                }
                result[i][j] = (int) (sum / 16); // ปรับค่าการแบ่งเพื่อลดความเข้มของเส้นขอบ
            }
        }
        return result;
    }

    /**
     * ฟังก์ชันตรวจจับขอบในภาพโดยใช้ Robert's Cross Operator
     *
     * @param image  ภาพในรูปแบบ array 2D ของพิกเซลแบบ grayscale
     */
    private void findEdges(int[][] image) {
        int rows = image.length;
        int cols = image[0].length;
        edges = new int[rows][cols];
        int maxGradient = Integer.MIN_VALUE;

        // คำนวณ gradient สำหรับแต่ละพิกเซลในภาพ
        for (int y = 0; y < rows - 1; y++) {
            for (int x = 0; x < cols - 1; x++) {
                int gx = (int) (image[y][x] * X_kernel[0][0] + image[y + 1][x + 1] * X_kernel[1][1]);
                int gy = (int) (image[y + 1][x] * Y_kernel[1][0] + image[y][x + 1] * Y_kernel[0][1]);

                // คำนวณขนาดของ gradient และปรับตามค่า strength
                int gradient = (int) (Math.sqrt(gx * gx + gy * gy) * (strength / 100.0));

                if (gradient > maxGradient) {
                    maxGradient = gradient;
                }

                // ตรวจสอบว่า gradient มากกว่า threshold หรือไม่
                edges[y][x] = gradient > threshold ? 0 : 255; // เส้นขอบเป็นสีดำ (0), พื้นหลังเป็นสีขาว (255)
            }
        }
    }

    /**
     * คืนค่าผลลัพธ์ของการตรวจจับขอบในรูปแบบของ array 2D
     *
     * @return array 2D ที่แสดงขอบภาพ
     */
    public int[][] getEdges() {
        return edges;
    }

    /**
     * ตั้งค่า strength สำหรับควบคุมความเข้มของขอบ
     *
     * @param strength ความเข้มของขอบ (1-100)
     */
    public void setStrength(int strength) {
        this.strength = Math.max(1, Math.min(strength, 100)); // จำกัดค่า strength ให้อยู่ระหว่าง 1 ถึง 100
    }

    /**
     * อัพเดทการตรวจจับขอบใหม่หลังจากตั้งค่า strength
     *
     * @param image ภาพในรูปแบบ array 2D ของพิกเซลแบบ grayscale
     */
    public void updateEdges(int[][] image) {
        image = applyGaussianBlur(image); // ลด noise ก่อนการตรวจจับขอบ
        findEdges(image); // ตรวจจับขอบใหม่
    }
}
