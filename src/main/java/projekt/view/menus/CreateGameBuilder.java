package projekt.view.menus;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import projekt.model.Player;

public class CreateGameBuilder extends MenuBuilder {

    private final BiFunction<String, Color, Boolean> addPlayerHandler;
    private final ObservableList<Player> observablePlayers;
    private final Supplier<Boolean> startGameHandler;

    public CreateGameBuilder(final List<Player> players, final Runnable quitHandler,
            final BiFunction<String, Color, Boolean> addPlayerHandler, final Supplier<Boolean> startGameHandler) {
        super("Create Game", quitHandler);
        this.addPlayerHandler = addPlayerHandler;
        this.startGameHandler = startGameHandler;
        this.observablePlayers = FXCollections.observableArrayList(players);
    }

    @Override
    protected Node initCenter() {
        VBox mainBox = new VBox();

        ColorPicker playerColorPicker = new ColorPicker();
        TextField playerNameField = new TextField();
        Button addPlayerButton = new Button("Add Player");
        Label playerErrorLabel = new Label();
        addPlayerButton.setOnAction(e -> {
            if (!addPlayerHandler.apply(playerNameField.getText(), playerColorPicker.getValue())) {
                playerErrorLabel.setText("Cannot add player");
            }

            playerNameField.clear();
        });

        ListView<Player> playersList = new ListView<>(observablePlayers);
        playersList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Player player, boolean empty) {
                super.updateItem(player, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("Spieler %d", getIndex() + 1));
                    Rectangle colorRectangle = new Rectangle(15, 15, player.getColor());
                    colorRectangle.setStroke(Color.BLACK);
                    colorRectangle.setStrokeWidth(2);
                    setGraphic(colorRectangle);
                }
            }
        });

        Button startGameButton = new Button("Start Game");
        Label startGameErrorLabel = new Label();
        startGameButton.setOnAction(e -> {
            if (!startGameHandler.get()) {
                startGameErrorLabel.setText("Cannot start game");
            }
        });

        mainBox.getChildren().addAll(playerColorPicker, playerNameField, addPlayerButton, playerErrorLabel, playersList,
                startGameButton, startGameErrorLabel);
        return mainBox;
    }

    public void updatePlayers(List<Player> players) {
        observablePlayers.setAll(players);
    }

}
