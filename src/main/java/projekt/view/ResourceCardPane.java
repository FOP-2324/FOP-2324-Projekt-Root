package projekt.view;

import projekt.model.ResourceType;

public class ResourceCardPane extends CardPane {
    public ResourceCardPane(ResourceType resourceType, int amount) {
        this(resourceType, Integer.toString(amount));
    }

    public ResourceCardPane(ResourceType resourceType) {
        this(resourceType, null, 0.0);
    }

    public ResourceCardPane(ResourceType resourceType, String labelText) {
        this(resourceType, labelText, 0.0);
    }

    public ResourceCardPane(ResourceType resourceType, String labelText, double cardWidth) {
        super(resourceType.color, null, labelText, cardWidth);
        Utils.attachTooltip(resourceType.toString(), this);
    }
}
