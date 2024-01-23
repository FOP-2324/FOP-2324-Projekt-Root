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
import projekt.controller.PlayerController.PlayerObjective;
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

    private Tile setupThirdVillage(Player player) {
        Tile tile = gameState.getGrid().getTileAt(0, 0);
        tile.getIntersection(IntersectionDirection.NORTH_EAST)
                .placeVillage(player, true);
        tile.addRoad(EdgeDirection.EAST, player, true);
        tile.addRoad(EdgeDirection.SOUTH_EAST, player, false);
        tile.getIntersection(IntersectionDirection.SOUTH).placeVillage(player, true);
        return tile;
    }

    @Test
    void testInstantWin() {
        this.gameState.getPlayers().get(0).getVictoryPointsProperty().set(100);
        this.gameController.nextPlayer();
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
        PlayerController playerController = this.gameController.getPlayerControllers()
                .get(gameState.getPlayers().get(0));
        playerController.getPlayerObjectiveProperty().setValue(PlayerObjective.PLACE_TWO_VILLAGES);
        Assertions.assertTrue(
                playerController.buildVillage(gameState.getGrid().getIntersections().values().iterator().next()));
        Assertions.assertTrue(playerController.getPlayer().getSettlements().size() == 1);
    }

    @Test
    void buildFirstRoad() {
        this.gameController.startGame();
        Tile tile = gameState.getGrid().getTileAt(0, 0);
        PlayerController playerController = this.gameController.getPlayerControllers()
                .get(gameState.getPlayers().get(0));
        tile.getIntersection(IntersectionDirection.NORTH_EAST).placeVillage(playerController.getPlayer(), true);
        playerController.getPlayerObjectiveProperty().setValue(PlayerObjective.PLACE_TWO_ROADS);
        Assertions.assertTrue(playerController.buildRoad(tile, EdgeDirection.EAST));
        Assertions.assertTrue(playerController.getPlayer().getRoads().size() == 1);
    }

    @Test
    void firstRoadRequiresVillage() {
        Tile tile = gameState.getGrid().getTileAt(0, 0);
        PlayerController playerController = this.gameController.getPlayerControllers()
                .get(gameState.getPlayers().get(0));
        playerController.getPlayerObjectiveProperty().setValue(PlayerObjective.PLACE_TWO_ROADS);
        Assertions.assertFalse(playerController.buildRoad(tile, EdgeDirection.EAST));
        Assertions.assertTrue(playerController.getPlayer().getRoads().isEmpty());
    }

    @Test
    void firstRoadRequiresOwnVillage() {
        Tile tile = gameState.getGrid().getTileAt(0, 0);
        PlayerController playerController = this.gameController.getPlayerControllers()
                .get(gameState.getPlayers().get(0));
        tile.getIntersection(IntersectionDirection.NORTH_EAST).placeVillage(gameState.getPlayers().get(1), true);
        playerController.getPlayerObjectiveProperty().setValue(PlayerObjective.PLACE_TWO_ROADS);
        Assertions.assertFalse(playerController.buildRoad(tile, EdgeDirection.EAST));
        Assertions.assertTrue(playerController.getPlayer().getRoads().isEmpty());
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
        this.gameController.nextPlayer();
        Assertions.assertEquals(this.gameController.getActivePlayerController().getPlayerObjectiveProperty().getValue(),
                PlayerController.PlayerObjective.REGULAR_TURN);
    }

    @Test
    void diceRollSevenCorrectPlayerObjective() {
        dice = Stream.generate(() -> 7).iterator();
        this.gameController = new GameController(gameState, dice);
        this.gameController.startGame();
        this.gameController.nextPlayer();
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

    @Test
    void thirdVillageNeedsConnectedRoad() {
        this.gameController.startGame();
        setupPlayerResources();
        Player player = this.gameController.getActivePlayerController().getPlayer();
        Tile tile = setupThirdVillage(player);

        Assertions.assertFalse(this.gameController.getActivePlayerController()
                .buildVillage(tile.getIntersection(IntersectionDirection.NORTH_WEST)));
        Assertions.assertTrue(player.getSettlements().size() == 2);
    }

    @Test
    void thirdVillageNeedsResources() {
        this.gameController.startGame();
        Player player = this.gameController.getActivePlayerController().getPlayer();
        Tile tile = setupThirdVillage(player);
        tile.addRoad(EdgeDirection.SOUTH_WEST, player, false);
        tile.addRoad(EdgeDirection.NORTH_WEST, player, false);
        Assertions.assertFalse(this.gameController.getActivePlayerController()
                .buildVillage(tile.getIntersection(IntersectionDirection.NORTH_WEST)));
        Assertions.assertTrue(player.getSettlements().size() == 2);
    }

}
