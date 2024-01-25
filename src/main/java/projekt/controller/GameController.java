package projekt.controller;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import projekt.Config;
import projekt.controller.actions.EndTurnAction;
import projekt.model.GameState;
import projekt.model.HexGridImpl;
import projekt.model.Player;

import java.util.*;
import java.util.stream.Collectors;

public class GameController {

    private final GameState state;
    private final Map<Player, PlayerController> playerControllers;
    private final Iterator<Integer> dice;
    private final IntegerProperty currentDiceRoll = new SimpleIntegerProperty(0);

    private final Property<PlayerController> activePlayerControllerProperty = new SimpleObjectProperty<>();

    public GameController(
        final GameState state,
        final Map<Player, PlayerController> playerControllers,
        final Iterator<Integer> dice
    ) {
        this.state = state;
        this.playerControllers = playerControllers;
        this.dice = dice;
    }

    public GameController(final GameState state, final Iterator<Integer> dice) {
        this.state = state;
        this.dice = dice;
        this.playerControllers = new HashMap<>();
        for (final Player player : state.getPlayers()) {
            playerControllers.put(player, new PlayerController(this, player));
        }
    }

    public GameController() {
        this(
            new GameState(new HexGridImpl(Config.GRID_RADIUS), new ArrayList<>()),
            Config.RANDOM.ints(1, 2 * Config.DICE_SIDES * Config.NUMBER_OF_DICE + 1).iterator()
        );
    }

    public GameState getState() {
        return state;
    }

    public Map<Player, PlayerController> getPlayerControllers() {
        return playerControllers;
    }

    public Property<PlayerController> getActivePlayerControllerProperty() {
        return activePlayerControllerProperty;
    }

    public PlayerController getActivePlayerController() {
        return activePlayerControllerProperty.getValue();
    }

    private void setActivePlayerControllerProperty(final Player activePlayer) {
        this.activePlayerControllerProperty.setValue(playerControllers.get(activePlayer));
    }

    public int castDice() {
        currentDiceRoll.set(dice.next());
        return currentDiceRoll.get();
    }

    @StudentImplementationRequired
    public Set<Player> getWinners() {
        return getState().getPlayers().stream()
            .filter(player -> player.getVictoryPoints() >= 10)
            .collect(Collectors.toUnmodifiableSet());
    }

    public void startGame() {
        if (this.state.getPlayers().size() < Config.MIN_PLAYERS) {
            throw new IllegalStateException("Not enough players");
        }
        setActivePlayerControllerProperty(this.state.getPlayers().get(0));
        firstRound(getActivePlayerController().getPlayer());

        while (!getWinners().isEmpty()) {
            for (final PlayerController playerController : playerControllers.values()) {
                activePlayerControllerProperty.setValue(playerController);

                // Dice roll
                playerController.setPlayerObjective(PlayerObjective.DICE_ROLL);
                playerController.waitForNextAction();
                final var diceRoll = currentDiceRoll.get();

                if (diceRoll == 7) {
                    diceRollSeven();
                } else {
                    distributeResources(diceRoll);
                }
                // Regular turn
                playerController.setPlayerObjective(PlayerObjective.REGULAR_TURN);
                var action = playerController.waitForNextAction();
                while (!(action instanceof EndTurnAction)) {
                    action = playerController.waitForNextAction();
                }
                playerController.setPlayerObjective(PlayerObjective.IDLE);
                activePlayerControllerProperty.setValue(null);
            }
        }

        // Game End
        getState().setGameOver(true);
    }

    private void firstRound(final Player activePlayer) {
        for (final PlayerController playerController : playerControllers.values()) {
            playerController.setPlayerObjective(PlayerObjective.PLACE_TWO_VILLAGES);
            playerController.waitForNextAction();
            playerController.setPlayerObjective(PlayerObjective.PLACE_TWO_ROADS);
            playerController.waitForNextAction();
        }
    }

    private void diceRollSeven() {
        final var origPC = getActivePlayerController();
        for (final PlayerController playerController : playerControllers.values()) {
            activePlayerControllerProperty.setValue(playerController);
            if (playerController.getPlayer().getResources().values().stream().mapToInt(Integer::intValue).sum() > 7) {
                playerController.setPlayerObjective(PlayerObjective.DROP_HALF_CARDS);
                playerController.waitForNextAction();
            }
        }
        activePlayerControllerProperty.setValue(origPC);
        origPC.setPlayerObjective(PlayerObjective.SELECT_ROBBER_TILE);
        origPC.waitForNextAction();
        origPC.setPlayerObjective(PlayerObjective.SELECT_CARD_TO_STEAL);
        origPC.waitForNextAction();
    }

    @StudentImplementationRequired
    public void distributeResources(final int diceRoll) {
        for (final var tile : state.getGrid().getTiles(diceRoll)) {
            for (final var intersection : tile.getIntersections()) {
                Optional.ofNullable(intersection.getSettlement()).ifPresent(
                    settlement -> settlement.owner().addResource(
                        tile.getType().resourceType,
                        settlement.type().resourceAmount
                    ));
            }
        }
    }
}
