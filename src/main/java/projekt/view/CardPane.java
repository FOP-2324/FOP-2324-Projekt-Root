package projekt.view;

import org.tudalgo.algoutils.student.annotation.DoNotTouch;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * A StackPane that displays a card with an icon and a label.
 */
@DoNotTouch
public class CardPane extends StackPane {
    private static final int defaultCardWidth = 30;

    /**
     * Creates a new CardPane with the given color and no icon or label.
     *
     * @param cardColor The color of the card.
     */
    public CardPane(final Color cardColor) {
        this(cardColor, "", "");
    }

    /**
     * Creates a new CardPane with the given color and label and no icon.
     *
     * @param cardColor The color of the card.
     * @param labelText The text of the label.
     */
    public CardPane(final Color cardColor, final String labelText) {
        this(cardColor, "", labelText);
    }

    /**
     * Creates a new CardPane with the given color and icon and label.
     *
     * @param cardColor The color of the card.
     * @param labelText The text of the label.
     * @param iconPath  The path to the icon.
     */
    public CardPane(final Color cardColor, final String iconPath, final String labelText) {
        this(cardColor, new Image(iconPath), labelText);
    }

    public CardPane(final Color cardColor, final Image icon, final String labelText) {
        this(cardColor, new ImageView(icon), labelText, 0);
    }

    /**
     * Creates a card with the given color, icon and label. With the given width.
     *
     * @param cardColor The color of the card.
     * @param iconPath  The path to the icon.
     * @param labelText The text of the label.
     */
    public CardPane(final Color cardColor, final ImageView icon, final String labelText, double cardWidth) {
        super();
        this.setAlignment(Pos.CENTER);
        final ImageView cardImage = new ColoredImageView(Utils.emptyCardImage, cardColor);

        if (cardWidth <= 0) {
            cardWidth = defaultCardWidth;
        }

        cardImage.setFitWidth(cardWidth);
        cardImage.setPreserveRatio(true);
        this.getChildren().add(cardImage);

        if (icon != null) {
            icon.setFitWidth(cardWidth * 0.7);
            icon.setPreserveRatio(true);
            this.getChildren().add(icon);
        }

        if (labelText != null && !labelText.isBlank()) {
            final Label valueLabel = new Label(labelText);
            valueLabel.setBackground(
                    new Background(
                            new BackgroundFill(new Color(1, 1, 1, 0.9), new CornerRadii(2), new Insets(0, -2, 0, -2))));
            this.getChildren().add(valueLabel);
        }
    }
}
