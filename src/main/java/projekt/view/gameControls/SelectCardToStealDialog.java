package projekt.view.gameControls;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import projekt.model.Player;
import projekt.model.ResourceType;
import projekt.view.CardPane;
import projekt.view.PlayerLabel;

public class SelectCardToStealDialog extends Dialog<Entry<Player, ResourceType>> {
    private final ObservableMap<Player, ResourceType> selectedCard = FXCollections.observableMap(new HashMap<>());

    public SelectCardToStealDialog(final List<Player> players) {
        this.setTitle("Select card to steal");
        this.setHeaderText("Select a card to steal from a player");
        final GridPane mainPane = new GridPane(10, 10);

        for (final Player player : players) {
            mainPane.add(new PlayerLabel(player), 0, players.indexOf(player));
            for (final ResourceType resourceType : player.getResources().keySet()) {
                final CardPane resourceCard = new CardPane(Color.LIGHTGRAY, null, null, 40.0);
                resourceCard.getStyleClass().add("selectable");
                resourceCard.setOnMouseClicked(e -> {
                    if (selectedCard.containsValue(resourceType)) {
                        return;
                    }
                    selectedCard.put(player, resourceType);
                });
                selectedCard.addListener((MapChangeListener<Player, ResourceType>) change -> {
                    if (resourceType.equals(change.getValueAdded())) {
                        resourceCard.getStyleClass().add("selected");
                        return;
                    }
                    resourceCard.getStyleClass().remove("selected");
                });
                mainPane.add(resourceCard, resourceType.ordinal() + 1, players.indexOf(player));
            }
        }

        final DialogPane dialogPane = this.getDialogPane();
        dialogPane.setContent(mainPane);
        dialogPane.getButtonTypes().add(ButtonType.OK);

        setResultConverter(buttonType -> {
            if (buttonType.equals(ButtonType.OK)) {
                if (selectedCard.isEmpty()) {
                    return null;
                }
                return selectedCard.entrySet().iterator().next();
            }
            return null;
        });
    }
}
