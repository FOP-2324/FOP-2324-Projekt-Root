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
import projekt.controller.PlayerObjective;
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

//    @BeforeEach
//    void setup() {
//        this.hexGrid = new HexGridImpl(1);
//        this.gameState = new GameState(hexGrid, List.of(
//                new PlayerImpl(hexGrid, Color.RED),
//                new PlayerImpl(hexGrid, Color.BLUE)));
//        this.dice = Stream.generate(() -> 1).iterator();
//        this.gameController = new GameController(gameState, dice);
//    }
//
//    int setupPlayerResources() {
//        final int amount = 10000;
//        for (final Player player : gameState.getPlayers()) {
//            for (final ResourceType resourceType : ResourceType.values()) {
//                player.addResource(resourceType, amount);
//            }
//        }
//        return amount;
//    }
//
//    private Tile setupThirdVillage(final Player player) {
//        final Tile tile = hexGrid.getTileAt(0, 0);
//        tile.getIntersection(IntersectionDirection.NORTH_EAST)
//                .placeVillage(player, true);
//        tile.addRoad(EdgeDirection.EAST, player, true);
//        tile.addRoad(EdgeDirection.SOUTH_EAST, player, false);
//        tile.getIntersection(IntersectionDirection.SOUTH).placeVillage(player, true);
//        return tile;
//    }
//
//    @Test
//    void testInstantWin() {
//        this.gameState.getPlayers().get(0).getVictoryPointsProperty().set(100);
//        this.gameController.nextPlayer();
//        Assertions.assertTrue(this.gameState.isGameOver());
//    }
//
//    @Test
//    void correctWinners() {
//        this.gameState.getPlayers().get(0).getVictoryPointsProperty().set(100);
//        this.gameController.startGame();
//        Assertions.assertEquals(1, this.gameController.getWinners().size());
//        Assertions.assertTrue(
//                this.gameController.getWinners().contains(this.gameState.getPlayers().get(0)));
//    }
//
//    @Test
//    void firstRound() {
//        this.gameController.startGame();
//        this.gameController.nextPlayer();
//        for (final Player player : gameState.getPlayers()) {
//            Assertions.assertTrue(player.getResources().isEmpty());
//            Assertions.assertTrue(player.getDevelopmentCards().isEmpty());
//            Assertions.assertTrue(player.getRoads().isEmpty());
//        }
//    }
//
//    @Test
//    void setupRound() {
//        this.gameController.startGame();
//        final PlayerController playerController0 = this.gameController.getActivePlayerController();
//        Assertions.assertEquals(playerController0.getPlayerObjectiveProperty().getValue(),
//                PlayerObjective.PLACE_TWO_VILLAGES);
//        playerController0.endTurn();
//        Assertions.assertEquals(playerController0.getPlayerObjectiveProperty().getValue(),
//                PlayerObjective.PLACE_TWO_ROADS);
//        playerController0.endTurn();
//        Assertions.assertNotEquals(playerController0, this.gameController.getActivePlayerController());
//        final PlayerController playerController1 = this.gameController.getActivePlayerController();
//        Assertions.assertEquals(playerController1.getPlayerObjectiveProperty().getValue(),
//                PlayerObjective.PLACE_TWO_VILLAGES);
//        playerController1.endTurn();
//        Assertions.assertEquals(playerController1.getPlayerObjectiveProperty().getValue(),
//                PlayerObjective.PLACE_TWO_ROADS);
//        playerController1.endTurn();
//        Assertions.assertEquals(playerController0, this.gameController.getActivePlayerController());
//        Assertions.assertEquals(playerController0.getPlayerObjectiveProperty().getValue(),
//                PlayerObjective.REGULAR_TURN);
//    }
//
//    @Test
//    void buildFirstVillage() {
//        final PlayerController playerController = this.gameController.getPlayerControllers()
//                .get(gameState.getPlayers().get(0));
//        playerController.getPlayerObjectiveProperty().setValue(PlayerObjective.PLACE_TWO_VILLAGES);
//        Assertions.assertTrue(
//                playerController.buildVillage(hexGrid.getIntersections().values().iterator().next()));
//        Assertions.assertEquals(1, playerController.getPlayer().getSettlements().size());
//    }
//
//    @Test
//    void buildFirstRoad() {
//        final Tile tile = hexGrid.getTileAt(0, 0);
//        final PlayerController playerController = this.gameController.getPlayerControllers()
//                .get(gameState.getPlayers().get(0));
//        tile.getIntersection(IntersectionDirection.NORTH_EAST).placeVillage(playerController.getPlayer(), true);
//        playerController.getPlayerObjectiveProperty().setValue(PlayerObjective.PLACE_TWO_ROADS);
//        Assertions.assertTrue(playerController.buildRoad(tile, EdgeDirection.EAST));
//        Assertions.assertEquals(1, playerController.getPlayer().getRoads().size());
//    }
//
//    @Test
//    void firstRoadRequiresVillage() {
//        final Tile tile = hexGrid.getTileAt(0, 0);
//        final PlayerController playerController = this.gameController.getPlayerControllers()
//                .get(gameState.getPlayers().get(0));
//        playerController.getPlayerObjectiveProperty().setValue(PlayerObjective.PLACE_TWO_ROADS);
//        Assertions.assertFalse(playerController.buildRoad(tile, EdgeDirection.EAST));
//        Assertions.assertTrue(playerController.getPlayer().getRoads().isEmpty());
//    }
//
//    @Test
//    void firstRoadRequiresOwnVillage() {
//        final Tile tile = hexGrid.getTileAt(0, 0);
//        final PlayerController playerController = this.gameController.getPlayerControllers()
//                .get(gameState.getPlayers().get(0));
//        tile.getIntersection(IntersectionDirection.NORTH_EAST).placeVillage(gameState.getPlayers().get(1), true);
//        playerController.getPlayerObjectiveProperty().setValue(PlayerObjective.PLACE_TWO_ROADS);
//        Assertions.assertFalse(playerController.buildRoad(tile, EdgeDirection.EAST));
//        Assertions.assertTrue(playerController.getPlayer().getRoads().isEmpty());
//    }
//
//    @Test
//    void upgradeVillageWithoutVillage() {
//        setupPlayerResources();
//        this.gameController.startGame();
//        Assertions.assertFalse(this.gameController.getActivePlayerController()
//                .upgradeVillage(hexGrid.getIntersections().values().iterator().next()));
//        Assertions.assertTrue(this.gameController.getActivePlayerController().getPlayer().getSettlements().isEmpty());
//    }
//
//    @Test
//    void regularTurnCorrectPlayerObjective() {
//        this.gameController.startGame();
//        this.gameController.nextPlayer();
//        Assertions.assertEquals(this.gameController.getActivePlayerController().getPlayerObjectiveProperty().getValue(),
//                                PlayerObjective.REGULAR_TURN);
//    }
//
//    @Test
//    void diceRollSevenCorrectPlayerObjective() {
//        dice = Stream.generate(() -> 7).iterator();
//        this.gameController = new GameController(gameState, dice);
//        this.gameController.startGame();
//        this.gameController.nextPlayer();
//        final PlayerController playerController = this.gameController.getActivePlayerController();
//        Assertions.assertEquals(playerController.getPlayerObjectiveProperty().getValue(),
//                                PlayerObjective.SELECT_ROBBER_TILE);
//        playerController.endTurn();
//        Assertions.assertEquals(playerController.getPlayerObjectiveProperty().getValue(),
//                                PlayerObjective.SELECT_CARD_TO_STEAL);
//        playerController.endTurn();
//        Assertions.assertEquals(playerController.getPlayerObjectiveProperty().getValue(),
//                                PlayerObjective.REGULAR_TURN);
//    }
//
//    @Test
//    void tradeWithBank() {
//        final int resourceAmount = setupPlayerResources();
//        this.gameController.startGame();
//        final PlayerController playerController = this.gameController.getActivePlayerController();
//        playerController.tradeWithBank(ResourceType.CLAY, 4, ResourceType.WOOD);
//        Assertions.assertEquals((int) playerController.getPlayer().getResources().get(ResourceType.CLAY), resourceAmount - 4);
//        Assertions.assertEquals((int) playerController.getPlayer().getResources().get(ResourceType.WOOD), resourceAmount + 1);
//    }
//
//    @Test
//    void distributeResources() {
//        final Player player0 = gameState.getPlayers().get(0);
//        final Player player1 = gameState.getPlayers().get(1);
//        final Tile tile = hexGrid.getTileAt(0, 0);
//        tile.getIntersection(IntersectionDirection.NORTH).placeVillage(player0, true);
//        tile.getIntersection(IntersectionDirection.SOUTH).placeVillage(player1, true);
//        tile.getIntersection(IntersectionDirection.SOUTH).upgradeSettlement(player1);
//        this.gameController.distributeResources(tile.getRollNumber());
//
//        Assertions.assertNotNull(player0.getResources().get(tile.getType().resourceType));
//        Assertions.assertNotNull(player1.getResources().get(tile.getType().resourceType));
//        Assertions.assertEquals((int) player0.getResources()
//            .get(tile.getType().resourceType), Settlement.Type.VILLAGE.resourceAmount);
//        Assertions.assertEquals((int) player1.getResources()
//            .get(tile.getType().resourceType), Settlement.Type.CITY.resourceAmount);
//    }
//
//    @Test
//    void thirdVillageNeedsConnectedRoad() {
//        this.gameController.startGame();
//        setupPlayerResources();
//        final Player player = this.gameController.getActivePlayerController().getPlayer();
//        final Tile tile = setupThirdVillage(player);
//
//        Assertions.assertFalse(this.gameController.getActivePlayerController()
//                .buildVillage(tile.getIntersection(IntersectionDirection.NORTH_WEST)));
//        Assertions.assertEquals(2, player.getSettlements().size());
//    }
//
//    @Test
//    void thirdVillageNeedsResources() {
//        this.gameController.startGame();
//        final Player player = this.gameController.getActivePlayerController().getPlayer();
//        final Tile tile = setupThirdVillage(player);
//        tile.addRoad(EdgeDirection.SOUTH_WEST, player, false);
//        tile.addRoad(EdgeDirection.NORTH_WEST, player, false);
//        Assertions.assertFalse(this.gameController.getActivePlayerController()
//                .buildVillage(tile.getIntersection(IntersectionDirection.NORTH_WEST)));
//        Assertions.assertEquals(2, player.getSettlements().size());
//    }

}
