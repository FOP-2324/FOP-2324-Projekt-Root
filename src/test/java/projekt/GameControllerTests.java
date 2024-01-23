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
import projekt.model.buildings.Settlement;
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

    int setupPlayerResources() {
        int amount = 10000;
        for (Player player : gameState.getPlayers()) {
            for (ResourceType resourceType : ResourceType.values()) {
                player.addResource(resourceType, amount);
            }
        }
        return amount;
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
        this.gameController.getActivePlayerController()
                .buildVillage(gameState.getGrid().getIntersections().values().iterator().next());
        Assertions.assertFalse(gameController.getActivePlayerController().getPlayer().getSettlements().isEmpty());
    }

    @Test
    void buildFirstRoad() {
        this.gameController.startGame();
        setupPlayerResources();
        Tile tile = gameState.getGrid().getTileAt(0, 0);
        this.gameController.getActivePlayerController()
                .buildVillage(tile.getIntersection(IntersectionDirection.NORTH_EAST));
        this.gameController.getActivePlayerController()
                .buildRoad(tile, EdgeDirection.EAST);
        Assertions.assertFalse(gameController.getActivePlayerController().getPlayer().getRoads().isEmpty());
    }

    @Test
    void upgradeVillageWithoutVillage() {
        setupPlayerResources();
        this.gameController.startGame();
        Assertions.assertFalse(this.gameController.getActivePlayerController()
                .upgradeVillage(hexGrid.getIntersections().values().iterator().next()));
        Assertions.assertTrue(this.gameController.getActivePlayerController().getPlayer().getSettlements().isEmpty());
    }

    @Test
    void regularTurnCorrectPlayerObjective() {
        this.gameController.startGame();
        Assertions.assertEquals(this.gameController.getActivePlayerController().getPlayerObjectiveProperty().getValue(),
                PlayerController.PlayerObjective.REGULAR_TURN);
    }

    @Test
    void diceRollSevenCorrectPlayerObjective() {
        dice = Stream.generate(() -> 7).iterator();
        this.gameController = new GameController(gameState, dice);
        this.gameController.startGame();
        PlayerController playerController = this.gameController.getActivePlayerController();
        Assertions.assertEquals(playerController.getPlayerObjectiveProperty().getValue(),
                PlayerController.PlayerObjective.SELECT_ROBBER_TILE);
        playerController.endTurn();
        Assertions.assertEquals(playerController.getPlayerObjectiveProperty().getValue(),
                PlayerController.PlayerObjective.SELECT_CARD_TO_STEAL);
        playerController.endTurn();
        Assertions.assertEquals(playerController.getPlayerObjectiveProperty().getValue(),
                PlayerController.PlayerObjective.REGULAR_TURN);
    }

    @Test
    void tradeWithBank() {
        int resourceAmount = setupPlayerResources();
        this.gameController.startGame();
        PlayerController playerController = this.gameController.getActivePlayerController();
        playerController.tradeWithBank(ResourceType.CLAY, 4, ResourceType.WOOD);
        Assertions.assertTrue(
                playerController.getPlayer().getResources().get(ResourceType.CLAY) == resourceAmount - 4);
        Assertions.assertTrue(
                playerController.getPlayer().getResources().get(ResourceType.WOOD) == resourceAmount + 1);
    }

    @Test
    void distributeResources() {
        Player player0 = gameState.getPlayers().get(0);
        Player player1 = gameState.getPlayers().get(1);
        Tile tile = hexGrid.getTileAt(0, 0);
        tile.getIntersection(IntersectionDirection.NORTH).placeVillage(player0, true);
        tile.getIntersection(IntersectionDirection.SOUTH).placeVillage(player1, true);
        tile.getIntersection(IntersectionDirection.SOUTH).upgradeSettlement(player1);
        this.gameController.distributeResources(tile.getRollNumber());

        Assertions.assertNotNull(player0.getResources().get(tile.getType().resourceType));
        Assertions.assertNotNull(player1.getResources().get(tile.getType().resourceType));
        Assertions.assertTrue(player0.getResources()
                .get(tile.getType().resourceType) == Settlement.Type.VILLAGE.resourceAmount);
        Assertions.assertTrue(player1.getResources()
                .get(tile.getType().resourceType) == Settlement.Type.CITY.resourceAmount);
    }
}
