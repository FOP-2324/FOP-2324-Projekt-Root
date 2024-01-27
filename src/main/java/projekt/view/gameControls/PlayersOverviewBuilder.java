package projekt.view.gameControls;

import java.util.List;

import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Builder;
import projekt.model.Player;
import projekt.view.CardPane;

public class PlayersOverviewBuilder implements Builder<Region> {
    private final List<Player> players;

    public PlayersOverviewBuilder(List<Player> players) {
        this.players = players;
    }

    @Override
    @StudentImplementationRequired
    public Region build() {
        VBox mainBox = new VBox();
        for (Player player : players) {
            mainBox.getChildren().add(createPlayerTiltedPane(player, players.indexOf(player) + 1));
        }
        mainBox.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        return mainBox;
    }

    public TitledPane createPlayerTiltedPane(Player player, int playerNumber) {
        GridPane detailsBox = new GridPane();

        Label resourcesLabel = new Label("Rohstoffe:");
        detailsBox.add(resourcesLabel, 0, 0);
        detailsBox.add(createValuePane(
                Integer.toString(player.getResources().values().stream().reduce(0, Integer::sum))), 1, 0);

        Label developmentCardsLabel = new Label("Entwicklungskarten:");
        detailsBox.add(developmentCardsLabel, 0, 1);
        detailsBox.add(createValuePane(
                Integer.toString(player.getDevelopmentCards().values().stream().reduce(0, Integer::sum))), 1, 1);

        Label victoryPointsLabel = new Label(String.format("Siegpunkte: %d", player.getVictoryPoints()));
        detailsBox.add(victoryPointsLabel, 0, 2);

        Label knightCardsLabel = new Label("Ritterkarten:");
        detailsBox.add(knightCardsLabel, 0, 3);
        detailsBox.add(createValuePane(Integer.toString(player.getKnightsPlayed()), "img/knight.png"), 1, 3);

        ColumnConstraints titleColumn = new ColumnConstraints();
        titleColumn.setPercentWidth(50);
        ColumnConstraints valueColumn = new ColumnConstraints();
        valueColumn.setPercentWidth(50);

        detailsBox.getColumnConstraints().addAll(titleColumn, valueColumn);

        // TODO: replace with player name
        TitledPane playerPane = new TitledPane(String.format("Spieler %d", playerNumber), detailsBox);
        Rectangle playerColor = new Rectangle(20, 20, player.getColor());
        playerColor.setStroke(Color.BLACK);
        playerColor.setStrokeWidth(2);
        playerPane.setGraphic(playerColor);
        return playerPane;
    }

    private StackPane createValuePane(String value) {
        return createValuePane(value, null);
    }

    private StackPane createValuePane(String value, String iconPath) {
        return new CardPane(Color.LIGHTGRAY, iconPath, value);
    }
}
