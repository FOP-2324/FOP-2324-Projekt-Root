package projekt.view.gameControls;

import java.util.Map;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.util.converter.IntegerStringConverter;
import projekt.model.ResourceType;
import projekt.view.CardPane;
import projekt.view.ResourceCardPane;
import projekt.view.Utils;

public class DropCardsDialog extends Dialog<Map<ResourceType, Integer>> {
    public DropCardsDialog(Map<ResourceType, Integer> playerResources) {
        GridPane mainPane = new GridPane(10, 10);
        for (ResourceType resourceType : playerResources.keySet()) {
            CardPane resourceCard = new ResourceCardPane(resourceType,
                    Integer.toString(playerResources.get(resourceType)), 50);
            mainPane.add(resourceCard, resourceType.ordinal(), 0);

            TextField amountField = new TextField();
            amountField.setTextFormatter(
                    new TextFormatter<>(new IntegerStringConverter(), 0, Utils.positiveIntegerFilter));
            mainPane.add(amountField, resourceType.ordinal(), 1);
        }

        setTitle("Drop Cards");

        DialogPane dialogPane = getDialogPane();
        dialogPane.setContent(mainPane);
        dialogPane.getButtonTypes().add(ButtonType.OK);
    }
}
