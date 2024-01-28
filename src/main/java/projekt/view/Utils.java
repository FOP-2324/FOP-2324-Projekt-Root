package projekt.view;

import java.util.function.UnaryOperator;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public final class Utils {
    /**
     * A filter that only allows positive integers.
     * Can be used with {@link TextField#setTextFormatter(TextFormatter)} to make a
     * text field only accept positive integers.
     */
    public static final UnaryOperator<Change> positiveIntegerFilter = change -> {
        final String newText = change.getControlNewText();
        if (newText.matches("([0-9]*)?")) {
            return change;
        }
        return null;
    };

    /**
     * Attaches a tooltip to a node.
     *
     * @param text   The text to display in the tooltip.
     * @param target The node to attach the tooltip to.
     */
    public static final void attachTooltip(String text, Node target) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.millis(100));
        Tooltip.install(target, tooltip);
    }
}
