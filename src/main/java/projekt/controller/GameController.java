package projekt.controller;

import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;
import projekt.Config;
import projekt.model.GameState;
import projekt.model.HexGridImpl;
import projekt.model.Player;

import java.util.*;
import java.util.stream.Collectors;

public class GameController {

    private static GameController INSTANCE;
    private final GameState state;
    private final PlayerController playerController;
    private final Iterator<Integer> dice;

    public GameController(final GameState state, final PlayerController playerController,
            final Iterator<Integer> dice) {
        this.state = state;
        this.playerController = playerController;
        this.dice = dice;
    }

    public GameController(final GameState state, final Iterator<Integer> dice) {
        this.state = state;
        this.playerController = new PlayerController(this);
        this.dice = dice;
    }

    public GameController() {
        this.state = new GameState(new HexGridImpl(Config.GRID_RADIUS), new ArrayList<>());
        this.playerController = new PlayerController(this);
        this.dice = Config.RANDOM
            .ints(
                1,
                2 * Config.DICE_SIDES * Config.NUMBER_OF_DICE + 1
            )
            .iterator();
    }

    public GameState getState() {
        return state;
    }

    public PlayerController getPlayerController() {
        return playerController;
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
        final var index = state.getPlayers().indexOf(playerController.getActivePlayer());
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
        playerController.setActivePlayer(newActivePlayer);
        playerController.setCallback(this::nextPlayer);
        playerController.setPlayerObjective(PlayerController.PlayerObjective.REGULAR_TURN);
    }

    public void startGame() {
        if (this.state.getPlayers().size() < Config.MIN_PLAYERS) {
            throw new IllegalStateException("Not enough players");
        }
        this.playerController.setActivePlayer(this.state.getPlayers().get(0));
        nextPlayer();
    }

    private void diceRollSeven(final Player activePlayer) {
        diceRollSeven(activePlayer, this.getState().getPlayers().iterator());
    }

    private void diceRollSeven(final Player activePlayer, final Iterator<Player> remainingPlayers) {
        if (!remainingPlayers.hasNext()) {
            playerController.setActivePlayer(activePlayer);
            playerController.setPlayerObjective(PlayerController.PlayerObjective.SELECT_ROBBER_TILE);
            playerController.setCallback(() -> {
                playerController.setPlayerObjective(PlayerController.PlayerObjective.SELECT_CARD_TO_STEAL);
                playerController.setCallback(() -> {
                    playerController.setCallback(this::nextPlayer);
                    playerController.setPlayerObjective(PlayerController.PlayerObjective.REGULAR_TURN);
                });
            });
            return;
        }
        final var player = remainingPlayers.next();
        if (player.getResources().values().stream().mapToInt(Integer::intValue).sum() > 7) {
            playerController.setActivePlayer(player);
            playerController.setPlayerObjective(PlayerController.PlayerObjective.DROP_HALF_CARDS);
            playerController.setCallback(() -> diceRollSeven(activePlayer, remainingPlayers));
        } else {
            diceRollSeven(activePlayer, remainingPlayers);
        }
    }

    @StudentImplementationRequired
    public void distributeResources(final int diceRoll) {
        for (final var tile : state.getGrid().getTiles(diceRoll)) {
            for (final var intersection : tile.getIntersections()) {
                Optional.ofNullable(intersection.getSettlement()).ifPresent(
                    settlement -> settlement.owner().addResource(tile.getType().resourceType, 1)
                );
            }
        }
    }
}
