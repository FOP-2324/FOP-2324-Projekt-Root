package projekt.view.gameControls;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Builder;
import javafx.util.Duration;
import projekt.model.DevelopmentCardType;
import projekt.model.Player;
import projekt.model.ResourceType;
import projekt.view.CardPane;

public class PlayerInformationBuilder implements Builder<Region> {
    private final Player player;

    public PlayerInformationBuilder(Player player) {
        this.player = player;
    }

    @Override
    public Region build() {
        VBox mainBox = new VBox();
        Label resourcesLabel = new Label("Deine Resourcen:");
        FlowPane resourcesBox = new FlowPane(5, 5);
        for (ResourceType resourceType : player.getResources().keySet()) {
            CardPane resourceCard = new CardPane(resourceType.color,
                    Integer.toString(player.getResources().get(resourceType)));
            attachTooltip(resourceCard, resourceType.name());
            resourcesBox.getChildren().add(resourceCard);
        }

        Label developmentCardsLabel = new Label("Deine Entwicklungskarten:");
        FlowPane developmentCardsBox = new FlowPane(5, 5);
        for (DevelopmentCardType developmentCardType : player.getDevelopmentCards().keySet()) {
            CardPane developmentCardTypeCard = new CardPane(Color.LIGHTGRAY,
                    Integer.toString(player.getDevelopmentCards().get(developmentCardType)));
            attachTooltip(developmentCardTypeCard, developmentCardType.name());
            developmentCardsBox.getChildren().add(developmentCardTypeCard);
        }

        Label remainingRoadsLabel = new Label(
                String.format("Deine verbleibenden Straßen: %d", player.getRemainingRoads()));
        Label remainingSettlementsLabel = new Label(
                String.format("Deine verbleibenden Siedlungen: %d", player.getRemainingSettlements()));

        // TODO: get correct victory points
        Label victoryPointsLabel = new Label(String.format("Deine Siegpunkte: %d", 0));

        mainBox.getChildren().addAll(resourcesLabel, resourcesBox, developmentCardsLabel, developmentCardsBox,
                remainingRoadsLabel, remainingSettlementsLabel, victoryPointsLabel);
        mainBox.setPadding(new Insets(5));
        mainBox.setSpacing(5);
        return mainBox;
    }

    private void attachTooltip(Node region, String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.millis(100));
        Tooltip.install(region, tooltip);
    }
}
