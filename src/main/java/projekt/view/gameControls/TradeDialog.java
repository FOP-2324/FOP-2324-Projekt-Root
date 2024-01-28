package projekt.view.gameControls;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.converter.IntegerStringConverter;
import projekt.model.ResourceType;
import projekt.model.TradePayload;
import projekt.view.CardPane;
import projekt.view.ResourceCardPane;
import projekt.view.Utils;

public class TradeDialog extends Dialog<TradePayload> {
    private final ObjectProperty<ResourceType> selectedBankOffer = new SimpleObjectProperty<>();
    private final ObjectProperty<ResourceType> selectedBankRequest = new SimpleObjectProperty<>();
    private final Map<ResourceType, Integer> playerOffer = new HashMap<>();
    private final Map<ResourceType, Integer> playerRequest = new HashMap<>();
    private final TabPane tabPane = new TabPane();

    public TradeDialog(TradePayload payload) {
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        tabPane.getStylesheets().add("css/hexmap.css");
        tabPane.getTabs().addAll(createBankTradeTab(payload), createPlayerTradeTab());
        setTitle("Trade");

        DialogPane dialogPane = getDialogPane();
        dialogPane.setContent(tabPane);
        dialogPane.getButtonTypes().add(ButtonType.OK);
        dialogPane.getButtonTypes().add(ButtonType.CANCEL);

        setResultConverter((buttonType) -> {
            if (buttonType.equals(ButtonType.OK)) {
                if (selectedBankOffer.getValue() == null || selectedBankRequest.getValue() == null) {
                    if (playerOffer.isEmpty() || playerRequest.isEmpty()) {
                        return null;
                    }
                    return new TradePayload(playerOffer, playerRequest, isResizable(), payload.player());
                }
                return new TradePayload(
                        Map.of(selectedBankOffer.getValue(),
                                payload.player().getTradeRatio(selectedBankOffer.getValue())),
                        Map.of(selectedBankRequest.getValue(), 1), true, payload.player());
            }
            return null;
        });
    }

    private Tab createBankTradeTab(TradePayload payload) {
        Tab bankTradeTab = new Tab("Bank");
        GridPane mainPane = new GridPane(10, 10);

        mainPane.add(new Label("Offer:"), 0, 0);
        mainPane.add(new Label("Request:"), 0, 1);

        for (ResourceType resourceType : ResourceType.values()) {
            HBox offeredResourceCard = new HBox(getSelectableResourceCard(resourceType, selectedBankOffer));
            offeredResourceCard.setAlignment(Pos.CENTER);

            Label ratio = new Label(String.format("%d", payload.player().getTradeRatio(resourceType)));

            VBox offeredResourceBox = new VBox(offeredResourceCard, ratio);
            offeredResourceBox.setSpacing(5);
            offeredResourceBox.setAlignment(Pos.CENTER);
            mainPane.add(offeredResourceBox, resourceType.ordinal() + 1, 0);
            GridPane.setHgrow(offeredResourceBox, Priority.ALWAYS);

            HBox requestedResourceCard = new HBox(getSelectableResourceCard(resourceType, selectedBankRequest));
            requestedResourceCard.setAlignment(Pos.CENTER);

            VBox requestedResourceBox = new VBox(requestedResourceCard);
            requestedResourceBox.setSpacing(5);
            requestedResourceBox.setAlignment(Pos.CENTER);
            mainPane.add(requestedResourceBox, resourceType.ordinal() + 1, 1);
            GridPane.setHgrow(requestedResourceBox, Priority.ALWAYS);
        }

        bankTradeTab.setContent(mainPane);
        return bankTradeTab;
    }

    private Tab createPlayerTradeTab() {
        Tab playerTradeTab = new Tab("Player");
        GridPane mainPane = new GridPane(10, 10);
        mainPane.add(new Label("Offer:"), 0, 1);
        mainPane.add(new Label("Request:"), 0, 2);

        for (ResourceType resourceType : ResourceType.values()) {
            CardPane resourceCard = new ResourceCardPane(resourceType, "", 50);
            mainPane.add(resourceCard, resourceType.ordinal() + 1, 0);

            TextField offeredResourcesField = new TextField();
            offeredResourcesField
                    .setTextFormatter(
                            new TextFormatter<>(new IntegerStringConverter(), 0, Utils.positiveIntegerFilter));
            offeredResourcesField.textProperty().subscribe((oldValue, newValue) -> {
                if (newValue == null || newValue.isBlank()) {
                    playerOffer.remove(resourceType);
                    return;
                }
                playerOffer.put(resourceType, Integer.parseInt(newValue));
            });
            mainPane.add(offeredResourcesField, resourceType.ordinal() + 1, 1);

            TextField requestedResourcesField = new TextField();
            requestedResourcesField
                    .setTextFormatter(
                            new TextFormatter<>(new IntegerStringConverter(), 0, Utils.positiveIntegerFilter));
            requestedResourcesField.textProperty().subscribe((oldValue, newValue) -> {
                if (newValue == null || newValue.isBlank()) {
                    playerRequest.remove(resourceType);
                    return;
                }
                playerRequest.put(resourceType, Integer.parseInt(newValue));
            });

            mainPane.add(requestedResourcesField, resourceType.ordinal() + 1, 2);
        }

        playerTradeTab.setContent(mainPane);
        return playerTradeTab;
    }

    private CardPane getSelectableResourceCard(ResourceType resourceType, Property<ResourceType> selectedResourceType) {
        CardPane requestedResourceCard = new ResourceCardPane(resourceType, "", 50);
        requestedResourceCard.getStyleClass().add("selectable");
        requestedResourceCard.setOnMouseClicked(e -> {
            if (Objects.equals(selectedResourceType.getValue(), resourceType)) {
                selectedResourceType.setValue(null);
                return;
            }
            selectedResourceType.setValue(resourceType);
        });
        selectedResourceType.subscribe((oldValue, newValue) -> {
            if (Objects.equals(newValue, resourceType)) {
                requestedResourceCard.getStyleClass().add("selected");
                return;
            }
            requestedResourceCard.getStyleClass().remove("selected");
        });
        return requestedResourceCard;
    }
}
