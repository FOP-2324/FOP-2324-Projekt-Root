package projekt.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.opentest4j.AssertionFailedError;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSet;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSetTest;
import projekt.Config;
import projekt.SubmissionExecutionHandler;
import projekt.controller.actions.AcceptTradeAction;
import projekt.controller.actions.EndTurnAction;
import projekt.controller.actions.IllegalActionException;
import projekt.controller.actions.PlayerAction;
import projekt.model.*;
import projekt.model.tiles.Tile;
import projekt.util.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.*;
import static projekt.controller.PlayerObjective.*;

@TestForSubmission
public class GameControllerTest {

    private final SubmissionExecutionHandler executionHandler = SubmissionExecutionHandler.getInstance();
    private final HexGrid hexGrid = new HexGridImpl(Config.GRID_RADIUS, () -> 6, () -> Tile.Type.WOODLAND);
    private final List<Player> players = IntStream.range(0, Config.MAX_PLAYERS)
        .mapToObj(i -> new PlayerImpl.Builder(i).build(hexGrid))
        .toList();
    private final GameController gameController = new GameController(new GameState(hexGrid, players));
    private final AtomicReference<PlayerAction> playerAction = new AtomicReference<>(playerController -> {});
    private Map<Player, List<PlayerObjective>> playerObjectives;
    private Map<Player, PlayerController> playerControllers;
    private final Context baseContext = contextBuilder().add("players", players).build();

    @BeforeEach
    public void setup() throws ReflectiveOperationException {
        playerObjectives = players.stream()
            .collect(Collectors.toMap(Function.identity(), player -> new ArrayList<>()));
        playerControllers = players.stream()
            .collect(Collectors.toMap(Function.identity(), player -> new PlayerController(gameController, player) {{
                getPlayerObjectiveProperty().addListener((observable, oldValue, newValue) -> playerObjectives.get(getPlayer()).add(newValue));
            }}));
        Field playerControllersField = GameController.class.getDeclaredField("playerControllers");
        playerControllersField.trySetAccessible();
        playerControllersField.set(gameController, playerControllers);

        executionHandler.substituteMethod(PlayerController.class.getDeclaredMethod("blockingGetNextAction"),
            invocation -> playerAction.get());
        executionHandler.substituteMethod(PlayerController.class.getDeclaredMethod("waitForNextAction", PlayerObjective.class),
            invocation -> {
                PlayerController instance = (PlayerController) invocation.getInstance();
                instance.setPlayerObjective(invocation.getParameter(0, PlayerObjective.class));
                return instance.waitForNextAction();
            });
        executionHandler.substituteMethod(PlayerController.class.getDeclaredMethod("waitForNextAction"),
            invocation -> {
                try {
                    return ((PlayerController) invocation.getInstance()).blockingGetNextAction();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    @AfterEach
    public void reset() {
        executionHandler.resetMethodInvocationLogging();
        executionHandler.resetMethodDelegation();
        executionHandler.resetMethodSubstitution();
    }

    @Test
    public void testFirstRound() throws ReflectiveOperationException {
        Method firstRoundMethod = GameController.class.getDeclaredMethod("firstRound");
        firstRoundMethod.trySetAccessible();

        executionHandler.disableMethodDelegation(firstRoundMethod);
        call(() -> firstRoundMethod.invoke(gameController), baseContext, result ->
            "An exception occurred while invoking GameController.firstRound");
        playerObjectives.forEach((player, objectives) -> {
            Context context = contextBuilder()
                .add(baseContext)
                .add("active player", player)
                .build();

            try {
                assertEquals(List.of(PLACE_VILLAGE, PLACE_ROAD, PLACE_VILLAGE, PLACE_ROAD, IDLE), objectives, context, result ->
                    "Actual objectives do not match the expected ones");
            } catch (AssertionFailedError e) {
                assertEquals(List.of(PLACE_VILLAGE, PLACE_ROAD, IDLE, PLACE_VILLAGE, PLACE_ROAD, IDLE), objectives, context, result ->
                    "Actual objectives do not match the expected ones");
            }
        });
    }

    @Test
    public void testRegularTurn() throws ReflectiveOperationException {
        Player activePlayer = players.get(0);
        AtomicInteger counter = new AtomicInteger();
        executionHandler.substituteMethod(PlayerController.class.getDeclaredMethod("blockingGetNextAction"),
            invocation -> {
                if (counter.incrementAndGet() > 3) {
                    playerAction.set(new EndTurnAction());
                }
                return playerAction.get();
            });
        gameController.getActivePlayerControllerProperty().setValue(playerControllers.get(activePlayer));
        Method regularTurnMethod = GameController.class.getDeclaredMethod("regularTurn");
        regularTurnMethod.trySetAccessible();
        executionHandler.disableMethodDelegation(regularTurnMethod);

        Context context = contextBuilder()
            .add(baseContext)
            .add("active player", activePlayer)
            .build();
        call(() -> regularTurnMethod.invoke(gameController), context, result ->
            "An uncaught exception was thrown by GameController.regularTurn");
        assertEquals(List.of(REGULAR_TURN), playerObjectives.get(activePlayer), context, result ->
            "Actual objectives do not match the expected ones");
    }

    @Test
    public void testDiceRollSeven() throws ReflectiveOperationException {
        Method diceRollSevenMethod = GameController.class.getDeclaredMethod("diceRollSeven");
        diceRollSevenMethod.trySetAccessible();
        Field cardsToSelectField = PlayerController.class.getDeclaredField("cardsToSelect");
        cardsToSelectField.trySetAccessible();

        Player rollingPlayer = players.get(0);
        Player dropCardsPlayer = players.get(1);
        Field resourcesField = PlayerImpl.class.getDeclaredField("resources");
        resourcesField.trySetAccessible();
        resourcesField.set(dropCardsPlayer, new HashMap<>() {{put(ResourceType.WOOD, 15);}});
        gameController.getActivePlayerControllerProperty().setValue(playerControllers.get(rollingPlayer));

        executionHandler.disableMethodDelegation(diceRollSevenMethod);
        call(() -> diceRollSevenMethod.invoke(gameController), baseContext, result ->
            "An exception occurred while invoking GameController.diceRollSeven");

        int cardsToSelect = (int) cardsToSelectField.get(playerControllers.get(dropCardsPlayer));
        playerObjectives.forEach((player, objectives) -> {
            Context context = contextBuilder()
                .add(baseContext)
                .add("active player", player)
                .build();

            if (player == rollingPlayer) {
                assertEquals(List.of(SELECT_ROBBER_TILE, SELECT_CARD_TO_STEAL), objectives, context, result ->
                    "Actual objectives do not match the expected ones");
            } else if (player == dropCardsPlayer) {
                assertEquals(7, cardsToSelect, context, result ->
                    "The amount of cards to select differs from the expected amount");
                assertEquals(List.of(DROP_CARDS, IDLE), objectives, context, result ->
                    "Actual objectives do not match the expected ones");
            } else {
                assertEquals(Collections.emptyList(), objectives, context, result ->
                    "Actual objectives do not match the expected ones");
            }
        });
    }

    @Test
    public void testDistributeResources() throws ReflectiveOperationException {
        Field intersectionsField = HexGridImpl.class.getDeclaredField("intersections");
        intersectionsField.trySetAccessible();
        Player activePlayer = players.get(0);
        List<TilePosition> tilePositions = List.of(new TilePosition(0, 0), new TilePosition(0, 1), new TilePosition(-1, 1));

        hexGrid.getIntersectionAt(tilePositions.get(0), tilePositions.get(1), tilePositions.get(2))
            .placeVillage(activePlayer, true);

        TilePosition robberPosition = new TilePosition(1, 2);
        int diceRoll = 6;
        Context context = contextBuilder()
            .add(baseContext)
            .add("settlements", activePlayer.getSettlements())
            .add("robber position", robberPosition)
            .add("diceRoll", diceRoll)
            .build();

        hexGrid.setRobberPosition(robberPosition);
        executionHandler.disableMethodDelegation(GameController.class.getDeclaredMethod("distributeResources", int.class));
        call(() -> gameController.distributeResources(diceRoll), context, result ->
            "An exception occurred while invoking GameController.distributeResources");
        assertEquals(Map.of(ResourceType.WOOD, 3), activePlayer.getResources(), context, result ->
            "The added resources do not match the expected ones");
    }

    @ParameterizedTest
    @JsonParameterSetTest("/controller/GameController/offerTrade.json")
    public void testOfferTrade(JsonParameterSet jsonParams) throws ReflectiveOperationException {
        Player offeringPlayer = players.get(0);
        Integer acceptingPlayerIndex = jsonParams.get("acceptingPlayerIndex");
        Player acceptingPlayer = (acceptingPlayerIndex != null ? players.get(acceptingPlayerIndex) : null);
        Map<ResourceType, Integer> offer = Collections.unmodifiableMap(Utils.deserializeEnumMap(jsonParams.get("offer"),
            ResourceType.class,
            Utils.AS_INTEGER));
        offeringPlayer.addResources(offer);
        Map<ResourceType, Integer> request = Collections.unmodifiableMap(Utils.deserializeEnumMap(jsonParams.get("request"),
            ResourceType.class,
            Utils.AS_INTEGER));
        if (acceptingPlayer != null) {
            acceptingPlayer.addResources(request);
        }

        executionHandler.substituteMethod(PlayerController.class.getDeclaredMethod("blockingGetNextAction"),
            invocation -> {
                PlayerController playerController = ((PlayerController) invocation.getInstance());
                Player player = playerController.getPlayer();
                if (player != acceptingPlayer) {
                    return playerAction.get();
                } else {
                    PlayerAction action = new AcceptTradeAction(true);
                    try {
                        action.execute(playerController);
                    } catch (IllegalActionException e) {
                        throw new RuntimeException(e);
                    }
                    return action;
                }
            });

        Context baseContext = contextBuilder()
            .add("offering Player", offeringPlayer)
            .add("accepting Player", acceptingPlayer)
            .add("offer", offer)
            .add("request", request)
            .build();
        executionHandler.disableMethodDelegation(GameController.class.getDeclaredMethod("offerTrade", Player.class, Map.class, Map.class));
        call(() -> gameController.offerTrade(offeringPlayer, offer, request), baseContext, result ->
            "GameController.offerTrade threw an uncaught exception");
        for (Map.Entry<Player, PlayerController> entry : playerControllers.entrySet()) {
            Player player = entry.getKey();
            Context context = contextBuilder()
                .add(baseContext)
                .add("current Player", player)
                .add("current PlayerController", entry.getValue())
                .build();
            if (player == offeringPlayer) {
                assertEquals(List.of(), playerObjectives.get(player), context, result ->
                    "The objectives of the current player do not match the expected ones");
                if (acceptingPlayer != null) {
                    assertEquals(request, player.getResources(), context, result ->
                        "Offering player does not have the expected resources after trading with someone");
                } else {
                    assertEquals(offer, player.getResources(), context, result ->
                        "Offering player does not have the expected resources after trading with no one");
                }
            } else {
                if (player == acceptingPlayer) {
                    assertEquals(offer, player.getResources(), context, result ->
                        "Accepting player does not have the expected resources after trading");
                    assertEquals(List.of(ACCEPT_TRADE, IDLE), playerObjectives.get(player), context, result ->
                        "The objectives of the current player do not match the expected ones");
                } else {
                    assertEquals(Collections.emptyMap(), player.getResources(), context, result ->
                        "Non-accepting player does not have the expected resources after trading");
                    assertEquals(List.of(), playerObjectives.get(player), context, result ->
                        "The objectives of the current player do not match the expected ones");
                }
            }
        }
    }
}
