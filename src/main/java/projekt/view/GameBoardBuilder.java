package projekt.view;

import java.util.List;
import java.util.function.Supplier;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
    private final Supplier<Region> actions;
    private final Pane playerInformation = new VBox();
    private final IntegerProperty diceRollProperty = new SimpleIntegerProperty(0);

    public GameBoardBuilder(final Region hexGrid, final Supplier<Region> actions) {
        this.hexGrid = hexGrid;
        this.actions = actions;
    }

    @Override
    public Region build() {
        final GridPane mainPane = new GridPane();
        mainPane.add(hexGrid, 0, 0);

        // Right box which holds the Players information

        VBox rightBox = new VBox();
        rightBox.getChildren().add(playerInformation);

        rightBox.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

        ScrollPane playersInformationPane = new ScrollPane(rightBox);
        rightBox.setMinWidth(150);
        rightBox.maxWidthProperty().bind(Bindings
                .createDoubleBinding(() -> playersInformationPane.getWidth() - 2,
                        playersInformationPane.widthProperty()));

        // Bottom box which holds the Player controls

        HBox bottomBox = new HBox();
        Label diceRoll = new Label();
        diceRoll.textProperty().bind(Bindings.createStringBinding(() -> String.format("Rolled Number: %s",
                diceRollProperty.get() == 0 ? "" : diceRollProperty.get()), diceRollProperty));
        bottomBox.getChildren().addAll(actions.get(), diceRoll);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setSpacing(10);
        bottomBox.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

        mainPane.add(bottomBox, 0, 1);
        mainPane.add(playersInformationPane, 1, 0, 1, 2);

        // TODO: Remove for release
        mainPane.setGridLinesVisible(true);

        // Make it look pretty

        ColumnConstraints hexGridColumn = new ColumnConstraints();
        hexGridColumn.setPercentWidth(75);
        ColumnConstraints playersInformationColumn = new ColumnConstraints();
        playersInformationColumn.setPercentWidth(25);

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

    public void updatePlayerInformation(Player player, List<Player> players) {
        playerInformation.getChildren().clear();
        playerInformation.getChildren().add(new PlayerInformationBuilder(player).build());
        playerInformation.getChildren().add(new PlayersOverviewBuilder(players).build());
    }

    public void setDiceRoll(int diceRoll) {
        diceRollProperty.set(diceRoll);
    }
}
