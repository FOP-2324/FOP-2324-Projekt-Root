package projekt.view.gameControls;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.util.converter.IntegerStringConverter;
import projekt.model.Player;
import projekt.model.ResourceType;
import projekt.view.CardPane;
import projekt.view.ResourceCardPane;
import projekt.view.Utils;

public class DropCardsDialog extends Dialog<Map<ResourceType, Integer>> {
    private final Map<ResourceType, Integer> droppedCards = new HashMap<>();

    public DropCardsDialog(final Map<ResourceType, Integer> playerResources, final int amountToDrop,
            final Player player) {
        final GridPane mainPane = new GridPane(10, 10);
        final DialogPane dialogPane = this.getDialogPane();
        dialogPane.setContent(mainPane);
        dialogPane.getButtonTypes().add(ButtonType.OK);
        dialogPane.lookupButton(ButtonType.OK).setDisable(true);
        this.setTitle(String.format("Drop %d cards", amountToDrop));
        this.setHeaderText(constructTooFewCardsString(amountToDrop, player));

        for (final ResourceType resourceType : playerResources.keySet()) {
            final CardPane resourceCard = new ResourceCardPane(resourceType,
                    Integer.toString(playerResources.get(resourceType)),
                    50);
            mainPane.add(resourceCard, resourceType.ordinal(), 0);

            final TextField amountField = new TextField();
            amountField.setTextFormatter(
                    new TextFormatter<>(new IntegerStringConverter(), 0, Utils.positiveIntegerFilter));
            amountField.textProperty().subscribe((oldText, newText) -> {

                if (newText.isEmpty()) {
                    this.droppedCards.remove(resourceType);
                } else {
                    final int enteredAmount = Integer.parseInt(newText);
                    if (enteredAmount > playerResources.get(resourceType)) {
                        amountField.setText(oldText);
                        return;
                    }
                    this.droppedCards.put(resourceType, enteredAmount);
                }

                final int currentTotalAmount = this.droppedCards.values().stream().mapToInt(Integer::intValue).sum();
                dialogPane.lookupButton(ButtonType.OK).setDisable(true);
                if (currentTotalAmount > amountToDrop) {
                    this.setHeaderText(
                            String.format("You (%s) can only drop %d cards", player.getName(), amountToDrop));
                } else if (currentTotalAmount < amountToDrop) {
                    this.setHeaderText(
                            constructTooFewCardsString(amountToDrop - currentTotalAmount, player));
                } else {
                    this.setHeaderText("");
                    dialogPane.lookupButton(ButtonType.OK).setDisable(false);
                }
            });

            mainPane.add(amountField, resourceType.ordinal(), 1);
        }

        this.setResultConverter(buttonType -> {
            if (ButtonType.OK.equals(buttonType)) {
                return this.droppedCards;
            }
            return null;
        });
    }

    private String constructTooFewCardsString(final int amount, final Player player) {
        return String.format("You (%s) still need to drop %d cards", player.getName(), amount);
    }
}
