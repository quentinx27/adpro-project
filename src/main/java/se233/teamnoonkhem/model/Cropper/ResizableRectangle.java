package se233.teamnoonkhem.model.Cropper;

import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class ResizableRectangle extends Rectangle {

    private static final double RESIZER_SQUARE_SIDE = 8;
    private Paint resizerSquareColor = Color.WHITE;
    private Paint rectangleStrokeColor = Color.RED;

    private double mouseClickPosX;
    private double mouseClickPosY;

    // List to store all resize handles
    private final List<Rectangle> resizeHandles = new ArrayList<>();
    private Pane parentPane; // Holds reference to the parent pane
    private Runnable updateDarkAreaCallback; // Callback function for updating Dark Area

    public ResizableRectangle(double x, double y, double width, double height, Pane pane, Runnable updateDarkAreaCallback) {
        super(x, y, width, height);
        this.parentPane = pane;
        this.updateDarkAreaCallback = updateDarkAreaCallback;
        pane.getChildren().add(this);
        super.setStroke(rectangleStrokeColor);
        super.setStrokeWidth(1);
        super.setFill(Color.color(1, 1, 1, 0));

        // Create resize handles
        createResizerSquares(pane);

        // Enable dragging of the rectangle
        this.setOnMousePressed(event -> {
            mouseClickPosX = event.getX();
            mouseClickPosY = event.getY();
            getParent().setCursor(Cursor.MOVE);
        });

        this.setOnMouseDragged(event -> {
            double offsetX = event.getX() - mouseClickPosX;
            double offsetY = event.getY() - mouseClickPosY;

            double newX = getX() + offsetX;
            double newY = getY() + offsetY;

            // Ensure it stays within parent bounds
            if (newX >= 0 && newX + getWidth() <= parentPane.getWidth()) {
                setX(newX);
            }
            if (newY >= 0 && newY + getHeight() <= parentPane.getHeight()) {
                setY(newY);
            }

            mouseClickPosX = event.getX();
            mouseClickPosY = event.getY();

            // Update Dark Area
            updateDarkAreaCallback.run();
        });

        this.setOnMouseReleased(event -> getParent().setCursor(Cursor.DEFAULT));
    }

    // Function to remove all resize handles
    public void removeResizeHandles(Pane pane) {
        for (Rectangle handle : resizeHandles) {
            pane.getChildren().remove(handle);
        }
        resizeHandles.clear(); // Clear the list after removing
    }

    private void createResizerSquares(Pane pane) {
        // Create each resizer handle for each side/corner
        makeNWResizerSquare(pane);
        makeWResizerSquare(pane);
        makeSWResizerSquare(pane);
        makeSResizerSquare(pane);
        makeSEResizerSquare(pane);
        makeEResizerSquare(pane);
        makeNEResizerSquare(pane);
        makeNResizerSquare(pane);
    }

    private void makeNWResizerSquare(Pane pane) {
        Rectangle squareNW = createResizerRectangle(pane);
        squareNW.xProperty().bind(super.xProperty().subtract(squareNW.widthProperty().divide(2.0)));
        squareNW.yProperty().bind(super.yProperty().subtract(squareNW.heightProperty().divide(2.0)));

        squareNW.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> resizeNW(event));
        squareNW.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareNW.getParent().setCursor(Cursor.NW_RESIZE));
    }

    private void makeWResizerSquare(Pane pane) {
        Rectangle squareW = createResizerRectangle(pane);
        squareW.xProperty().bind(super.xProperty().subtract(squareW.widthProperty().divide(2.0)));
        squareW.yProperty().bind(super.yProperty().add(super.heightProperty().divide(2.0).subtract(squareW.heightProperty().divide(2.0))));

        squareW.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> resizeW(event));
        squareW.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareW.getParent().setCursor(Cursor.W_RESIZE));
    }

    private void makeSWResizerSquare(Pane pane) {
        Rectangle squareSW = createResizerRectangle(pane);
        squareSW.xProperty().bind(super.xProperty().subtract(squareSW.widthProperty().divide(2.0)));
        squareSW.yProperty().bind(super.yProperty().add(super.heightProperty().subtract(squareSW.heightProperty().divide(2.0))));

        squareSW.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> resizeSW(event));
        squareSW.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareSW.getParent().setCursor(Cursor.SW_RESIZE));
    }

    private void makeSResizerSquare(Pane pane) {
        Rectangle squareS = createResizerRectangle(pane);
        squareS.xProperty().bind(super.xProperty().add(super.widthProperty().divide(2.0).subtract(squareS.widthProperty().divide(2.0))));
        squareS.yProperty().bind(super.yProperty().add(super.heightProperty().subtract(squareS.heightProperty().divide(2.0))));

        squareS.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> resizeS(event));
        squareS.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareS.getParent().setCursor(Cursor.S_RESIZE));
    }

    private void makeSEResizerSquare(Pane pane) {
        Rectangle squareSE = createResizerRectangle(pane);
        squareSE.xProperty().bind(super.xProperty().add(super.widthProperty()).subtract(squareSE.widthProperty().divide(2.0)));
        squareSE.yProperty().bind(super.yProperty().add(super.heightProperty().subtract(squareSE.heightProperty().divide(2.0))));

        squareSE.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> resizeSE(event));
        squareSE.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareSE.getParent().setCursor(Cursor.SE_RESIZE));
    }

    private void makeEResizerSquare(Pane pane) {
        Rectangle squareE = createResizerRectangle(pane);
        squareE.xProperty().bind(super.xProperty().add(super.widthProperty()).subtract(squareE.widthProperty().divide(2.0)));
        squareE.yProperty().bind(super.yProperty().add(super.heightProperty().divide(2.0).subtract(squareE.heightProperty().divide(2.0))));

        squareE.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> resizeE(event));
        squareE.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareE.getParent().setCursor(Cursor.E_RESIZE));
    }

    private void makeNEResizerSquare(Pane pane) {
        Rectangle squareNE = createResizerRectangle(pane);
        squareNE.xProperty().bind(super.xProperty().add(super.widthProperty()).subtract(squareNE.widthProperty().divide(2.0)));
        squareNE.yProperty().bind(super.yProperty().subtract(squareNE.heightProperty().divide(2.0)));

        squareNE.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> resizeNE(event));
        squareNE.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareNE.getParent().setCursor(Cursor.NE_RESIZE));
    }

    private void makeNResizerSquare(Pane pane) {
        Rectangle squareN = createResizerRectangle(pane);
        squareN.xProperty().bind(super.xProperty().add(super.widthProperty().divide(2.0).subtract(squareN.widthProperty().divide(2.0))));
        squareN.yProperty().bind(super.yProperty().subtract(squareN.heightProperty().divide(2.0)));

        squareN.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> resizeN(event));
        squareN.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareN.getParent().setCursor(Cursor.N_RESIZE));
    }

    private Rectangle createResizerRectangle(Pane pane) {
        Rectangle square = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);
        square.setFill(resizerSquareColor);
        pane.getChildren().add(square);
        resizeHandles.add(square);  // Add to the resizeHandles list
        return square;
    }

    // Resizing logic for different sides
    private void resizeNW(MouseEvent event) {
        double offsetX = event.getX() - getX();
        double offsetY = event.getY() - getY();

        if (getWidth() - offsetX > 0) {
            setX(event.getX());
            setWidth(getWidth() - offsetX);
        }

        if (getHeight() - offsetY > 0) {
            setY(event.getY());
            setHeight(getHeight() - offsetY);
        }

        updateDarkAreaCallback.run();
    }

    private void resizeW(MouseEvent event) {
        double offsetX = event.getX() - getX();
        if (getWidth() - offsetX > 0) {
            setX(event.getX());
            setWidth(getWidth() - offsetX);
        }

        updateDarkAreaCallback.run();
    }

    private void resizeSW(MouseEvent event) {
        double offsetX = event.getX() - getX();
        double offsetY = event.getY() - (getY() + getHeight());

        if (getWidth() - offsetX > 0) {
            setX(event.getX());
            setWidth(getWidth() - offsetX);
        }

        if (offsetY >= 0 && offsetY <= parentPane.getHeight()) {
            setHeight(offsetY);
        }

        updateDarkAreaCallback.run();
    }

    private void resizeS(MouseEvent event) {
        double offsetY = event.getY() - getY();
        if (offsetY > 0) {
            setHeight(offsetY);
        }

        updateDarkAreaCallback.run();
    }

    private void resizeSE(MouseEvent event) {
        double offsetX = event.getX() - getX();
        double offsetY = event.getY() - getY();

        if (offsetX >= 0 && offsetX <= parentPane.getWidth()) {
            setWidth(offsetX);
        }

        if (offsetY >= 0 && offsetY <= parentPane.getHeight()) {
            setHeight(offsetY);
        }

        updateDarkAreaCallback.run();
    }

    private void resizeE(MouseEvent event) {
        double offsetX = event.getX() - getX();
        if (offsetX > 0 && offsetX <= parentPane.getWidth()) {
            setWidth(offsetX);
        }

        updateDarkAreaCallback.run();
    }

    private void resizeNE(MouseEvent event) {
        double offsetX = event.getX() - getX();
        double offsetY = event.getY() - getY();
        double newY = getY() + offsetY;

        if (offsetX >= 0 && offsetX <= parentPane.getWidth()) {
            setWidth(offsetX);
        }

        if (newY >= 0 && newY <= getY() + getHeight()) {
            setY(newY);
            setHeight(getHeight() - offsetY);
        }

        updateDarkAreaCallback.run();
    }

    private void resizeN(MouseEvent event) {
        double offsetY = event.getY() - getY();
        double newY = getY() + offsetY;

        if (newY >= 0 && newY <= getY() + getHeight()) {
            setY(newY);
            setHeight(getHeight() - offsetY);
        }

        updateDarkAreaCallback.run();
    }
}
