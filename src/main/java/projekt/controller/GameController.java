package projekt.controller;

import projekt.Config;
import projekt.model.GameState;
import projekt.model.HexGridImpl;
import projekt.model.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

public class GameController {

    private static GameController INSTANCE;
    private final GameState state;
    private Player currentPlayer;
    private final Iterator<Integer> dice;

    public GameController(final GameState state, final Iterator<Integer> dice) {
        this.state = state;
        this.dice = dice;
    }

    public GameController() {
        this.state = new GameState(new HexGridImpl(Config.GRID_RADIUS), new ArrayList<>());
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

    public int castDice() {
        return dice.next();
    }

    public void startGame() {
        if (this.state.getPlayers().size() < Config.MIN_PLAYERS) {
            throw new IllegalStateException("Not enough players");
        }
        this.currentPlayer = state.getPlayers().get(0);
        gameLoop();
    }

    public void nextPlayer() {
        final var index = state.getPlayers().indexOf(currentPlayer);
        currentPlayer = state.getPlayers().get((index + 1) % state.getPlayers().size());
    }

    private void gameLoop() {
        while (!getState().isGameOver()) {
            final var diceRoll = castDice();
            if (diceRoll == 7) {
                diceRollSeven();
            } else {
                resourcePhase(diceRoll);
            }
            buildTradeDevelopmentPhase();
            nextPlayer();
        }
    }

    private void diceRollSeven() {
        // each player with more than 7 cards needs to select half of their cards to discard
        // player can choose robber position
        // player can steal from one of the players with a settlement next to the robber
        throw new UnsupportedOperationException("Problem for later");
    }

    public void resourcePhase(final int diceRoll) {
        for (final var tile : state.getGrid().getTiles(diceRoll)) {
            for (final var intersection : tile.getIntersections()) {
                Optional.ofNullable(intersection.getSettlement()).ifPresent(
                    settlement -> settlement.owner().addResource(tile.getType().resourceType, 1)
                );
            }
        }
    }

    private void buildTradeDevelopmentPhase() {
    }
}
