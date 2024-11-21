package se233.teamnoonkhem.Controller;

import javafx.scene.image.ImageView;
import javafx.scene.transform.Scale;

public class ZoomHandler {

    private static final double ZOOM_FACTOR = 1.1;
    private static final double MIN_SCALE = 0.5;
    private static final double MAX_SCALE = 5.0;
    private double currentScale = 1.0;

    public void zoomIn(ImageView imageView) {
        if(currentScale < MAX_SCALE) {
            currentScale *= ZOOM_FACTOR;
            applyScale(imageView);
        }
    }
    public void zoomOut(ImageView imageView) {
        if(currentScale > MIN_SCALE) {
            currentScale /= ZOOM_FACTOR;
            applyScale(imageView);
        }
    }
    public void resetZoom(ImageView imageView) {
        currentScale = 1.0;
        applyScale(imageView);
    }
    private void applyScale(ImageView imageView) {
        imageView.getTransforms().clear();

        // Calculate the center of the imagePane instead of the imageView
        double pivotX = imageView.getBoundsInParent().getWidth() / 2;
        double pivotY = imageView.getBoundsInParent().getHeight() / 2;

        // Apply scaling with pivot at the center of imagePane
        Scale scale = new Scale(currentScale, currentScale, pivotX, pivotY);
        imageView.getTransforms().add(scale);
    }
}
