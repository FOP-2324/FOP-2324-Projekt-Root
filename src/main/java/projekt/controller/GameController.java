package projekt.controller;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.tudalgo.algoutils.student.annotation.DoNotTouch;
import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import projekt.Config;
import projekt.controller.actions.AcceptTradeAction;
import projekt.controller.actions.EndTurnAction;
import projekt.controller.actions.PlayerAction;
import projekt.model.*;
import projekt.model.tiles.Tile;

public class GameController {

    private final GameState state;
    private final Map<Player, PlayerController> playerControllers;
    private final Iterator<Integer> dice;
    private final IntegerProperty currentDiceRoll = new SimpleIntegerProperty(0);
    private final Stack<DevelopmentCardType> availableDevelopmentCards = Config.generateDevelopmentCards();

    private final Property<PlayerController> activePlayerControllerProperty = new SimpleObjectProperty<>();

    public GameController(
            final GameState state,
            final Map<Player, PlayerController> playerControllers,
            final Iterator<Integer> dice) {
        this.state = state;
        this.playerControllers = playerControllers;
        this.dice = dice;
    }

    public GameController(final GameState state) {
        this(state, Config.RANDOM.ints(
                1,
                Config.DICE_SIDES * Config.NUMBER_OF_DICE + 1).iterator());
    }

    public GameController(final GameState state, final Iterator<Integer> dice) {
        this.state = state;
        this.dice = dice;
        this.playerControllers = new HashMap<>();
    }

    public GameController() {
        this(new GameState(new HexGridImpl(Config.GRID_RADIUS), new ArrayList<>()));
    }

    public void initPlayerControllers() {
        for (final Player player : state.getPlayers()) {
            playerControllers.put(player, new PlayerController(this, player));
        }
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

    public IntegerProperty getCurrentDiceRollProperty() {
        return currentDiceRoll;
    }

    private void setActivePlayerControllerProperty(final Player activePlayer) {
        this.activePlayerControllerProperty.setValue(playerControllers.get(activePlayer));
    }

    public int castDice() {
        currentDiceRoll.set(dice.next());
        return currentDiceRoll.get();
    }

    public int remainingDevelopmentCards() {
        return availableDevelopmentCards.size();
    }

    public DevelopmentCardType drawDevelopmentCard() {
        return availableDevelopmentCards.pop();
    }

    @StudentImplementationRequired
    public Set<Player> getWinners() {
        return getState().getPlayers().stream()
            .filter(player -> player.getVictoryPoints() >= 10)
            .collect(Collectors.toUnmodifiableSet());
    }

    @DoNotTouch
    public void startGame() {
        if (this.state.getPlayers().size() < Config.MIN_PLAYERS) {
            throw new IllegalStateException("Not enough players");
        }
        if (playerControllers.isEmpty()) {
            initPlayerControllers();
        }

        firstRound();

        while (getWinners().isEmpty()) {
            for (final PlayerController playerController : playerControllers.values()) {
                withActivePlayer(playerController, () -> {
                    // Dice roll
                    playerController.waitForNextAction(PlayerObjective.DICE_ROLL);
                    final var diceRoll = currentDiceRoll.get();

                    if (diceRoll == 7) {
                        diceRollSeven();
                    } else {
                        distributeResources(diceRoll);
                    }
                    // Regular turn
                    regularTurn();
                });
            }
        }

        // Game End
        getState().setWinner(getWinners().iterator().next());
    }

    @DoNotTouch
    public void withActivePlayer(final PlayerController pc, final Runnable r) {
        activePlayerControllerProperty.setValue(pc);
        r.run();
        pc.setPlayerObjective(PlayerObjective.IDLE);
        activePlayerControllerProperty.setValue(null);
    }

    /**
     * This method assumes active player is already handled externally.
     */
    @StudentImplementationRequired
    private void regularTurn() {
        final var pc = activePlayerControllerProperty.getValue();
        var action = pc.waitForNextAction(PlayerObjective.REGULAR_TURN);
        while (!(action instanceof EndTurnAction)) {
            action = pc.waitForNextAction();
        }
    }

    @StudentImplementationRequired
    private void firstRound() {
        for (final PlayerController playerController : playerControllers.values()) {
            withActivePlayer(playerController, () -> {
                playerController.waitForNextAction(PlayerObjective.PLACE_VILLAGE);
                playerController.waitForNextAction(PlayerObjective.PLACE_ROAD);
                playerController.waitForNextAction(PlayerObjective.PLACE_VILLAGE);
                playerController.waitForNextAction(PlayerObjective.PLACE_ROAD);
            });
        }
    }

    /**
     * Offer the trade to all players that can accept the trade. As soon as one
     * player accepts the trade, the offering player can continue with his round.
     */
    public void offerTrade(final Player offeringPlayer, final Map<ResourceType, Integer> offer,
            final Map<ResourceType, Integer> request) {
        final BooleanProperty tradeAccepted = new SimpleBooleanProperty(true);
        for (final PlayerController playerController : playerControllers.values().stream()
                .filter(pc -> pc.canAcceptTradeOffer(offeringPlayer, request)).collect(Collectors.toList())) {
            playerController.setPlayerTradeOffer(offeringPlayer, offer, request);
            withActivePlayer(playerController, () -> {
                final PlayerAction action = playerController.waitForNextAction(PlayerObjective.ACCEPT_TRADE);
                if (action instanceof final AcceptTradeAction tradeAction) {
                    if (tradeAction.accepted()) {
                        tradeAccepted.set(true);
                    }
                }
            });
            playerController.resetPlayerTradeOffer();
            if (tradeAccepted.get()) {
                break;
            }
        }
        activePlayerControllerProperty.setValue(playerControllers.get(offeringPlayer));
    }

    @StudentImplementationRequired
    private void diceRollSeven() {
        final var origPC = getActivePlayerController();
        for (final PlayerController playerController : playerControllers.values()) {
            withActivePlayer(playerController, () -> {
                if (playerController.getPlayer().getResources().values().stream().mapToInt(Integer::intValue).sum() > 7) {
                    playerController.waitForNextAction(PlayerObjective.DROP_HALF_CARDS);
                }
            });
        }
        activePlayerControllerProperty.setValue(origPC);
        origPC.waitForNextAction(PlayerObjective.SELECT_ROBBER_TILE);
        origPC.waitForNextAction(PlayerObjective.SELECT_CARD_TO_STEAL);
    }

    @StudentImplementationRequired
    public void distributeResources(final int diceRoll) {
        for (final var tile : state.getGrid().getTiles(diceRoll).stream().filter(Predicate.not(Tile::hasRobber))
                .collect(Collectors.toSet())) {
            for (final var intersection : tile.getIntersections()) {
                Optional.ofNullable(intersection.getSettlement()).ifPresent(
                        settlement -> settlement.owner().addResource(
                                tile.getType().resourceType,
                                settlement.type().resourceAmount));
            }
        }
    }
}
