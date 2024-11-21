package se233.teamnoonkhem.model.Edge;

public class SobelEdge {
    private final static double[][] X_KERNEL = {
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}
    };

    private final static double[][] Y_KERNEL = {
            {1, 2, 1},
            {0, 0, 0},
            {-1, -2, -1}
    };

    private int[][] edges;
    private int threshold;

    public SobelEdge(int[][] image, String grayscaleMethod) {
        findEdges(image, false, grayscaleMethod);
    }

    private int[][] convertToGrayscale(int[][] image, String grayscaleMethod) {
        int rows = image.length;
        int cols = image[0].length;
        int[][] grayscaleImage = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int gray;
                switch (grayscaleMethod) {
                    case "Average":
                        gray = image[i][j];
                        break;
                    case "Luminosity":
                        gray = (int) (0.1 * image[i][j] + 0.85 * image[i][j] + 0.05 * image[i][j]);
                        // ความเข้มที่ 180 จะใช้ค่า Alpha เป็น 180
                        grayscaleImage[i][j] = isEdge(image, i, j,75) ? 0xB4ADD8E6 : gray; // ฟ้าอ่อนใน RGBA (Alpha = 180
                        break;
                    case "Custom":
                        // เปลี่ยนเส้นขอบเป็นสีแดง พร้อมความเข้ม Alpha = 125
                        gray = (int) (0.5 * image[i][j] + 0.3 * image[i][j] + 0.2 * image[i][j]);
                        // ความเข้มที่ 125 จะใช้ค่า Alpha เป็น 125
                        grayscaleImage[i][j] = isEdge(image, i, j,100) ? 0x7DFF0000 : gray; // สีแดงใน RGBA (Alpha = 125)
                        break;
                    default:
                        gray = image[i][j];
                        break;
                }
                grayscaleImage[i][j] = gray;
            }
        }
        return grayscaleImage;
    }

    // ฟังก์ชันตรวจสอบว่าเป็นเส้นขอบหรือไม่ (ทำงานร่วมกับ edge detection)
    private boolean isEdge(int[][] image, int x, int y, int threshold) {
        int rows = image.length;
        int cols = image[0].length;

        // ตรวจสอบขอบของภาพ หากค่าพิกเซลต่างจากรอบข้างมากกว่าค่า threshold
        if (x > 0 && x < rows - 1 && y > 0 && y < cols - 1) {
            int currentPixel = image[x][y];
            int topPixel = image[x - 1][y];
            int bottomPixel = image[x + 1][y];
            int leftPixel = image[x][y - 1];
            int rightPixel = image[x][y + 1];

            // ตรวจสอบว่าค่าพิกเซลต่างจากรอบข้างมากกว่าค่า threshold หรือไม่
            if (Math.abs(currentPixel - topPixel) > threshold || Math.abs(currentPixel - bottomPixel) > threshold ||
                    Math.abs(currentPixel - leftPixel) > threshold || Math.abs(currentPixel - rightPixel) > threshold) {
                return true;  // ถือว่าเป็นเส้นขอบ
            }
        }
        return false;  // ไม่ใช่เส้นขอบ
    }

    private void findEdges(int[][] image, boolean L1norm, String grayscaleMethod) {
        image = convertToGrayscale(image, grayscaleMethod);
        int rows = image.length;
        int cols = image[0].length;
        edges = new int[rows][cols];

        for (int i = 1; i < rows - 1; i++) {
            for (int j = 1; j < cols - 1; j++) {
                int gx = 0;
                int gy = 0;

                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        gx += image[i + x][j + y] * X_KERNEL[1 + x][1 + y];
                        gy += image[i + x][j + y] * Y_KERNEL[1 + x][1 + y];
                    }
                }

                edges[i][j] = (int) Math.sqrt(gx * gx + gy * gy);
            }
        }

        threshold = calcThreshold(edges);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                edges[i][j] = edges[i][j] > threshold ? 0 : 255;
            }
        }
    }

    private int calcThreshold(int[][] edges) {
        int total = 0;
        int count = 0;
        for (int i = 0; i < edges.length; i++) {
            for (int j = 0; j < edges[i].length; j++) {
                total += edges[i][j];
                count++;
            }
        }
        return total / count;
    }

    public int[][] getEdges() {
        return edges;
    }

    public int getThreshold() {
        return threshold;
    }
}
