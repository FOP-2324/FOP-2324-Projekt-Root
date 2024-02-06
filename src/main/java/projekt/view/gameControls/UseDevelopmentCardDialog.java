package projekt.view.gameControls;

import java.util.Map;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import projekt.model.DevelopmentCardType;
import projekt.model.Player;
import projekt.view.CardPane;
import projekt.view.DevelopmentCardPane;

public class UseDevelopmentCardDialog extends Dialog<DevelopmentCardType> {

    private final ObjectProperty<CardPane> selectedCard = new SimpleObjectProperty<>();
    private final ObjectProperty<DevelopmentCardType> selectedDevelopmentCard = new SimpleObjectProperty<>();

    public UseDevelopmentCardDialog(final Player player) {
        this.setTitle("Select development card to play");
        this.setHeaderText("Select a development card to play");
        final GridPane mainPane = new GridPane(10, 10);
        mainPane.getStylesheets().add("css/hexmap.css");

        int cards = 0;
        for (final DevelopmentCardType developmentCard : player.getDevelopmentCards()
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 0 && entry.getKey() != DevelopmentCardType.VICTORY_POINTS)
                .map(Map.Entry::getKey)
                .toList()) {
            final CardPane cardPane = new DevelopmentCardPane(developmentCard, "", 50);
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

            final HBox developmentCardBox = new HBox(cardPane);
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
