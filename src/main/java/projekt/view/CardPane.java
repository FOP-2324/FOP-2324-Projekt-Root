package projekt.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * A StackPane that displays a card with an icon and a label.
 */
public class CardPane extends StackPane {
    /**
     * Creates a new CardPane with the given color and no icon or label.
     *
     * @param cardColor The color of the card.
     */
    public CardPane(Color cardColor) {
        this(cardColor, null, null);
    }

    /**
     * Creates a new CardPane with the given color and label and no icon.
     *
     * @param cardColor The color of the card.
     * @param labelText The text of the label.
     */
    public CardPane(Color cardColor, String labelText) {
        this(cardColor, null, labelText);
    }

    /**
     * Creates a card with the given color, icon and label.
     *
     * @param cardColor The color of the card.
     * @param iconPath  The path to the icon.
     * @param labelText The text of the label.
     */
    public CardPane(Color cardColor, String iconPath, String labelText) {
        super();
        this.setAlignment(Pos.CENTER);
        ImageView cardImage = new ImageView("img/empty_card.png");

        Lighting lighting = new Lighting();
        lighting.setDiffuseConstant(1.0);
        lighting.setSpecularConstant(0.0);
        lighting.setSpecularExponent(0.0);
        lighting.setSurfaceScale(0.0);

        // idk why but these values produces accurate colors, azimuth seems to not
        // matter that much
        lighting.setLight(new Light.Distant(0.0, 90.0, cardColor));

        double cardWidth = 30;
        cardImage.setFitWidth(cardWidth);
        cardImage.setPreserveRatio(true);

        cardImage.setEffect(lighting);
        this.getChildren().add(cardImage);

        if (iconPath != null && !iconPath.isBlank()) {
            ImageView iconImage = new ImageView(iconPath);
            iconImage.setFitWidth(cardWidth * 0.9);
            iconImage.setPreserveRatio(true);
            this.getChildren().add(iconImage);
        }

        if (labelText != null && !labelText.isBlank()) {
            Label valueLabel = new Label(labelText);
            valueLabel.setBackground(
                    new Background(
                            new BackgroundFill(new Color(1, 1, 1, 0.9), new CornerRadii(2), new Insets(0, -2, 0, -2))));
            this.getChildren().add(valueLabel);
        }
    }
}
