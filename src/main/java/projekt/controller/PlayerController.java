package projekt.controller;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import projekt.Config;
import projekt.model.Intersection;
import projekt.model.Player;
import projekt.model.ResourceType;
import projekt.model.TilePosition;
import projekt.model.buildings.Settlement;
import projekt.model.tiles.Tile;

import java.util.Map;

public class PlayerController {
    /**
     * The currently active {@link Player} instance of this {@link PlayerController}.
     */
    private final Property<Player> activePlayerProperty;

    private final GameController gameController;

    private Runnable callback;

    public static enum PlayerObjective {
        DROP_HALF_CARDS,
        SELECT_CARD_TO_STEAL,
        SELECT_ROBBER_TILE,
        REGULAR_TURN,
    }

    private final Property<PlayerObjective> playerObjectiveProperty;

    /**
     * Creates a new {@link PlayerController} with the given {@link GameController}.
     *
     * @param gameController the {@link GameController} to use.
     */
    public PlayerController(final GameController gameController) {
        this.gameController = gameController;
        this.activePlayerProperty = new SimpleObjectProperty<>();
        this.playerObjectiveProperty = new SimpleObjectProperty<>();
    }

    /**
     * Returns the currently active {@link Player} instance of this {@link PlayerController}.
     *
     * @return the currently active {@link Player} instance of this {@link PlayerController}.
     */
    public Player getActivePlayer() {
        return activePlayerProperty.getValue();
    }

    /**
     * Returns a {@link Property} that represents the currently active {@link Player} instance of this {@link PlayerController}.
     *
     * @return a {@link Property} that represents the currently active {@link Player} instance of this {@link PlayerController}.
     */
    public Property<Player> getActivePlayerProperty() {
        return activePlayerProperty;
    }

    /**
     * Sets the currently active {@link Player} instance of this {@link PlayerController}.
     *
     * @param activePlayer the currently active {@link Player} instance of this {@link PlayerController}.
     */
    public void setActivePlayer(final Player activePlayer) {
        this.activePlayerProperty.setValue(activePlayer);
    }

    /**
     * Returns the {@link GameController} instance of this {@link PlayerController}.
     *
     * @return the {@link GameController} instance of this {@link PlayerController}.
     */
    public GameController getGameController() {
        return gameController;
    }

    public Runnable getCallback() {
        return callback;
    }

    public void setCallback(final Runnable callback) {
        this.callback = callback;
    }

    public Property<PlayerObjective> getPlayerObjectiveProperty() {
        return playerObjectiveProperty;
    }

    public void setPlayerObjective(final PlayerObjective playerObjectiveProperty) {
        this.playerObjectiveProperty.setValue(playerObjectiveProperty);
    }

    /**
     * Ends the turn of the current {@link Player}.
     */
    public void endTurn() {
        callback.run();
    }

    // -- Building methods --

    public boolean buildVillage(final Intersection intersection) {
        final var player = getActivePlayer();
        final var requiredResources = Config.SETTLEMENT_BUILDING_COST.get(Settlement.Type.VILLAGE);
        if (!player.hasResources(requiredResources)) {
            return false;
        }
        if (!intersection.placeVillage(player)) {
            return false;
        }
        return player.removeResources(requiredResources);
    }

    public boolean upgradeVillage(final Intersection intersection) {
        final var player = getActivePlayer();
        final var requiredResources = Config.SETTLEMENT_BUILDING_COST.get(Settlement.Type.CITY);
        if (!player.hasResources(requiredResources)) {
            return false;
        }
        if (!intersection.upgradeSettlement(player)) {
            return false;
        }
        return player.removeResources(requiredResources);
    }

    public boolean buildRoad(final Tile tile, final TilePosition.EdgeDirection edgeDirection) {
        final var player = getActivePlayer();
        final var requiredResources = Config.ROAD_BUILDING_COST;
        if (!player.hasResources(requiredResources)) {
            return false;
        }
        if (!tile.addRoad(edgeDirection, player)) {
            return false;
        }
        return player.removeResources(requiredResources);
    }

    // -- Trading methods --

    /**
     * Trades the given resources with the bank.
     *
     * @param offerType   the type of resource to offer
     * @param offerAmount the amount of resources to offer
     * @param request     the type of resource to request
     * @return whether the trade was successful
     */
    public boolean tradeWithBank(final ResourceType offerType, final int offerAmount, final ResourceType request) {
        final var player = getActivePlayer();
        // check for port
        final var ratio = player.getTradeRatio(offerType);
        if (offerAmount != ratio) {
            return false;
        }
        if (player.getResources().get(offerType) < offerAmount) {
            return false;
        }
        return player.removeResource(offerType, offerAmount);
    }

    /**
     * Trades the given resources with the given player. Does not check for consent of the other player.
     *
     * @param otherPlayer the player to trade with
     * @param offer       the resources to offer
     * @param request     the resources to request
     * @return whether the trade was successful
     */
    public boolean tradeWithPlayer(final Player otherPlayer, final Map<ResourceType, Integer> offer, final Map<ResourceType, Integer> request) {
        final var player = getActivePlayer();
        if (!player.hasResources(offer)) {
            return false;
        }
        if (!otherPlayer.hasResources(request)) {
            return false;
        }
        // remove resources from players
        player.removeResources(offer);
        otherPlayer.removeResources(request);
        // add resources to players
        for (final var entry : offer.entrySet()) {
            otherPlayer.addResource(entry.getKey(), entry.getValue());
        }
        for (final var entry : request.entrySet()) {
            player.addResource(entry.getKey(), entry.getValue());
        }
        return true;
    }

    /**
     * Selects the resources to drop when a 7 is rolled. Also invokes {@link #endTurn()} to proceed to the next player.
     *
     * @param resourcesToDrop the resources to drop
     */
    public void selectResourcesToDrop(final Map<ResourceType, Integer> resourcesToDrop) {
        final var player = getActivePlayer();
        if (!player.hasResources(resourcesToDrop)) {
            return;
        }
        // check if half of the cards are selected
        final var playerResourceAmount = player.getResources().values().stream().mapToInt(Integer::intValue).sum();
        final var resourcesToDropAmount = resourcesToDrop.values().stream().mapToInt(Integer::intValue).sum();
        if (resourcesToDropAmount != playerResourceAmount / 2) {
            return;
        }
        // remove resources from player
        player.removeResources(resourcesToDrop);
        endTurn();
    }
}
