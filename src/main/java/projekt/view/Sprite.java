package projekt.view;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

public class Sprite extends ColoredImageView {
    private final IntegerProperty imageIndex;

    public Sprite(String spriteFilePath, int startingIndex) {
        this(spriteFilePath, startingIndex, null);
    }

    public Sprite(String spriteFilePath, int startingIndex, Color color) {
        super(spriteFilePath, color);
        this.imageIndex = new SimpleIntegerProperty(startingIndex);
        initialize();
    }

    private void initialize() {
        double cellSize = getImage().getWidth();
        setPreserveRatio(true);
        setViewport(cellSize);
        imageIndex.addListener(
                observable -> setViewport(cellSize));
    }

    private void setViewport(double cellSize) {
        setViewport(new Rectangle2D(0, cellSize * imageIndex.get(), cellSize, cellSize));
    }

    public IntegerProperty imageIndexProperty() {
        return imageIndex;
    }
}
