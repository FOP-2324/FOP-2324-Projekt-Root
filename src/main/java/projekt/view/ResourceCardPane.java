package projekt.view;

import org.tudalgo.algoutils.student.annotation.DoNotTouch;

import projekt.model.ResourceType;

@DoNotTouch
public class ResourceCardPane extends CardPane {
    public ResourceCardPane(final ResourceType resourceType, final int amount) {
        this(resourceType, Integer.toString(amount));
    }

    public ResourceCardPane(final ResourceType resourceType) {
        this(resourceType, null, 0.0);
    }

    public ResourceCardPane(final ResourceType resourceType, final String labelText) {
        this(resourceType, labelText, 0.0);
    }

    public ResourceCardPane(final ResourceType resourceType, final String labelText, final double cardWidth) {
        super(resourceType.color, new Sprite(Utils.resourcesSpriteSheet, resourceType.iconIndex, resourceType.color),
                labelText, cardWidth);
        Utils.attachTooltip(resourceType.toString(), this);
    }
}
