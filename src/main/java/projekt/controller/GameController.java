package projekt.controller;

import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import projekt.Config;
import projekt.model.GameState;
import projekt.model.HexGridImpl;
import projekt.model.Player;

import java.util.*;
import java.util.stream.Collectors;

public class GameController {

    private final GameState state;
    private final Map<Player, PlayerController> playerControllers;
    private final Iterator<Integer> dice;
    private final Property<PlayerController> activePlayerControllerProperty = new SimpleObjectProperty<>();

    public GameController(
            final GameState state,
            final Map<Player, PlayerController> playerControllers,
            final Iterator<Integer> dice) {
        this.state = state;
        this.playerControllers = playerControllers;
        this.dice = dice;
    }

    public GameController(final GameState state, final Iterator<Integer> dice) {
        this.state = state;
        this.dice = dice;
        this.playerControllers = new HashMap<>();
    }

    private void initPlayerControllers(final GameState state) {
        for (final Player player : state.getPlayers()) {
            playerControllers.put(player, new PlayerController(this, player));
        }
    }

    public GameController() {
        this(
                new GameState(new HexGridImpl(Config.GRID_RADIUS), new ArrayList<>()),
                Config.RANDOM.ints(1, 2 * Config.DICE_SIDES * Config.NUMBER_OF_DICE + 1).iterator());
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
        return dice.next();
    }

    @StudentImplementationRequired
    public Set<Player> getWinners() {
        return getState().getPlayers().stream()
                .filter(player -> player.getVictoryPoints() >= 10)
                .collect(Collectors.toUnmodifiableSet());
    }

    public void nextPlayer() {
        // check for winner
        if (!getWinners().isEmpty()) {
            getState().setGameOver(true);
            return;
        }
        // advance to next player
        final var index = state.getPlayers().indexOf(getActivePlayerController().getPlayer());
        final var newActivePlayer = state.getPlayers().get((index + 1) % state.getPlayers().size());
        // roll dice
        final var diceRoll = castDice();
        // special case: 7
        if (diceRoll == 7) {
            diceRollSeven(newActivePlayer);
            return;
        }
        // normal case
        distributeResources(diceRoll);
        setActivePlayerControllerProperty(newActivePlayer);
        getActivePlayerController().setCallback(this::nextPlayer);
        getActivePlayerController().setPlayerObjective(PlayerController.PlayerObjective.REGULAR_TURN);
    }

    public void startGame() {
        if (this.state.getPlayers().size() < Config.MIN_PLAYERS) {
            throw new IllegalStateException("Not enough players");
        }
        initPlayerControllers(state);
        setActivePlayerControllerProperty(this.state.getPlayers().get(0));
        setupRound(getActivePlayerController().getPlayer());
    }

    private void setupRound(final Player activePlayer) {
        setupRound(activePlayer, this.getState().getPlayers().iterator());
    }

    private void setupRound(final Player activePlayer, final Iterator<Player> remainingPlayers) {
        if (!remainingPlayers.hasNext()) {
            nextPlayer();
            return;
        }
        final var player = remainingPlayers.next();
        setActivePlayerControllerProperty(player);
        getActivePlayerController().setPlayerObjective(PlayerController.PlayerObjective.PLACE_TWO_VILLAGES);
        getActivePlayerController().setCallback(() -> {
            getActivePlayerController().setPlayerObjective(PlayerController.PlayerObjective.PLACE_TWO_ROADS);
            getActivePlayerController().setCallback(() -> setupRound(activePlayer, remainingPlayers));
        });
    }

    private void diceRollSeven(final Player activePlayer) {
        diceRollSeven(activePlayer, this.getState().getPlayers().iterator());
    }

    private void diceRollSeven(final Player activePlayer, final Iterator<Player> remainingPlayers) {
        if (!remainingPlayers.hasNext()) {
            setActivePlayerControllerProperty(activePlayer);
            getActivePlayerController().setPlayerObjective(PlayerController.PlayerObjective.SELECT_ROBBER_TILE);
            getActivePlayerController().setCallback(() -> {
                getActivePlayerController().setPlayerObjective(PlayerController.PlayerObjective.SELECT_CARD_TO_STEAL);
                getActivePlayerController().setCallback(() -> {
                    getActivePlayerController().setCallback(this::nextPlayer);
                    getActivePlayerController().setPlayerObjective(PlayerController.PlayerObjective.REGULAR_TURN);
                });
            });
            return;
        }
        final var player = remainingPlayers.next();
        if (player.getResources().values().stream().mapToInt(Integer::intValue).sum() > 7) {
            setActivePlayerControllerProperty(player);
            getActivePlayerController().setPlayerObjective(PlayerController.PlayerObjective.DROP_HALF_CARDS);
            getActivePlayerController().setCallback(() -> diceRollSeven(activePlayer, remainingPlayers));
        } else {
            diceRollSeven(activePlayer, remainingPlayers);
        }
    }

    @StudentImplementationRequired
    public void distributeResources(final int diceRoll) {
        for (final var tile : state.getGrid().getTiles(diceRoll)) {
            for (final var intersection : tile.getIntersections()) {
                Optional.ofNullable(intersection.getSettlement()).ifPresent(
                        settlement -> settlement.owner().addResource(
                                tile.getType().resourceType,
                                settlement.type().resourceAmount));
            }
        }
    }
}
