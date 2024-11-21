package se233.teamnoonkhem.Controller;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class ImageConverter {

    // แปลง Image เป็นอาร์เรย์ int[][] ของสีเทา
    public static int[][] convertToGrayscaleArray(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int[][] grayscaleArray = new int[height][width];

        PixelReader pixelReader = image.getPixelReader();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                // คำนวณค่าสีเทาจากค่า RGB
                int gray = (int) (color.getRed() * 255 * 0.3 + color.getGreen() * 255 * 0.59 + color.getBlue() * 255 * 0.11);
                grayscaleArray[y][x] = gray;
            }
        }
        return grayscaleArray;
    }

    // แปลงอาร์เรย์ int[][] กลับเป็น Image
    public static Image convertToImage(int[][] grayscaleArray) {
        int height = grayscaleArray.length;
        int width = grayscaleArray[0].length;
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = grayscaleArray[y][x];
                // ตั้งค่าให้เป็นสีเทา โดยใช้ค่า RGB เท่ากันในทุกช่อง
                Color color = Color.rgb(gray, gray, gray);
                pixelWriter.setColor(x, y, color);
            }
        }
        return writableImage;
    }
}
