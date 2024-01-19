package projekt.view;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class Sprite extends ImageView {
    private final IntegerProperty imageIndex;
    private final String spriteFilePath;
    private final Color color;

    public Sprite(String spriteFilePath, int startingIndex) {
        this(spriteFilePath, startingIndex, null);
    }

    public Sprite(String spriteFilePath, int startingIndex, Color color) {
        this.spriteFilePath = spriteFilePath;
        this.imageIndex = new SimpleIntegerProperty(startingIndex);
        this.color = color;
        initialize();
        colorize();
    }

    private void initialize() {
        Image image = new Image(spriteFilePath);
        setImage(image);
        double cellSize = image.getWidth();
        setPreserveRatio(true);
        setViewport(new Rectangle2D(0, 0, cellSize, cellSize));
        imageIndex.addListener(
                observable -> setViewport(new Rectangle2D(0, cellSize * imageIndex.get(), cellSize, cellSize)));
    }

    private void colorize() {
        if (color == null) {
            return;
        }

        Lighting lighting = new Lighting();
        lighting.setDiffuseConstant(1.0);
        lighting.setSpecularConstant(0.0);
        lighting.setSpecularExponent(0.0);
        lighting.setSurfaceScale(0.0);

        // idk why but these values produces accurate colors, azimuth seems to not
        // matter that much
        lighting.setLight(new Light.Distant(0.0, 90.0, color));

        this.setEffect(lighting);
    }

    public IntegerProperty imageIndexProperty() {
        return imageIndex;
    }
}
