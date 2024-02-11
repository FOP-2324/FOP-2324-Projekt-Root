package projekt.view.menus;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.tudalgo.algoutils.student.annotation.DoNotTouch;
import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;
import projekt.model.PlayerImpl;
import projekt.model.PlayerImpl.Builder;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A Builder to create the create game view.
 * The create game view lets users add and remove players and start the game.
 * It is possible to give each player a name, a color and to select whether the
 * player is a bot or not.
 */
public class CreateGameBuilder extends MenuBuilder {
    private final ObservableList<PlayerImpl.Builder> observablePlayers;
    private final Supplier<Boolean> startGameHandler;

    /**
     * Creates a new CreateGameBuilder with the given players and handlers.
     *
     * @param players          The list of players to display and modify.
     * @param returnHandler    The handler to call when the user wants to return to
     *                         the main menu
     * @param startGameHandler The handler to call when the user wants to start the
     *                         game
     */
    @DoNotTouch
    public CreateGameBuilder(
        final ObservableList<PlayerImpl.Builder> players,
        final Runnable returnHandler,
        final Supplier<Boolean> startGameHandler
    ) {
        super("Start new Game", returnHandler);
        this.startGameHandler = startGameHandler;
        this.observablePlayers = players;
    }

    @Override
    protected Node initCenter() {
        final VBox mainBox = new VBox();
        mainBox.setStyle("-fx-font-size: 2em");
        // For icons see https://pictogrammers.com/library/mdi/
        final VBox playerListVBox = new VBox();
        this.observablePlayers.subscribe(() -> {
            playerListVBox.getChildren().clear();
            for (final PlayerImpl.Builder playerBuilder : this.observablePlayers) {
                final HBox playerListingHBox = new HBox();
                playerListingHBox.setAlignment(Pos.CENTER);
                final TextField playerNameTextField = new TextField(playerBuilder.nameOrDefault());
                playerNameTextField.setOnKeyPressed(e -> {
                    final String newName = playerNameTextField.getText();
                    if (newName.isBlank()) {
                        playerBuilder.name(null);
                        playerNameTextField.setText(playerBuilder.nameOrDefault());
                        playerNameTextField.selectAll();
                    } else {
                        playerBuilder.name(newName);
                    }
                });
                playerListingHBox.getChildren().addAll(
                    playerNameTextField,
                    createBotOrPlayerSelector(playerBuilder), // TODO: Remove from student template
                    createPlayerColorPicker(playerBuilder), // TODO: Remove from student template
                    createRemovePlayerButton(playerBuilder.getId())
                ); // TODO: Remove from student template
                playerListVBox.getChildren().add(playerListingHBox);
            }
        });

        final Button startGameButton = new Button("Start Game");
        final Label startGameErrorLabel = new Label();
        startGameButton.setOnAction(e -> {
            if (!this.startGameHandler.get()) {
                startGameErrorLabel.setText("Cannot start game");
            }
        });

        mainBox.getChildren().addAll(
            createAddPlayerButton(), // TODO: Remove from student template
            playerListVBox,
            startGameButton,
            startGameErrorLabel
        );
        mainBox.alignmentProperty().set(Pos.TOP_CENTER);
        return mainBox;
    }

    /**
     * Creates a button to add a new player to the game.
     * The button adds a new player to the list of players when clicked.
     *
     * @return a button to add a new player to the game
     */
    @StudentImplementationRequired("H3.4")
    private Node createAddPlayerButton() {
        final HBox addRemoveButtonRow = new HBox();
        addRemoveButtonRow.setAlignment(Pos.CENTER);
        final Button addPlayerButton = new Button(String.format("%c  Add Player", 0xF0417));
        addPlayerButton.setOnAction(e -> this.observablePlayers.add(this.nextPlayerBuilder()));
        addRemoveButtonRow.getChildren().addAll(addPlayerButton);
        return addRemoveButtonRow;
    }

    /**
     * Creates a color picker to select the color of the player.
     * Two players cannot have the same color.
     *
     * @param playerBuilder the builder for the player to create the color picker
     *                      for
     * @return a color picker to select the color of the player
     */
    @StudentImplementationRequired("H3.4")
    private Node createPlayerColorPicker(final Builder playerBuilder) {
        final ColorPicker playerColorPicker = new ColorPicker(playerBuilder.getColor());
        playerColorPicker.setOnAction(e -> {
            final Color newColor = playerColorPicker.getValue();
            if (this.observablePlayers
                .stream()
                .filter(Predicate.not(playerBuilder::equals))
                .anyMatch(x -> x.getColor().equals(newColor))) {
                new Alert(Alert.AlertType.ERROR, "Two Players cannot have the same color!").showAndWait();
                playerColorPicker.setValue(playerBuilder.getColor());
            } else {
                playerBuilder.color(newColor);
            }
        });
        return playerColorPicker;
    }

    /**
     * Creates a node to select whether the player is a bot or not.
     *
     * @param playerBuilder the builder for the player to create the selector for
     * @return a node to select whether the player is a bot or not
     */
    @StudentImplementationRequired("H3.4")
    private Node createBotOrPlayerSelector(final Builder playerBuilder) {
        enum PlayerTypeIcos {
            BOT(String.format("%c", 0xF167A)),
            PLAYER(String.format("%c", 0xF0004));

            public final String icon;

            PlayerTypeIcos(final String icon) {
                this.icon = icon;
            }

            public static String getIcon(final PlayerImpl.Builder builder) {
                return builder.isAi() ? BOT.icon : PLAYER.icon;
            }
        }
        final Button botOrPlayerSelectorButton = new Button();
        botOrPlayerSelectorButton.textProperty()
            .bind(playerBuilder.aiProperty().map(x -> PlayerTypeIcos.getIcon(playerBuilder)));
        botOrPlayerSelectorButton.setOnAction(e -> playerBuilder.ai(!playerBuilder.isAi()));
        return botOrPlayerSelectorButton;
    }

    /**
     * Creates a button to remove the player with the given id.
     *
     * @param id the id of the player to remove
     * @return a button to remove the player with the given id
     */
    @StudentImplementationRequired("H3.4")
    private Button createRemovePlayerButton(final int id) {
        final Button removePlayerButton = new Button(String.format("%c", 0xF0A79));
        removePlayerButton.setOnAction(e -> {
            this.removePlayer(id);
        });
        return removePlayerButton;
    }

    /**
     * Removes the player with the given id and updates the ids of the remaining
     * players.
     *
     * @param id the id of the player to remove
     */
    @StudentImplementationRequired("H3.4")
    private void removePlayer(final int id) {
        for (int i = id - 1; i < this.observablePlayers.size(); i++) {
            this.observablePlayers.get(i).id(i);
        }
        this.observablePlayers.remove(id - 1);
    }

    /**
     * Returns a new {@link PlayerImpl.Builder} for the player with the next id.
     *
     * @return a new {@link PlayerImpl.Builder} for the player with the next id
     */
    public PlayerImpl.Builder nextPlayerBuilder() {
        return new PlayerImpl.Builder(this.observablePlayers.size() + 1);
    }

}
