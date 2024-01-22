package projekt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.scene.paint.Color;
import projekt.controller.GameController;
import projekt.model.Player;
import projekt.model.PlayerImpl;
import projekt.model.ResourceType;
import projekt.model.TilePosition.EdgeDirection;
import projekt.model.TilePosition.IntersectionDirection;
import projekt.model.tiles.Tile;

public class GameControllerTests {
    private GameController gameController;

    @BeforeEach
    void setup() {
        this.gameController = new GameController();
        this.gameController.getState().addPlayer(new PlayerImpl(this.gameController.getState().getGrid(), Color.RED));
        this.gameController.getState().addPlayer(new PlayerImpl(this.gameController.getState().getGrid(), Color.BLUE));
    }

    @Test
    void testInstantWin() {
        this.gameController.getState().getPlayers().get(0).getVictoryPointsProperty().set(100);
        this.gameController.startGame();
        Assertions.assertTrue(this.gameController.getState().isGameOver());
    }

    @Test
    void firstRound() {
        this.gameController.startGame();
        for (Player player : gameController.getState().getPlayers()) {
            Assertions.assertTrue(player.getResources().isEmpty());
            Assertions.assertTrue(player.getDevelopmentCards().isEmpty());
            Assertions.assertTrue(player.getRoads().isEmpty());
        }
    }

    @Test
    void buildVillage() {
        this.gameController.startGame();
        for (ResourceType resourceType : ResourceType.values()) {
            this.gameController.getPlayerController().getActivePlayer().addResource(resourceType, 10000);
        }
        this.gameController.getPlayerController()
                .buildVillage(gameController.getState().getGrid().getIntersections().values().iterator().next());
        Assertions.assertFalse(gameController.getPlayerController().getActivePlayer().getSettlements().isEmpty());
    }

    @Test
    void buildFirstRoad() {
        this.gameController.startGame();
        for (ResourceType resourceType : ResourceType.values()) {
            this.gameController.getPlayerController().getActivePlayer().addResource(resourceType, 10000);
        }
        Tile tile = gameController.getState().getGrid().getTileAt(0, 0);
        this.gameController.getPlayerController().buildVillage(tile.getIntersection(IntersectionDirection.NORTH_EAST));
        this.gameController.getPlayerController()
                .buildRoad(tile, EdgeDirection.EAST);
        Assertions.assertFalse(gameController.getPlayerController().getActivePlayer().getRoads().isEmpty());
    }
}
