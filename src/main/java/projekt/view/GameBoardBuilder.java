package projekt.view;

import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Builder;
import projekt.model.Player;
import projekt.view.gameControls.PlayerInformationBuilder;
import projekt.view.gameControls.PlayersOverviewBuilder;

public class GameBoardBuilder implements Builder<Region> {
    private final Region hexGrid;
    private final List<Player> players;

    public GameBoardBuilder(final Region hexGrid, final List<Player> players) {
        this.hexGrid = hexGrid;
        this.players = players;
    }

    @Override
    public Region build() {
        final GridPane mainPane = new GridPane();
        mainPane.add(hexGrid, 0, 0);

        VBox playersInformationBox = new VBox();
        playersInformationBox.getChildren().add(new PlayerInformationBuilder(players.get(0)).build());
        playersInformationBox.getChildren().add(new PlayersOverviewBuilder(players).build());
        playersInformationBox.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        ScrollPane playersInformationPane = new ScrollPane(playersInformationBox);
        playersInformationBox.setMinWidth(150);
        playersInformationBox.maxWidthProperty().bind(Bindings
                .createDoubleBinding(() -> playersInformationPane.getWidth() - 2,
                        playersInformationPane.widthProperty()));

        mainPane.add(playersInformationPane, 1, 0, 1, 2);
        mainPane.setGridLinesVisible(true);

        ColumnConstraints hexGridColumn = new ColumnConstraints();
        hexGridColumn.setPercentWidth(85);
        ColumnConstraints playersInformationColumn = new ColumnConstraints();
        playersInformationColumn.setPercentWidth(15);

        RowConstraints hexGridRow = new RowConstraints();
        hexGridRow.setPercentHeight(90);
        RowConstraints playerActionRow = new RowConstraints();
        playerActionRow.setPercentHeight(10);

        mainPane.getColumnConstraints().addAll(hexGridColumn, playersInformationColumn);
        mainPane.getRowConstraints().addAll(hexGridRow, playerActionRow);

        hexGrid.minWidthProperty().bind(Bindings.createDoubleBinding(() -> hexGridColumn.getPercentWidth(),
                hexGridColumn.percentWidthProperty()));
        hexGrid.minHeightProperty().bind(
                Bindings.createDoubleBinding(() -> hexGridRow.getPercentHeight(), hexGridRow.percentHeightProperty()));

        return mainPane;
    }

}
