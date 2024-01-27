package projekt.view;

import org.tudalgo.algoutils.student.annotation.DoNotTouch;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

/**
 * A Sprite is used to display a single image from a sprite sheet and can be
 * dynamically changed.
 * The sprite sheet must be a rectangle a display all images in a single column.
 */
@DoNotTouch
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
