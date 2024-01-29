package projekt.view.menus;

import java.util.function.Predicate;
import java.util.function.Supplier;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.tudalgo.algoutils.student.annotation.DoNotTouch;
import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import projekt.model.PlayerImpl;

public class CreateGameBuilder extends MenuBuilder {
    private final ObservableList<PlayerImpl.Builder> observablePlayers;
    private final Supplier<Boolean> startGameHandler;

    @DoNotTouch
    public CreateGameBuilder(
        final ObservableList<PlayerImpl.Builder> players,
        final Runnable quitHandler,
        final Supplier<Boolean> startGameHandler
    ) {
        super("Start new Game", quitHandler);
        this.startGameHandler = startGameHandler;
        this.observablePlayers = players;
    }

    @Override
    @StudentImplementationRequired
    protected Node initCenter() {
        // populate initial Players
        final VBox mainBox = new VBox();
        // For icons see https://pictogrammers.com/library/mdi/
        final HBox addRemoveButtonRow = new HBox();
        addRemoveButtonRow.setAlignment(Pos.CENTER);
        final Button addPlayerButton2 = new Button(String.format("%c  Add Player", 0xF0417));
        addPlayerButton2.setOnAction(e -> this.observablePlayers.add(this.nextPlayerBuilder()));
        addRemoveButtonRow.getChildren().addAll(addPlayerButton2);
        final VBox playerListVBox = new VBox();
        this.observablePlayers.subscribe(() -> {
            playerListVBox.getChildren().clear();
            for (final var playerBuilder : this.observablePlayers) {
                final HBox playerListingHBox = new HBox();
                playerListingHBox.setAlignment(Pos.CENTER);
                final var playerNameTextField = new TextField(playerBuilder.nameOrDefault());
                playerNameTextField.setOnKeyPressed(e -> {
                    final var newName = playerNameTextField.getText();
                    if (newName.isBlank()) {
                        playerBuilder.name(null);
                        playerNameTextField.setText(playerBuilder.nameOrDefault());
                        playerNameTextField.selectAll();
                    } else {
                        playerBuilder.name(newName);
                    }
                });
                enum PlayerTypeIcos {
                    BOT(String.format("%c", 0xF167A)),
                    PLAYER(String.format("%c", 0xF0004));

                    public final String icon;

                    PlayerTypeIcos(String icon) {
                        this.icon = icon;
                    }

                    public static String getIcon(PlayerImpl.Builder builder) {
                        return builder.isAi() ? BOT.icon : PLAYER.icon;
                    }
                }
                final Button botOrPlayerSelectorButton = new Button();
                botOrPlayerSelectorButton.textProperty().bind(playerBuilder.aiProperty().map(x -> PlayerTypeIcos.getIcon(playerBuilder)));
                botOrPlayerSelectorButton.setOnAction(e -> playerBuilder.ai(!playerBuilder.isAi()));
                final var playerColorPicker = new ColorPicker(playerBuilder.getColor());
                playerColorPicker.setOnAction(e -> {
                    final var newColor = playerColorPicker.getValue();
                    if (this.observablePlayers
                        .stream()
                        .filter(Predicate.not(playerBuilder::equals))
                        .anyMatch(x -> x.getColor().equals(newColor))
                    ) {
                        new Alert(Alert.AlertType.ERROR, "Two Players cannot have the same color!").showAndWait();
                        playerColorPicker.setValue(playerBuilder.getColor());
                    } else {
                        playerBuilder.color(newColor);
                    }
                });
                final Button removePlayerButton = new Button(String.format("%c", 0xF0A79));
                removePlayerButton.setOnAction(e -> {
                    this.removePlayer(playerBuilder.getId());
                });
                playerListingHBox.getChildren().addAll(
                    playerNameTextField,
                    botOrPlayerSelectorButton,
                    playerColorPicker,
                    removePlayerButton
                );
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
            addRemoveButtonRow,
            playerListVBox,
//            playerColorPicker,
//            playerNameField,
//            addPlayerButton,
//            playerErrorLabel,
//            playersList,
            startGameButton,
            startGameErrorLabel
        );
//        mainBox.setPadding(new Insets(10));
        mainBox.alignmentProperty().set(Pos.TOP_CENTER);
        return mainBox;
    }

    public PlayerImpl.Builder nextPlayerBuilder() {
        return new PlayerImpl.Builder(this.observablePlayers.size() + 1);
    }

    private void removePlayer(final int id) {
        for (int i = id - 1; i < this.observablePlayers.size(); i++) {
            this.observablePlayers.get(i).id(i);
        }
        this.observablePlayers.remove(id - 1);
    }
}
