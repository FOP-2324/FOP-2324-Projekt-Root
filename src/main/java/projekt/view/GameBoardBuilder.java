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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
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
        final BorderPane mainPane = new BorderPane();
        mainPane.setCenter(hexGrid);

        // Right box which holds the Players information

        VBox rightBox = new VBox();
        rightBox.getChildren().add(playerInformation);

        rightBox.setBackground(Background.fill(Color.WHITE));

        ScrollPane playersInformationPane = new ScrollPane(rightBox);
        rightBox.setMinWidth(150);
        rightBox.maxWidthProperty().bind(Bindings
                .createDoubleBinding(() -> playersInformationPane.getWidth() - 2,
                        playersInformationPane.widthProperty()));

        mainPane.setRight(playersInformationPane);

        // Bottom box which holds the Player controls

        FlowPane bottomBox = new FlowPane(10, 10);
        Label diceRoll = new Label();
        diceRoll.textProperty().bind(Bindings.createStringBinding(() -> String.format("Rolled Number: %s",
                diceRollProperty.get() == 0 ? "" : diceRollProperty.get()), diceRollProperty));
        bottomBox.getChildren().addAll(actions.get(), diceRoll);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setBackground(Background.fill(Color.WHITE));

        mainPane.setBottom(bottomBox);

        // Make it look pretty

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
