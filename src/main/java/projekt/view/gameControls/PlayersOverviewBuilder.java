package projekt.view.gameControls;

import java.util.List;

import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Builder;
import projekt.model.DevelopmentCardType;
import projekt.model.Player;
import projekt.view.CardPane;
import projekt.view.DevelopmentCardPane;

public class PlayersOverviewBuilder implements Builder<Region> {
    private final List<Player> players;

    public PlayersOverviewBuilder(final List<Player> players) {
        this.players = players;
    }

    @Override
    @StudentImplementationRequired
    public Region build() {
        final VBox mainBox = new VBox();
        for (final Player player : players) {
            mainBox.getChildren().add(createPlayerTiltedPane(player, players.indexOf(player) + 1));
        }
        return mainBox;
    }

    public TitledPane createPlayerTiltedPane(final Player player, final int playerNumber) {
        final GridPane detailsBox = new GridPane();

        final Label resourcesLabel = new Label("Resources:");
        detailsBox.add(resourcesLabel, 0, 0);
        detailsBox.add(createValuePane(
                Integer.toString(player.getResources().values().stream().reduce(0, Integer::sum))), 1, 0);

        final Label developmentCardsLabel = new Label("Development Cards:");
        detailsBox.add(developmentCardsLabel, 0, 1);
        detailsBox.add(createValuePane(
                Integer.toString(player.getDevelopmentCards().values().stream().reduce(0, Integer::sum))), 1, 1);

        final Label victoryPointsLabel = new Label(String.format("Victory Points: %d", player.getVictoryPoints()));
        detailsBox.add(victoryPointsLabel, 0, 2);

        final Label knightCardsLabel = new Label("Knights:");
        detailsBox.add(knightCardsLabel, 0, 3);
        detailsBox.add(new DevelopmentCardPane(DevelopmentCardType.KNIGHT, player.getKnightsPlayed()), 1, 3);

        final ColumnConstraints titleColumn = new ColumnConstraints();
        titleColumn.setPercentWidth(50);
        final ColumnConstraints valueColumn = new ColumnConstraints();
        valueColumn.setPercentWidth(50);

        detailsBox.getColumnConstraints().addAll(titleColumn, valueColumn);

        final TitledPane playerPane = new TitledPane(player.getName(), detailsBox);
        final Rectangle playerColor = new Rectangle(20, 20, player.getColor());
        playerColor.setStroke(Color.BLACK);
        playerColor.setStrokeWidth(2);
        playerPane.setGraphic(playerColor);
        return playerPane;
    }

    private StackPane createValuePane(final String value) {
        return createValuePane(value, null);
    }

    private StackPane createValuePane(final String value, final Image icon) {
        return new CardPane(Color.LIGHTGRAY, icon, value);
    }
}
