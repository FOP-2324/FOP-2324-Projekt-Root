package projekt.view;

import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class ColoredImageView extends ImageView {

    public ColoredImageView(String imagePath, Color color) {
        super(imagePath);
        colorize(color);
    }

    public ColoredImageView(Image image, Color color) {
        super(image);
        colorize(color);
    }

    private void colorize(Color color) {
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
}
