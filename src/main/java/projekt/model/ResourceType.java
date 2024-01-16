package projekt.model;

import javafx.scene.paint.Color;

public enum ResourceType {
    WOOD(Color.DARKGREEN),
    CLAY(Color.SIENNA),
    WOOL(Color.LINEN),
    GRAIN(Color.YELLOW),
    ORE(Color.SLATEGRAY);

    public final Color color;

    ResourceType(Color color) {
        this.color = color;
    }
}
