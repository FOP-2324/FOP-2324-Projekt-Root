package projekt.view.gameControls;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.util.converter.IntegerStringConverter;
import projekt.model.ResourceType;
import projekt.view.CardPane;
import projekt.view.ResourceCardPane;
import projekt.view.Utils;

public class DropCardsDialog extends Dialog<Map<ResourceType, Integer>> {
    private final Map<ResourceType, Integer> droppedCards = new HashMap<>();
    private final String tooFewCardsText = "You still need to drop %d cards";

    public DropCardsDialog(Map<ResourceType, Integer> playerResources, int amountToDrop) {
        GridPane mainPane = new GridPane(10, 10);
        DialogPane dialogPane = getDialogPane();
        dialogPane.setContent(mainPane);
        dialogPane.getButtonTypes().add(ButtonType.OK);
        dialogPane.lookupButton(ButtonType.OK).setDisable(true);
        setTitle(String.format("Drop %d cards", amountToDrop));
        Label errorLabel = new Label(String.format(tooFewCardsText, amountToDrop));

        for (ResourceType resourceType : playerResources.keySet()) {
            CardPane resourceCard = new ResourceCardPane(resourceType, Integer.toString(playerResources.get(resourceType)), 50);
            mainPane.add(resourceCard, resourceType.ordinal(), 0);

            TextField amountField = new TextField();
            amountField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, Utils.positiveIntegerFilter));
            amountField.textProperty().subscribe((oldText, newText) -> {

                if (newText.isEmpty()) {
                    droppedCards.remove(resourceType);
                } else {
                    int enteredAmount = Integer.parseInt(newText);
                    if (enteredAmount > playerResources.get(resourceType)) {
                        amountField.setText(oldText);
                        return;
                    }
                    droppedCards.put(resourceType, enteredAmount);
                }

                int currentTotalAmount = droppedCards.values().stream().mapToInt(Integer::intValue).sum();
                dialogPane.lookupButton(ButtonType.OK).setDisable(true);
                if (currentTotalAmount > amountToDrop) {
                    errorLabel.setText(String.format("You can only drop %d cards", amountToDrop));
                } else if (currentTotalAmount < amountToDrop) {
                    errorLabel.setText(String.format(tooFewCardsText, amountToDrop - currentTotalAmount));
                } else {
                    errorLabel.setText("");
                    dialogPane.lookupButton(ButtonType.OK).setDisable(false);
                }
            });

            mainPane.add(amountField, resourceType.ordinal(), 1);
        }

        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return droppedCards;
            }
            return null;
        });
        mainPane.add(errorLabel, 0, 2, mainPane.getColumnCount(), 1);
    }
}
