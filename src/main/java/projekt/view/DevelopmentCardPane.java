package projekt.view;

import javafx.scene.paint.Color;
import projekt.model.DevelopmentCardType;

public class DevelopmentCardPane extends CardPane {
    public DevelopmentCardPane(final DevelopmentCardType type, final int amount) {
        this(type, Integer.toString(amount), 0.0);
    }

    public DevelopmentCardPane(final DevelopmentCardType type, final String labelText, final double cardWidth) {
        super(Color.BLUEVIOLET, new Sprite(Utils.developmentCardsSpriteSheet, type.iconIndex, Color.BLACK), labelText,
                cardWidth);
        Utils.attachTooltip(type.toString(), this);
    }
}
