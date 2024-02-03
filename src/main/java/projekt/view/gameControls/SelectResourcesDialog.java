package projekt.view.gameControls;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

public class SelectResourcesDialog extends Dialog<Map<ResourceType, Integer>> {

    private final Map<ResourceType, Integer> selectedResources = new HashMap<>();

    public SelectResourcesDialog(final int amountToSelect, final Player player,
            Map<ResourceType, Integer> resourcesToSelectFrom, final boolean dropCards) {
        System.out.println("SelectResourceDialog");
        if (resourcesToSelectFrom == null || resourcesToSelectFrom.isEmpty()) {
            resourcesToSelectFrom = Arrays.stream(ResourceType.values()).collect(Collectors.toMap(r -> r, r -> -1));
        }
        final Map<ResourceType, Integer> resourcesToSelectFromFinal = resourcesToSelectFrom;
        final String action = dropCards ? "drop" : "select";

        this.setTitle(String.format("%s %d cards", action.substring(0, 1).toUpperCase() + action.substring(1),
                amountToSelect));
        this.setHeaderText(constructTooFewCardsString(amountToSelect, player, action));
        final GridPane mainPane = new GridPane(10, 10);
        mainPane.getStylesheets().add("css/hexmap.css");

        final DialogPane dialogPane = this.getDialogPane();
        dialogPane.setContent(mainPane);
        dialogPane.getButtonTypes().add(ButtonType.OK);

        for (final ResourceType resourceType : resourcesToSelectFrom.keySet()) {
            final CardPane resourceCard = new ResourceCardPane(resourceType,
                    Integer.toString(resourcesToSelectFrom.get(resourceType)),
                    50);
            mainPane.add(resourceCard, resourceType.ordinal(), 0);

            final TextField amountField = new TextField();
            amountField.setTextFormatter(
                    new TextFormatter<>(new IntegerStringConverter(), 0, Utils.positiveIntegerFilter));
            amountField.textProperty().subscribe((oldText, newText) -> {

                if (newText.isEmpty()) {
                    this.selectedResources.remove(resourceType);
                } else {
                    final int enteredAmount = Integer.parseInt(newText);
                    if (enteredAmount > amountToSelect
                            || dropCards && enteredAmount > resourcesToSelectFromFinal.get(resourceType)) {
                        amountField.setText(oldText);
                        return;
                    }
                    this.selectedResources.put(resourceType, enteredAmount);
                }

                final int currentTotalAmount = this.selectedResources.values().stream().mapToInt(Integer::intValue)
                        .sum();
                dialogPane.lookupButton(ButtonType.OK).setDisable(true);
                if (currentTotalAmount > amountToSelect) {
                    this.setHeaderText(
                            String.format("You (%s) can only %s %d cards", player.getName(), action, amountToSelect));
                } else if (currentTotalAmount < amountToSelect) {
                    this.setHeaderText(
                            constructTooFewCardsString(amountToSelect - currentTotalAmount, player, action));
                } else {
                    this.setHeaderText("");
                    dialogPane.lookupButton(ButtonType.OK).setDisable(false);
                }
            });

            mainPane.add(amountField, resourceType.ordinal(), 1);
        }

        setResultConverter(buttonType -> {
            if (ButtonType.OK.equals(buttonType)) {
                return selectedResources;
            }
            return null;
        });
    }

    private String constructTooFewCardsString(final int amount, final Player player, final String action) {
        return String.format("You (%s) still need to %s %d cards", player.getName(), action, amount);
    }
}
