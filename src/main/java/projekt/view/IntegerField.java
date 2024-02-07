package projekt.view;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;

public class IntegerField extends TextField {
    private final IntegerProperty valueProperty = new SimpleIntegerProperty(0);

    public IntegerField() {
        this(0);
    }

    public IntegerField(final int initialValue) {
        super(Integer.toString(initialValue));
        this.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, Utils.positiveIntegerFilter));
        textProperty().subscribe((oldText, newText) -> {
            if (newText.isEmpty()) {
                return;
            }
            try {
                valueProperty.set(Integer.parseInt(newText));
            } catch (final NumberFormatException e) {
                setText(oldText);
            }
        });
    }

    public ReadOnlyIntegerProperty valueProperty() {
        return valueProperty;
    }

    public void setValue(final int value) {
        setText(Integer.toString(value));
    }

    public void setValue(final Number value) {
        setValue(value.intValue());
    }
}
