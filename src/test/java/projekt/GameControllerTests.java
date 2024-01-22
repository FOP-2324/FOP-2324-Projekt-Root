package projekt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Iterator;
import java.util.stream.Stream;
import javafx.scene.paint.Color;
import projekt.controller.GameController;
import projekt.controller.PlayerController;
import projekt.model.GameState;
import projekt.model.HexGrid;
import projekt.model.HexGridImpl;
import projekt.model.Player;
import projekt.model.PlayerImpl;
import projekt.model.ResourceType;
import projekt.model.TilePosition.EdgeDirection;
import projekt.model.TilePosition.IntersectionDirection;
import projekt.model.tiles.Tile;

public class GameControllerTests {
    private GameController gameController;
    private GameState gameState;
    private HexGrid hexGrid;
    private Iterator<Integer> dice;

    @BeforeEach
    void setup() {
        this.hexGrid = new HexGridImpl(1);
        this.gameState = new GameState(hexGrid, List.of(
                new PlayerImpl(hexGrid, Color.RED),
                new PlayerImpl(hexGrid, Color.BLUE)));
        this.dice = Stream.generate(() -> 1).iterator();
        this.gameController = new GameController(gameState, dice);

    }

    void setupPlayerResources() {
        for (Player player : gameState.getPlayers()) {
            for (ResourceType resourceType : ResourceType.values()) {
                player.addResource(resourceType, 10000);
            }
        }
    }

    @Test
    void testInstantWin() {
        this.gameState.getPlayers().get(0).getVictoryPointsProperty().set(100);
        this.gameController.startGame();
        Assertions.assertTrue(this.gameState.isGameOver());
    }

    @Test
    void correctWinners() {
        this.gameState.getPlayers().get(0).getVictoryPointsProperty().set(100);
        this.gameController.startGame();
        Assertions.assertTrue(this.gameController.getWinners().size() == 1);
        Assertions.assertTrue(
                this.gameController.getWinners().contains(this.gameState.getPlayers().get(0)));
    }

    @Test
    void firstRound() {
        this.gameController.startGame();
        for (Player player : gameState.getPlayers()) {
            Assertions.assertTrue(player.getResources().isEmpty());
            Assertions.assertTrue(player.getDevelopmentCards().isEmpty());
            Assertions.assertTrue(player.getRoads().isEmpty());
        }
    }

    @Test
    void buildFirstVillage() {
        this.gameController.startGame();
        this.gameController.getPlayerController()
                .buildVillage(gameState.getGrid().getIntersections().values().iterator().next());
        Assertions.assertFalse(gameController.getPlayerController().getActivePlayer().getSettlements().isEmpty());
    }

    @Test
    void buildFirstRoad() {
        this.gameController.startGame();
        setupPlayerResources();
        Tile tile = gameState.getGrid().getTileAt(0, 0);
        this.gameController.getPlayerController().buildVillage(tile.getIntersection(IntersectionDirection.NORTH_EAST));
        this.gameController.getPlayerController()
                .buildRoad(tile, EdgeDirection.EAST);
        Assertions.assertFalse(gameController.getPlayerController().getActivePlayer().getRoads().isEmpty());
    }

    @Test
    void upgradeVillageWithoutVillage() {
        setupPlayerResources();
        this.gameController.startGame();
        Assertions.assertFalse(this.gameController.getPlayerController()
                .upgradeVillage(hexGrid.getIntersections().values().iterator().next()));
        Assertions.assertTrue(this.gameController.getPlayerController().getActivePlayer().getSettlements().isEmpty());
    }

    @Test
    void regularTurnCorrectPlayerObjective() {
        this.gameController.startGame();
        Assertions.assertEquals(this.gameController.getPlayerController().getPlayerObjectiveProperty().getValue(),
                PlayerController.PlayerObjective.REGULAR_TURN);
    }

    @Test
    void diceRollSevenCorrectPlayerObjective() {
        dice = Stream.generate(() -> 7).iterator();
        this.gameController = new GameController(gameState, dice);
        this.gameController.startGame();
        PlayerController playerController = this.gameController.getPlayerController();
        Assertions.assertEquals(playerController.getPlayerObjectiveProperty().getValue(),
                PlayerController.PlayerObjective.SELECT_ROBBER_TILE);
        playerController.endTurn();
        Assertions.assertEquals(playerController.getPlayerObjectiveProperty().getValue(),
                PlayerController.PlayerObjective.SELECT_CARD_TO_STEAL);
        playerController.endTurn();
        Assertions.assertEquals(playerController.getPlayerObjectiveProperty().getValue(),
                PlayerController.PlayerObjective.REGULAR_TURN);
    }
}
