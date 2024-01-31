package projekt.view.gameControls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import projekt.model.Player;
import projekt.model.ResourceType;
import projekt.view.CardPane;
import projekt.view.ResourceCardPane;

public class SelectResourceDialog extends Dialog<ResourceType> {

    private final ObjectProperty<CardPane> selectedCard = new SimpleObjectProperty<>();
    private final ObjectProperty<ResourceType> selectedResourceType = new SimpleObjectProperty<>();

    public SelectResourceDialog() {
        this.setTitle("Select resource");
        this.setHeaderText("Select a resource");
        final GridPane mainPane = new GridPane(10, 10);
        mainPane.getStylesheets().add("css/hexmap.css");

        int cards = 0;
        for (ResourceType resourceType : ResourceType.values()) {
            ResourceCardPane cardPane = new ResourceCardPane(resourceType);
            cardPane.getStyleClass().add("selectable");
            cardPane.setOnMouseClicked(e -> {
                selectedCard.set(cardPane);
                if (selectedResourceType.get() == resourceType) {
                    return;
                }
                selectedResourceType.set(resourceType);
            });
            selectedCard.subscribe((oldValue, newValue) -> {
                if (cardPane.equals(newValue)) {
                    cardPane.getStyleClass().add("selected");
                    return;
                }
                cardPane.getStyleClass().remove("selected");
            });

            HBox developmentCardBox = new HBox(cardPane);
            GridPane.setHgrow(developmentCardBox, Priority.ALWAYS);
            mainPane.add(cardPane, cards++, 0);
        }

        final DialogPane dialogPane = this.getDialogPane();
        dialogPane.setContent(mainPane);
        dialogPane.getButtonTypes().add(ButtonType.OK);

        setResultConverter(buttonType -> {
            if (ButtonType.OK.equals(buttonType)) {
                return selectedResourceType.get();
            }
            return null;
        });
    }
}
