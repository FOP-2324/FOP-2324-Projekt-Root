package projekt.controller.gui;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Builder;
import org.jetbrains.annotations.Nullable;
import projekt.Config;
import projekt.model.GameState;
import projekt.model.PlayerImpl;
import projekt.view.menus.CreateGameBuilder;

import java.util.ArrayList;

public class CreateGameController implements SceneController {
    private final CreateGameBuilder builder;
    private final GameState gameState;
    private final ObservableList<PlayerImpl.Builder> playerBuilderList = FXCollections.observableArrayList();

    public CreateGameController(final GameState gameState) {
        this.gameState = gameState;
        this.builder = new CreateGameBuilder(
                this.playerBuilderList,
                SceneController::loadMainMenuScene,
                this::startGameHandler);
    }

    @Override
    public Region buildView() {
        final var tmp = SceneController.super.buildView();
        // initial Players
        this.playerBuilderList.add(
                this.builder.nextPlayerBuilder()
                .name(System.getProperty("user.name"))
                .color(Color.AQUA)
        );
        this.playerBuilderList.add(this.builder.nextPlayerBuilder());
        return tmp;
    }

    private boolean startGameHandler() {
        if (this.playerBuilderList.size() < Config.MIN_PLAYERS) {
            return false;
        }
        this.playerBuilderList.forEach(p ->
                this.gameState.addPlayer(p.build(this.gameState.getGrid()))
        );
        SceneController.loadGameScene();
        return true;
    }

    @Override
    public Builder<Region> getBuilder() {
        return this.builder;
    }

    @Override
    public String getTitle() {
        return "Create Game";
    }

}
