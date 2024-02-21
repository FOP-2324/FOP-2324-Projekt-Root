package projekt.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import projekt.Config;
import projekt.controller.actions.EndTurnAction;
import projekt.controller.actions.PlayerAction;
import projekt.model.*;
import projekt.util.PlayerMock;

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

    private final HexGrid hexGrid = new HexGridImpl(Config.GRID_RADIUS);
    private final List<Player> players = IntStream.range(0, 3)
        .mapToObj(i -> (Player) new PlayerMock(new PlayerImpl.Builder(i).build(hexGrid)))
        .toList();
    private final GameController gameController = new GameController(new GameState(hexGrid, players));
    private final AtomicReference<PlayerAction> playerAction = new AtomicReference<>(playerController -> {});
    private final AtomicReference<Runnable> blockingAction = new AtomicReference<>(() -> {});
    private Map<Player, List<PlayerObjective>> playerObjectives;
    private Map<Player, PlayerController> playerControllers;
    private final Context baseContext = contextBuilder().add("players", players).build();

    @BeforeEach
    public void setup() throws ReflectiveOperationException {
        playerObjectives = players.stream()
            .collect(Collectors.toMap(Function.identity(), player -> new ArrayList<>()));
        playerControllers = players.stream()
            .collect(Collectors.toMap(Function.identity(), player -> new PlayerController(gameController, player) {
                {
                    getPlayerObjectiveProperty().addListener((observable, oldValue, newValue) -> playerObjectives.get(getPlayer()).add(newValue));
                }

                @Override
                public PlayerAction blockingGetNextAction() {
                    blockingAction.get().run();
                    return playerAction.get();
                }

                @Override
                public PlayerAction waitForNextAction(PlayerObjective nextObjective) {
                    setPlayerObjective(nextObjective);
                    return waitForNextAction();
                }

                @Override
                public PlayerAction waitForNextAction() {
                    blockingAction.get().run();
                    return playerAction.get();
                }
            }));
        Field playerControllersField = GameController.class.getDeclaredField("playerControllers");
        playerControllersField.trySetAccessible();
        playerControllersField.set(gameController, playerControllers);
    }

    @Test
    public void testFirstRound() throws ReflectiveOperationException {
        Method firstRoundMethod = GameController.class.getDeclaredMethod("firstRound");
        firstRoundMethod.trySetAccessible();

        call(() -> firstRoundMethod.invoke(gameController), baseContext, result ->
            "An exception occurred while invoking GameController.firstRound");
        List<PlayerObjective> expected = List.of(PLACE_VILLAGE, PLACE_ROAD, PLACE_VILLAGE, PLACE_ROAD, IDLE);
        playerObjectives.forEach((player, objectives) -> {
            Context context = contextBuilder()
                .add(baseContext)
                .add("active player", player)
                .build();

            assertEquals(expected, objectives, context, result ->
                "Actual objectives do not match the expected ones");
        });
    }

    @Test
    public void testRegularTurn() throws ReflectiveOperationException, InterruptedException {
        Player activePlayer = players.get(0);
        PlayerController activePlayerController = playerControllers.get(activePlayer);
        AtomicInteger counter = new AtomicInteger();
        blockingAction.set(() -> {
            if (counter.incrementAndGet() > 3) {
                playerAction.set(new EndTurnAction());
            }
        });
        gameController.getActivePlayerControllerProperty().setValue(activePlayerController);
        Method regularTurnMethod = GameController.class.getDeclaredMethod("regularTurn");
        regularTurnMethod.trySetAccessible();

        Context context = contextBuilder()
            .add(baseContext)
            .add("active player", activePlayer)
            .build();
        Thread thread = new Thread(() -> {
            try {
                regularTurnMethod.invoke(gameController);
            } catch (Throwable t) {
                fail(t, baseContext, result -> "An uncaught exception was thrown by GameController.regularTurn");
            }
        });
        thread.start();
        thread.join(3000);
        if (thread.isAlive()) {
            thread.stop();
            fail(baseContext, result -> "Timeout of 3 seconds exceeded in GameController.regularTurn");
        }

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
        PlayerMock dropCardsPlayer = (PlayerMock) players.get(1);
        Field resourcesField = PlayerImpl.class.getDeclaredField("resources");
        resourcesField.trySetAccessible();
        resourcesField.set(dropCardsPlayer.getDelegate(), new HashMap<>() {{put(ResourceType.WOOD, 15);}});
        gameController.getActivePlayerControllerProperty().setValue(playerControllers.get(rollingPlayer));

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
}
