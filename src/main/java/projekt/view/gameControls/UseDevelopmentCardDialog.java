package projekt.view.gameControls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import projekt.model.DevelopmentCardType;
import projekt.model.Player;
import projekt.view.CardPane;
import projekt.view.Utils;

import java.util.Map;

public class UseDevelopmentCardDialog extends Dialog<DevelopmentCardType> {

    private final ObjectProperty<CardPane> selectedCard = new SimpleObjectProperty<>();
    private final ObjectProperty<DevelopmentCardType> selectedDevelopmentCard = new SimpleObjectProperty<>();

    public UseDevelopmentCardDialog(Player player) {
        this.setTitle("Select development card to play");
        this.setHeaderText("Select a development card to play");
        final GridPane mainPane = new GridPane(10, 10);
        mainPane.getStylesheets().add("css/hexmap.css");

        int cards = 0;
        for (DevelopmentCardType developmentCard : player.getDevelopmentCards()
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() > 0 && entry.getKey() != DevelopmentCardType.VICTORY_POINTS)
            .map(Map.Entry::getKey)
            .toList()
        ) {
            CardPane cardPane = new CardPane(Color.LIGHTGRAY, Utils.emptyCardImage, developmentCard.toString());
            cardPane.getStyleClass().add("selectable");
            cardPane.setOnMouseClicked(e -> {
                selectedCard.set(cardPane);
                if (selectedDevelopmentCard.get() == developmentCard) {
                    return;
                }
                selectedDevelopmentCard.set(developmentCard);
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
                return selectedDevelopmentCard.get();
            }
            return null;
        });
    }
}
