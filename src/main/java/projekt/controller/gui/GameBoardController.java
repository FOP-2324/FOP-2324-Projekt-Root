package projekt.controller.gui;

import javafx.scene.layout.Region;
import javafx.util.Builder;
import projekt.controller.GameController;
import projekt.view.GameBoardBuilder;

public class GameBoardController implements SceneController {
    private final GameController gameController;
    private final Builder<Region> gameBoardBuilder;

    protected GameBoardController(final GameController gameController) {
        this.gameBoardBuilder = new GameBoardBuilder(new HexGridController(gameController.getGrid()).buildView(),
                gameController.getPlayers());
        this.gameController = gameController;
    }

    @Override
    public String getTitle() {
        return "Catan Runde 1";
    }

    @Override
    public Builder<Region> getBuilder() {
        return gameBoardBuilder;
    }

}
