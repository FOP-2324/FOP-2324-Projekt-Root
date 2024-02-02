package projekt.view;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Builder;
import projekt.model.Player;
import projekt.model.ResourceType;
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

        final VBox rightBox = new VBox();
        rightBox.getChildren().add(playerInformation);

        rightBox.setBackground(Background.fill(Color.WHITE));

        final ScrollPane playersInformationPane = new ScrollPane(rightBox);
        rightBox.setMinWidth(150);
        rightBox.maxWidthProperty().bind(Bindings.createDoubleBinding(() -> playersInformationPane.getWidth() - 2,
                playersInformationPane.widthProperty()));

        mainPane.setRight(playersInformationPane);

        // Bottom box which holds the Player controls

        final HBox bottomBox = new HBox();
        final Label diceRoll = new Label();
        diceRoll.textProperty()
                .bind(Bindings.createStringBinding(
                        () -> String.format("Rolled Number: %s",
                                diceRollProperty.get() == 0 ? "" : diceRollProperty.get()),
                        diceRollProperty));
        final Region actionsRegion = actions.get();
        bottomBox.getChildren().addAll(actionsRegion, diceRoll);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        bottomBox.setBackground(Background.fill(Color.WHITE));
        bottomBox.setMinHeight(50);
        bottomBox.setSpacing(10);

        mainPane.setBottom(bottomBox);

        // Make it look pretty

        return mainPane;
    }

    public void updatePlayerInformation(final Player player, final List<Player> players,
            final Map<ResourceType, Integer> changedResources) {
        playerInformation.getChildren().clear();
        playerInformation.getChildren().add(new PlayerInformationBuilder(player, changedResources).build());
        playerInformation.getChildren().add(new PlayersOverviewBuilder(players).build());
    }

    public void setDiceRoll(final int diceRoll) {
        diceRollProperty.set(diceRoll);
    }
}
