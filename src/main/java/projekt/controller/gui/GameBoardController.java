package projekt.controller.gui;

import projekt.controller.GameController;
import projekt.view.GameBoardBuilder;

public class GameBoardController extends SceneController {
    private final GameController gameController;

    protected GameBoardController(final GameController gameController) {
        super(new GameBoardBuilder(new HexGridController(gameController.getGrid()).buildView(),
                gameController.getPlayers()));
        this.gameController = gameController;
    }

    @Override
    public String getTitle() {
        return "Catan Runde 1";
    }

}
