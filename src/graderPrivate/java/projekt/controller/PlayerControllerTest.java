package projekt.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSet;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSetTest;
import projekt.Config;
import projekt.controller.actions.IllegalActionException;
import projekt.model.*;
import projekt.util.PlayerControllerMock;
import projekt.util.PlayerMock;
import projekt.util.Utils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.*;

@TestForSubmission
public class PlayerControllerTest {

    private final HexGrid hexGrid = new HexGridImpl(Config.GRID_RADIUS);
    private final List<Player> players = IntStream.range(0, Config.MAX_PLAYERS)
        .mapToObj(i -> new PlayerMock(new PlayerImpl.Builder(i).build(hexGrid)))
        .collect(Collectors.toList());
    private GameController gameController;

    @BeforeEach
    public void setup() {
        GameState gameState = new GameState(hexGrid, players);
        gameController = new GameController(gameState);
    }

    @ParameterizedTest
    @JsonParameterSetTest("/controller/PlayerController/acceptTradeOffer.json")
    @SuppressWarnings("unchecked")
    public void testAcceptTradeOffer(JsonParameterSet jsonParams) throws ReflectiveOperationException {
        Field tradingPlayerField = PlayerController.class.getDeclaredField("tradingPlayer");
        Field playerTradingOfferField = PlayerController.class.getDeclaredField("playerTradingOffer");
        Field playerTradingRequestField = PlayerController.class.getDeclaredField("playerTradingRequest");
        tradingPlayerField.trySetAccessible();
        playerTradingOfferField.trySetAccessible();
        playerTradingRequestField.trySetAccessible();

        PlayerMock player = (PlayerMock) players.get(0);
        PlayerMock tradingPlayer = jsonParams.getBoolean("tradingPlayerSet") ? (PlayerMock) players.get(1) : null;
        Map<ResourceType, Integer> playerTradingOffer = Utils.deserializeEnumMap(jsonParams.get("playerTradingOffer"), ResourceType.class, Utils.AS_INTEGER);
        Map<ResourceType, Integer> playerTradingRequest = Utils.deserializeEnumMap(jsonParams.get("playerTradingRequest"), ResourceType.class, Utils.AS_INTEGER);

        Map<ResourceType, Integer> playerInventory = new HashMap<>(playerTradingRequest != null ? playerTradingRequest : Collections.emptyMap());
        player.setUseDelegate("addResource", "addResources", "hasResources", "removeResource", "removeResources");
        player.setMethodAction((methodName, params) -> switch (methodName) {
            case "addResource" -> {
                ResourceType key = (ResourceType) params[1];
                Integer value = (Integer) params[2];
                playerInventory.merge(key, value, Integer::sum);
                yield null;
            }
            case "addResources" -> {
                ((Map<ResourceType, Integer>) params[1]).forEach(((Player) params[0])::addResource);
                yield null;
            }
            case "hasResources" -> playerTradingRequest.equals(params[1]);
            case "removeResource" -> {
                ResourceType key = (ResourceType) params[1];
                Integer value = (Integer) params[2];
                playerInventory.merge(key, -value, Integer::sum);
                yield true;
            }
            case "removeResources" -> ((Map<ResourceType, Integer>) params[1]).entrySet()
                .stream()
                .map(entry -> ((Player) params[0]).removeResource(entry.getKey(), entry.getValue()))
                .reduce((a, b) -> a && b);
            default -> null;
        });
        PlayerControllerMock playerController = new PlayerControllerMock(gameController, player,
            Predicate.not(List.of("blockingGetNextAction", "waitForNextAction")::contains),
            (methodName, params) -> {
                if (params.length == 1 + 1 && params[1] instanceof PlayerObjective playerObjective) {
                    ((PlayerController) params[0]).setPlayerObjective(playerObjective);
                }
                return null;
            });
        boolean accepted = jsonParams.getBoolean("accepted");

        playerTradingOfferField.set(playerController, playerTradingOffer);
        playerTradingRequestField.set(playerController, playerTradingRequest);
        Map<ResourceType, Integer> tradingPlayerInventory = new HashMap<>(playerTradingOffer != null ? playerTradingOffer : Collections.emptyMap());
        if (tradingPlayer != null) {
            tradingPlayer.setUseDelegate("addResource", "addResources", "hasResources", "removeResource", "removeResources");
            tradingPlayer.setMethodAction((methodName, params) -> switch (methodName) {
                case "addResource" -> {
                    ResourceType key = (ResourceType) params[1];
                    Integer value = (Integer) params[2];
                    tradingPlayerInventory.merge(key, value, Integer::sum);
                    yield null;
                }
                case "addResources" -> {
                    ((Map<ResourceType, Integer>) params[1]).forEach(((Player) params[0])::addResource);
                    yield null;
                }
                case "hasResources" -> playerTradingOffer.equals(params[1]);
                case "removeResource" -> {
                    ResourceType key = (ResourceType) params[1];
                    Integer value = (Integer) params[2];
                    tradingPlayerInventory.merge(key, -value, Integer::sum);
                    yield true;
                }
                case "removeResources" -> ((Map<ResourceType, Integer>) params[1]).entrySet()
                    .stream()
                    .map(entry -> ((Player) params[0]).removeResource(entry.getKey(), entry.getValue()))
                    .reduce((a, b) -> a && b);
                default -> null;
            });
        }
        tradingPlayerField.set(playerController, tradingPlayer);

        Context context = contextBuilder()
            .add("player", player)
            .add("tradingPlayer", tradingPlayer)
            .add("playerTradingOffer", playerTradingOffer)
            .add("playerTradingRequest", playerTradingRequest)
            .add("accepted", accepted)
            .build();
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

            }

            assertEquals(PlayerObjective.IDLE, playerController.getPlayerObjectiveProperty().getValue(), context, result ->
                "PlayerController.acceptTradeOffer did not set the player objective to IDLE");
        }
    }
}
