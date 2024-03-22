package projekt.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSet;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSetTest;
import projekt.Config;
import projekt.SubmissionExecutionHandler;
import projekt.controller.actions.IllegalActionException;
import projekt.model.*;
import projekt.model.buildings.Settlement;
import projekt.util.Utils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.*;

@TestForSubmission
public class PlayerControllerTest {

    private final SubmissionExecutionHandler executionHandler = SubmissionExecutionHandler.getInstance();
    private final HexGrid hexGrid = new HexGridImpl(Config.GRID_RADIUS);
    private final List<Player> players = IntStream.range(0, Config.MAX_PLAYERS)
        .mapToObj(i -> new PlayerImpl.Builder(i).build(hexGrid))
        .toList();
    private GameController gameController;
    private Player player;
    private PlayerController playerController;
    private Context baseContext;

    @BeforeEach
    public void setup() throws NoSuchMethodException {
        GameState gameState = new GameState(hexGrid, players);
        gameController = new GameController(gameState);
        player = players.get(0);
        playerController = new PlayerController(gameController, player);
        baseContext = contextBuilder()
            .add("player", player)
            .add("playerController", playerController)
            .build();

        executionHandler.substituteMethod(PlayerController.class.getDeclaredMethod("blockingGetNextAction"),
            invocation -> null);
        executionHandler.substituteMethod(PlayerController.class.getDeclaredMethod("waitForNextAction"),
            invocation -> null);
    }

    @AfterEach
    public void reset() {
        executionHandler.resetMethodInvocationLogging();
        executionHandler.resetMethodDelegation();
        executionHandler.resetMethodSubstitution();
    }

    @ParameterizedTest
    @JsonParameterSetTest("/controller/PlayerController/acceptTradeOffer.json")
    public void testAcceptTradeOffer(JsonParameterSet jsonParams) throws ReflectiveOperationException {
        Field tradingPlayerField = PlayerController.class.getDeclaredField("tradingPlayer");
        Field playerTradingOfferField = PlayerController.class.getDeclaredField("playerTradingOffer");
        Field playerTradingRequestField = PlayerController.class.getDeclaredField("playerTradingRequest");
        tradingPlayerField.trySetAccessible();
        playerTradingOfferField.trySetAccessible();
        playerTradingRequestField.trySetAccessible();

        Player tradingPlayer = jsonParams.getBoolean("tradingPlayerSet") ? players.get(1) : null;
        Map<ResourceType, Integer> playerTradingOffer = Utils.deserializeEnumMap(jsonParams.get("playerTradingOffer"), ResourceType.class, Utils.AS_INTEGER);
        Map<ResourceType, Integer> playerTradingRequest = Utils.deserializeEnumMap(jsonParams.get("playerTradingRequest"), ResourceType.class, Utils.AS_INTEGER);
        player.addResources(jsonParams.getBoolean("enablePlayerInventory") && playerTradingRequest != null ? playerTradingRequest : Collections.emptyMap());
        if (tradingPlayer != null) {
            tradingPlayer.addResources(jsonParams.getBoolean("enablePartnerInventory") && playerTradingOffer != null ? playerTradingOffer : Collections.emptyMap());
        }

        boolean accepted = jsonParams.getBoolean("accepted");

        playerTradingOfferField.set(playerController, playerTradingOffer);
        playerTradingRequestField.set(playerController, playerTradingRequest);
        tradingPlayerField.set(playerController, tradingPlayer);

        Context context = contextBuilder()
            .add(baseContext)
            .add("tradingPlayer", tradingPlayer)
            .add("playerTradingOffer", playerTradingOffer)
            .add("playerTradingRequest", playerTradingRequest)
            .add("accepted", accepted)
            .build();
        executionHandler.disableMethodDelegation(PlayerController.class.getDeclaredMethod("acceptTradeOffer", boolean.class));
        if (jsonParams.getBoolean("exception")) {
            Exception e = assertThrows(IllegalActionException.class, () -> playerController.acceptTradeOffer(accepted), context, result ->
                "Expected PlayerController.acceptTradeOffer to throw an IllegalActionException");
            if (jsonParams.getString("exceptionMessage") != null) {
                assertEquals(jsonParams.getString("exceptionMessage"), e.getMessage(), context, result ->
                    "The message of the exception thrown by PlayerController.acceptTradeOffer is not correct");
            }
        } else {
            call(() -> playerController.acceptTradeOffer(accepted), context, result ->
                "PlayerController.acceptTradeOffer threw an uncaught exception");

            if (accepted) {
                Map<ResourceType, Integer> originalOffer = Utils.deserializeEnumMap(jsonParams.get("playerTradingOffer"), ResourceType.class, Utils.AS_INTEGER);
                Map<ResourceType, Integer> originalRequest = Utils.deserializeEnumMap(jsonParams.get("playerTradingRequest"), ResourceType.class, Utils.AS_INTEGER);

                assertEquals(originalOffer,
                    player.getResources()
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getValue() > 0)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                    context,
                    result -> "Current player's inventory does not equal the expected state");
                assertEquals(originalRequest,
                    tradingPlayer.getResources()
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getValue() > 0)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                    context,
                    result -> "Trading partner's inventory does not equal the expected state");
            }

            assertEquals(PlayerObjective.IDLE, playerController.getPlayerObjectiveProperty().getValue(), context, result ->
                "PlayerController.acceptTradeOffer did not set the player objective to IDLE");
        }
    }

    @ParameterizedTest
    @JsonParameterSetTest("/controller/PlayerController/tradeWithBank.json")
    public void testTradeWithBank(JsonParameterSet jsonParams) throws NoSuchMethodException {
        ResourceType offerType = jsonParams.get("offerType", ResourceType.class);
        int offerAmount = jsonParams.getInt("offerAmount");
        ResourceType request = jsonParams.get("request", ResourceType.class);
        int tradeRatio = jsonParams.getInt("tradeRatio");

        player.addResources(jsonParams.getBoolean("enablePlayerInventory") ? Map.of(offerType, offerAmount) : Collections.emptyMap());
        executionHandler.substituteMethod(PlayerImpl.class.getDeclaredMethod("getTradeRatio", ResourceType.class),
            invocation -> tradeRatio);

        Context context = contextBuilder()
            .add(baseContext)
            .add("offerType", offerType)
            .add("offerAmount", offerAmount)
            .add("request", request)
            .add("trade ratio", tradeRatio)
            .build();
        if (jsonParams.getBoolean("exception")) {
            assertThrows(IllegalActionException.class, () -> playerController.tradeWithBank(offerType, offerAmount, request), context, result ->
                "Expected PlayerController.tradeWithBank to throw an IllegalActionException");
        } else {
            call(() -> playerController.tradeWithBank(offerType, offerAmount, request), context, result ->
                "PlayerController.tradeWithBank threw an uncaught exception");
            assertEquals(Map.of(offerType, 0, request, 1),
                player.getResources(),
                context,
                result -> "Player's inventory is not in the expected state after invoking PlayerController.tradeWithBank");
        }
    }

    @ParameterizedTest
    @JsonParameterSetTest("/controller/PlayerController/canBuildVillage.json")
    public void testCanBuildVillage(JsonParameterSet jsonParams) throws NoSuchMethodException {
        player.addResources(jsonParams.getBoolean("hasResources") ? Config.SETTLEMENT_BUILDING_COST.get(Settlement.Type.VILLAGE) : Collections.emptyMap());
        executionHandler.substituteMethod(PlayerImpl.class.getDeclaredMethod("getRemainingVillages"),
            invocation -> jsonParams.getInt("remainingVillages"));

        boolean objectiveSet = jsonParams.getBoolean("objectiveSet");
        Context context = contextBuilder()
            .add(baseContext)
            .add("objective set to PLACE_VILLAGE", objectiveSet)
            .add("remaining villages", jsonParams.getInt("remainingVillages"))
            .build();
        if (objectiveSet) {
            playerController.setPlayerObjective(PlayerObjective.PLACE_VILLAGE);
        }
        executionHandler.disableMethodDelegation(PlayerController.class.getDeclaredMethod("canBuildVillage"));
        assertCallEquals(jsonParams.getBoolean("expected"), playerController::canBuildVillage, context, result ->
            "PlayerController.canBuildVillage did not return the expected value");
    }

    @ParameterizedTest
    @JsonParameterSetTest("/controller/PlayerController/canBuildRoad.json")
    public void testCanBuildRoad(JsonParameterSet jsonParams) throws NoSuchMethodException {
        player.addResources(jsonParams.getBoolean("hasResources") ? Config.ROAD_BUILDING_COST : Collections.emptyMap());
        executionHandler.substituteMethod(PlayerImpl.class.getDeclaredMethod("getRemainingRoads"),
            invocation -> jsonParams.getInt("remainingRoads"));

        boolean objectiveSet = jsonParams.getBoolean("objectiveSet");
        Context context = contextBuilder()
            .add(baseContext)
            .add("objective set to PLACE_ROAD", objectiveSet)
            .add("remaining roads", jsonParams.getInt("remainingRoads"))
            .build();
        if (objectiveSet) {
            playerController.setPlayerObjective(PlayerObjective.PLACE_ROAD);
        }
        executionHandler.disableMethodDelegation(PlayerController.class.getDeclaredMethod("canBuildRoad"));
        assertCallEquals(jsonParams.getBoolean("expected"), playerController::canBuildRoad, context, result ->
            "PlayerController.canBuildRoad did not return the expected value");
    }

    @ParameterizedTest
    @JsonParameterSetTest("/controller/PlayerController/buildVillage.json")
    public void testBuildVillage(JsonParameterSet jsonParams) throws NoSuchMethodException {
        boolean firstRound = jsonParams.getBoolean("firstRound");
        boolean canBuildVillage = jsonParams.getBoolean("canBuildVillage");
        player.addResources(canBuildVillage ? Config.SETTLEMENT_BUILDING_COST.get(Settlement.Type.VILLAGE) : Collections.emptyMap());
        AtomicBoolean calledRemoveResources = new AtomicBoolean();
        executionHandler.substituteMethod(PlayerImpl.class.getDeclaredMethod("removeResource", ResourceType.class, int.class),
            invocation -> {
                calledRemoveResources.set(true);
                return true;
            });
        executionHandler.substituteMethod(PlayerImpl.class.getDeclaredMethod("removeResources", Map.class),
            invocation -> {
                calledRemoveResources.set(true);
                return true;
            });
        executionHandler.substituteMethod(PlayerController.class.getDeclaredMethod("canBuildVillage"),
            invocation -> canBuildVillage);

        AtomicBoolean calledPlaceVillage = new AtomicBoolean();
        executionHandler.substituteMethod(IntersectionImpl.class.getDeclaredMethod("placeVillage", Player.class, boolean.class),
            invocation -> {
                calledPlaceVillage.set(true);
                return !jsonParams.getBoolean("exception");
            });
        Intersection intersection = hexGrid.getIntersectionAt(new TilePosition(0, 0), new TilePosition(0, 1), new TilePosition(1, 0));

        gameController.getRoundCounterProperty().set(firstRound ? 0 : 1);
        playerController.setPlayerObjective(firstRound ? PlayerObjective.PLACE_VILLAGE : PlayerObjective.IDLE);

        Context context = contextBuilder()
            .add(baseContext)
            .add("first round", firstRound)
            .add("canBuildVillage", jsonParams.getBoolean("canBuildVillage"))
            .build();
        if (jsonParams.getBoolean("exception")) {
            assertThrows(IllegalActionException.class, () -> playerController.buildVillage(intersection), context, result ->
                "Expected PlayerController.buildVillage to throw an IllegalActionException");
            assertFalse(calledPlaceVillage.get(), context, result ->
                "PlayerController.buildVillage called Intersection.placeVillage on the given intersection");
            assertFalse(calledRemoveResources.get(), context, result ->
                "PlayerController.buildVillage called Player.removeResource(s) on the current player");
        } else {
            call(() -> playerController.buildVillage(intersection), context, result ->
                "PlayerController.buildVillage threw an uncaught exception");
            assertTrue(calledPlaceVillage.get(), context, result ->
                "PlayerController.buildVillage did not call Intersection.placeVillage on the given intersection");
            assertTrue(firstRound || calledRemoveResources.get(), context, result ->
                "PlayerController.buildVillage did not call Player.removeResource(s) on the current player");
        }
    }

    @ParameterizedTest
    @JsonParameterSetTest("/controller/PlayerController/buildRoad.json")
    public void testBuildRoad(JsonParameterSet jsonParams) throws NoSuchMethodException {
        boolean firstRound = jsonParams.getBoolean("firstRound");
        boolean canBuildRoad = jsonParams.getBoolean("canBuildRoad");
        player.addResources(canBuildRoad ? Config.ROAD_BUILDING_COST : Collections.emptyMap());
        AtomicBoolean calledRemoveResources = new AtomicBoolean();
        executionHandler.substituteMethod(PlayerImpl.class.getDeclaredMethod("removeResource", ResourceType.class, int.class),
            invocation -> {
                calledRemoveResources.set(true);
                return true;
            });
        executionHandler.substituteMethod(PlayerImpl.class.getDeclaredMethod("removeResources", Map.class),
            invocation -> {
                calledRemoveResources.set(true);
                return true;
            });
        executionHandler.substituteMethod(PlayerController.class.getDeclaredMethod("canBuildRoad"),
            invocation -> canBuildRoad);

        AtomicBoolean calledAddRoad = new AtomicBoolean();
        executionHandler.substituteMethod(HexGridImpl.class.getDeclaredMethod("addRoad", TilePosition.class, TilePosition.class, Player.class, boolean.class),
            invocation -> {
                calledAddRoad.set(true);
                return !jsonParams.getBoolean("exception");
            });

        TilePosition tilePosition1 = new TilePosition(0, 0);
        TilePosition tilePosition2 = new TilePosition(0, 1);

        gameController.getRoundCounterProperty().set(firstRound ? 0 : 1);
        playerController.setPlayerObjective(firstRound ? PlayerObjective.PLACE_ROAD : PlayerObjective.IDLE);

        Context context = contextBuilder()
            .add(baseContext)
            .add("first round", firstRound)
            .add("canBuildRoad", jsonParams.getBoolean("canBuildRoad"))
            .build();
        if (jsonParams.getBoolean("exception")) {
            assertThrows(IllegalActionException.class, () -> playerController.buildRoad(tilePosition1, tilePosition2), context, result ->
                "Expected PlayerController.buildRoad to throw an IllegalActionException");
            assertFalse(calledAddRoad.get(), context, result ->
                "PlayerController.buildRoad called HexGrid.addRoad on the given intersection");
            assertFalse(calledRemoveResources.get(), context, result ->
                "PlayerController.buildRoad called Player.removeResource(s) on the current player");
        } else {
            call(() -> playerController.buildRoad(tilePosition1, tilePosition2), context, result ->
                "PlayerController.buildRoad threw an uncaught exception");
            assertTrue(calledAddRoad.get(), context, result ->
                "PlayerController.buildRoad did not call HexGrid.addRoad on the given intersection");
            assertTrue(firstRound || calledRemoveResources.get(), context, result ->
                "PlayerController.buildRoad did not call Player.removeResource(s) on the current player");
        }
    }

    @ParameterizedTest
    @JsonParameterSetTest("/controller/PlayerController/upgradeVillage.json")
    public void testUpgradeVillage(JsonParameterSet jsonParams) throws NoSuchMethodException {
        boolean canUpgradeVillage = jsonParams.getBoolean("canUpgradeVillage");
        player.addResources(canUpgradeVillage ? Config.SETTLEMENT_BUILDING_COST.get(Settlement.Type.CITY) : Collections.emptyMap());
        AtomicBoolean calledRemoveResources = new AtomicBoolean();
        executionHandler.substituteMethod(PlayerImpl.class.getDeclaredMethod("removeResource", ResourceType.class, int.class),
            invocation -> {
                calledRemoveResources.set(true);
                return true;
            });
        executionHandler.substituteMethod(PlayerImpl.class.getDeclaredMethod("removeResources", Map.class),
            invocation -> {
                calledRemoveResources.set(true);
                return true;
            });
        executionHandler.substituteMethod(PlayerController.class.getDeclaredMethod("canUpgradeVillage"),
            invocation -> canUpgradeVillage);

        AtomicBoolean calledUpgradeVillage = new AtomicBoolean();
        executionHandler.substituteMethod(IntersectionImpl.class.getDeclaredMethod("upgradeSettlement", Player.class),
            invocation -> {
                calledUpgradeVillage.set(true);
                return !jsonParams.getBoolean("exception");
            });
        Intersection intersection = hexGrid.getIntersectionAt(new TilePosition(0, 0), new TilePosition(0, 1), new TilePosition(1, 0));

        Context context = contextBuilder()
            .add(baseContext)
            .add("canUpgradeVillage", jsonParams.getBoolean("canUpgradeVillage"))
            .build();
        if (jsonParams.getBoolean("exception")) {
            assertThrows(IllegalActionException.class, () -> playerController.upgradeVillage(intersection), context, result ->
                "Expected PlayerController.upgradeVillage to throw an IllegalActionException");
            assertFalse(!jsonParams.getBoolean("canUpgradeVillage") && calledUpgradeVillage.get(), context, result ->
                "PlayerController.upgradeVillage called Intersection.upgradeSettlement on the given intersection");
            assertFalse(calledRemoveResources.get(), context, result ->
                "PlayerController.upgradeVillage called Player.removeResource(s) on the current player");
        } else {
            call(() -> playerController.upgradeVillage(intersection), context, result ->
                "PlayerController.upgradeVillage threw an uncaught exception");
            assertTrue(calledUpgradeVillage.get(), context, result ->
                "PlayerController.upgradeVillage did not call Intersection.upgradeSettlement on the given intersection");
        }
    }
}
