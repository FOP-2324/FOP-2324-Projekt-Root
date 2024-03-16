package projekt.view;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;

/**
 * A fixed popup is a popup that is fixed relative to the it is attached to.
 * It is shown when the attached node is hovered.
 */
public class FixedPopup extends Popup {
    private double yOffset = 0;
    private double xOffset = 0;
    private final Pane contentPane = new HBox();

    /**
     * Creates a new fixed popup.
     */
    public FixedPopup() {
        super();
        contentPane.getStyleClass().add("fixed-popup");
        getContent().add(contentPane);
    }

    /**
     * Sets the content of the popup.
     *
     * @param content the content
     */
    public void setContent(final Node content) {
        contentPane.getChildren().setAll(content);
    }

    /**
     * Sets the y offset of the popup.
     *
     * @param yOffset
     */
    public void setYOffset(final double yOffset) {
        this.yOffset = yOffset;
    }

    /**
     * Sets the x offset of the popup.
     *
     * @param xOffset
     */
    public void setxOffset(final double xOffset) {
        this.xOffset = xOffset;
    }

    /**
     * Installs the popup on the given node.
     *
     * @param node the node
     */
    public void install(final Node node) {
        node.hoverProperty().subscribe(hover -> {
            if (hover) {
                show(node);
            } else {
                hide();
            }
        });
    }

    /**
     * Shows the popup relative to the given node.
     *
     * @param attachedNode the node
     */
    private void show(final Node attachedNode) {
        /* Set the new position before showing. */
        final Point2D anchor = anchor(attachedNode);

        super.show(attachedNode, anchor.getX(), anchor.getY());
    }

    /**
     * Calculates the anchor point for the tooltip.
     * Places the popup at the center top of the node.
     *
     * @param n the node
     * @return the anchor position
     */
    private Point2D anchor(final Node n) {
        /* Get the node bounds on the screen. */
        final Bounds bounds = n.localToScreen(n.getBoundsInLocal());

        /* Calculate the opening position based on node bounds . */
        final Point2D openPosition = new Point2D(bounds.getMaxX() - bounds.getWidth() / 2 - bounds.getWidth() + xOffset,
                bounds.getMaxY() - bounds.getHeight() / 2 + yOffset);

        return openPosition;
    }
}
