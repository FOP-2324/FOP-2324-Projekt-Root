package projekt.view.gameControls;

import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Builder;
import projekt.model.DevelopmentCardType;
import projekt.model.Player;
import projekt.model.ResourceType;
import projekt.view.CardPane;
import projekt.view.ResourceCardPane;
import projekt.view.Utils;

public class PlayerInformationBuilder implements Builder<Region> {
    private final Player player;

    public PlayerInformationBuilder(Player player) {
        this.player = player;
    }

    @Override
    @StudentImplementationRequired
    public Region build() {
        VBox mainBox = new VBox();
        // TODO: get player name when correctly implemented...
        Label playerName = new Label("Placeholder for name");

        Rectangle playerColor = new Rectangle(20, 20, player.getColor());
        playerColor.setStroke(Color.BLACK);
        playerColor.setStrokeWidth(2);
        playerName.setGraphic(playerColor);

        Label resourcesLabel = new Label("Deine Resourcen:");
        FlowPane resourcesBox = new FlowPane(5, 5);
        for (ResourceType resourceType : player.getResources().keySet()) {
            if (player.getResources().get(resourceType) == 0) {
                continue;
            }

            ResourceCardPane resourceCard = new ResourceCardPane(resourceType, player.getResources().get(resourceType));
            resourcesBox.getChildren().add(resourceCard);
        }

        Label developmentCardsLabel = new Label("Deine Entwicklungskarten:");
        FlowPane developmentCardsBox = new FlowPane(5, 5);
        for (DevelopmentCardType developmentCardType : player.getDevelopmentCards().keySet()) {
            CardPane developmentCardTypeCard = new CardPane(Color.LIGHTGRAY,
                    Integer.toString(player.getDevelopmentCards().get(developmentCardType)));
            Utils.attachTooltip(developmentCardType.toString(), developmentCardTypeCard);
            developmentCardsBox.getChildren().add(developmentCardTypeCard);
        }

        Label remainingRoadsLabel = new Label(
                String.format("Deine verbleibenden Straßen: %d", player.getRemainingRoads()));
        Label remainingVillagesLabel = new Label(
                String.format("Deine verbleibenden Dörfer: %d", player.getRemainingVillages()));
        Label remainingCitiesLabel = new Label(
                String.format("Deine verbleibenden Städte: %d", player.getRemainingCities()));

        Label victoryPointsLabel = new Label(String.format("Deine Siegpunkte: %d", player.getVictoryPoints()));

        mainBox.getChildren().addAll(playerName, resourcesLabel, resourcesBox, developmentCardsLabel,
                developmentCardsBox, remainingRoadsLabel, remainingVillagesLabel, remainingCitiesLabel,
                victoryPointsLabel);
        mainBox.setPadding(new Insets(5));
        mainBox.setSpacing(5);
        return mainBox;
    }
}
