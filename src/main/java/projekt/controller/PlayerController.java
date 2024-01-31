package projekt.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.tudalgo.algoutils.student.annotation.DoNotTouch;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import projekt.Config;
import projekt.controller.actions.IllegalActionException;
import projekt.controller.actions.PlayerAction;
import projekt.model.DevelopmentCardType;
import projekt.model.Intersection;
import projekt.model.Player;
import projekt.model.PlayerState;
import projekt.model.ResourceType;
import projekt.model.TilePosition;
import projekt.model.TradePayload;
import projekt.model.buildings.Edge;
import projekt.model.buildings.Settlement;
import projekt.model.tiles.Tile;

public class PlayerController {
    private final Player player;

    private final GameController gameController;

    private final BlockingDeque<PlayerAction> actions = new LinkedBlockingDeque<>();

    private final Property<PlayerState> playerStateProperty = new SimpleObjectProperty<>();

    private final Property<PlayerObjective> playerObjectiveProperty = new SimpleObjectProperty<>(PlayerObjective.IDLE);

    private Player tradingPlayer;

    private Map<ResourceType, Integer> playerTradingOffer;

    private Map<ResourceType, Integer> playerTradingRequest;

    private Map<ResourceType, Integer> selectedResources = new HashMap<>();

    private int cardsToSelect = 0;

    /**
     * Creates a new {@link PlayerController} with the given {@link GameController}.
     *
     * @param gameController the {@link GameController} to use.
     */
    public PlayerController(final GameController gameController, final Player player) {
        this.gameController = gameController;
        this.player = player;
        this.playerObjectiveProperty.addListener((observable, oldValue, newValue) -> {
            updatePlayerState();
        });
    }

    /**
     * Returns a {@link Property} that represents the currently active
     * {@link Player} instance of this {@link PlayerController}.
     *
     * @return a {@link Property} that represents the currently active
     * {@link Player} instance of this {@link PlayerController}.
     */
    public Player getPlayer() {
        return player;
    }

    public Property<PlayerState> getPlayerStateProperty() {
        return playerStateProperty;
    }

    public PlayerState getPlayerState() {
        return playerStateProperty.getValue();
    }

    public Property<PlayerObjective> getPlayerObjectiveProperty() {
        return playerObjectiveProperty;
    }

    public void setPlayerObjective(final PlayerObjective nextObjective) {
        playerObjectiveProperty.setValue(nextObjective);
        updatePlayerState();
    }

    @DoNotTouch
    private void updatePlayerState() {
        playerStateProperty
                .setValue(new PlayerState(getBuildableVillageIntersections(), getUpgradeableVillageIntersections(),
                        getBuildableRoadEdges(), getPlayersToStealFrom(), getPlayerTradingPayload(),
                        getCardsToSelect()));
    }

    private int getCardsToSelect() {
        if (PlayerObjective.DROP_HALF_CARDS.equals(playerObjectiveProperty.getValue())) {
            return player.getResources().values().stream().mapToInt(Integer::intValue).sum() / 2;
        }
        if (PlayerObjective.SELECT_CARDS.equals(playerObjectiveProperty.getValue())) {
            return cardsToSelect;
        }
        return 0;
    }

    public List<Player> getOtherPlayers() {
        return gameController.getState()
            .getPlayers()
            .stream()
            .filter(p -> p != player)
            .toList();
    }

    public void rollDice() {
        gameController.castDice();
    }

    public void processSelectedResources(final Map<ResourceType, Integer> selectedResources)
            throws IllegalActionException {
        if (selectedResources.values().stream().mapToInt(Integer::intValue).sum() != getCardsToSelect()) {
            throw new IllegalActionException("Wrong amount of cards selected");
        }
        this.selectedResources = selectedResources;
    }

    // Process Actions

    /**
     * Gets called from viewer thread to trigger an Action. This action will then be waited for using the method
     * {@link #waitForNextAction()}
     *
     * @param action The Action that should be triggered next
     */
    public void triggerAction(final PlayerAction action) {
        actions.add(action);
    }

    public PlayerAction blockingGetNextAction() throws InterruptedException {
        return actions.take();
    }

    public PlayerAction waitForNextAction(final PlayerObjective nextObjective) {
        setPlayerObjective(nextObjective);
        return waitForNextAction();
    }

    @DoNotTouch
    public PlayerAction waitForNextAction() {
        try {
            // blocking, waiting for viewing thread
            final PlayerAction action = blockingGetNextAction();

            System.out.println("TRIGGER " + action + " [" + player.getName() + "]");

            if (!playerObjectiveProperty.getValue().allowedActions.contains(action.getClass())) {
                throw new IllegalActionException(String.format("Illegal Action %s performed. Allowed Actions: %s",
                        action, playerObjectiveProperty.getValue().getAllowedActions()));
            }
            action.execute(this);
            updatePlayerState();
            return action;
        } catch (final IllegalActionException e) {
            // Ignore and keep going
            e.printStackTrace();
            return waitForNextAction();
        } catch (final InterruptedException e) {
            throw new RuntimeException("Main thread was interrupted!", e);
        }
    }

    // -- Building methods --

    private Set<Intersection> getBuildableVillageIntersections() {
        if (!canBuildVillage()) {
            return Set.of();
        }
        Stream<Intersection> intersections = gameController.getState().getGrid().getIntersections().values().stream()
                .filter(intersection -> intersection.getSettlement() == null).filter(intersection -> intersection
                        .getAdjacentIntersections().stream().noneMatch(Intersection::hasSettlement));
        intersections = switch (playerObjectiveProperty.getValue()) {
            case PLACE_VILLAGE -> intersections;
            default ->
                intersections.filter(intersection -> intersection.getConnectedEdges().stream()
                        .anyMatch(edge -> edge.hasRoad() && edge.roadOwner().getValue().equals(player)));
        };
        return intersections.collect(Collectors.toUnmodifiableSet());
    }

    public boolean canBuildVillage() {
        final var requiredResources = Config.SETTLEMENT_BUILDING_COST.get(Settlement.Type.VILLAGE);
        return (playerObjectiveProperty.getValue().equals(PlayerObjective.PLACE_VILLAGE)
                || player.hasResources(requiredResources)) && player.getRemainingVillages() > 0;
    }

    public boolean buildVillage(final Intersection intersection) {
        final var requiredResources = Config.SETTLEMENT_BUILDING_COST.get(Settlement.Type.VILLAGE);
        if (!canBuildVillage()) {
            return false;
        }
        if (!intersection.placeVillage(player,
                playerObjectiveProperty.getValue().equals(PlayerObjective.PLACE_VILLAGE))) {
            return false;
        }
        return playerObjectiveProperty.getValue().equals(PlayerObjective.PLACE_VILLAGE)
                || player.removeResources(requiredResources);
    }

    private Set<Intersection> getUpgradeableVillageIntersections() {
        if (!canUpgradeVillage()) {
            return Set.of();
        }
        return player.getSettlements().stream().filter(settlement -> settlement.type() == Settlement.Type.VILLAGE)
                .map(Settlement::intersection).collect(Collectors.toUnmodifiableSet());
    }

    public boolean canUpgradeVillage() {
        final var requiredResources = Config.SETTLEMENT_BUILDING_COST.get(Settlement.Type.CITY);
        return player.hasResources(requiredResources) && player.getSettlements().stream()
                .anyMatch(settlement -> settlement.type() == Settlement.Type.VILLAGE)
                && player.getRemainingCities() > 0;
    }

    public boolean upgradeVillage(final Intersection intersection) {
        final var requiredResources = Config.SETTLEMENT_BUILDING_COST.get(Settlement.Type.CITY);
        if (!canUpgradeVillage()) {
            return false;
        }
        if (!intersection.upgradeSettlement(player)) {
            return false;
        }
        return player.removeResources(requiredResources);
    }

    private Set<Edge> getBuildableRoadEdges() {
        if (!canBuildRoad()) {
            return Set.of();
        }
        Stream<Edge> edges = gameController.getState().getGrid().getEdges().values().stream()
                .filter(edge -> !edge.hasRoad());
        edges = switch (playerObjectiveProperty.getValue()) {
            case PLACE_ROAD ->
                edges.filter(edge -> edge.getIntersections().stream()
                        .anyMatch(intersection -> intersection.playerHasSettlement(player) && intersection
                                .getConnectedEdges().stream().noneMatch(otherEdge -> otherEdge.hasRoad())));
            default ->
                edges.filter(edge -> edge.getConnectedRoads(player).size() < 4)
                        .filter(edge -> !edge.getConnectedRoads(player).isEmpty());
        };
        return edges.collect(Collectors.toUnmodifiableSet());
    }

    public boolean canBuildRoad() {
        final var requiredResources = Config.ROAD_BUILDING_COST;
        return (playerObjectiveProperty.getValue().equals(PlayerObjective.PLACE_ROAD)
                || player.hasResources(requiredResources)) && player.getRemainingRoads() > 0;
    }

    public boolean buildRoad(final Tile tile, final TilePosition.EdgeDirection edgeDirection) {
        return buildRoad(tile.getPosition(), TilePosition.neighbour(tile.getPosition(), edgeDirection));
    }

    public boolean buildRoad(final TilePosition position0, final TilePosition position1) {
        if (!canBuildRoad()) {
            return false;
        }
        if (!gameController.getState().getGrid().addRoad(position0, position1, player,
                playerObjectiveProperty.getValue().equals(PlayerObjective.PLACE_ROAD))) {
            return false;
        }
        final var requiredResources = Config.ROAD_BUILDING_COST;
        return playerObjectiveProperty.getValue().equals(PlayerObjective.PLACE_ROAD)
                || player.removeResources(requiredResources);
    }

    // Development card methods

    public boolean canBuyDevelopmentCard() {
        return gameController.remainingDevelopmentCards() > 0 && player.hasResources(Config.DEVELOPMENT_CARD_COST);
    }

    public boolean buyDevelopmentCard() {
        if (!canBuyDevelopmentCard()) {
            return false;
        }

        final var requiredResources = Config.DEVELOPMENT_CARD_COST;
        player.addDevelopmentCard(gameController.drawDevelopmentCard());
        player.removeResources(requiredResources);
        return true;
    }

    public void playDevelopmentCard(final DevelopmentCardType developmentCard) {
        switch (developmentCard) {
            case KNIGHT -> {
                waitForNextAction(PlayerObjective.SELECT_ROBBER_TILE);
                waitForNextAction(PlayerObjective.SELECT_CARD_TO_STEAL);
            }
            case ROAD_BUILDING -> {
                waitForNextAction(PlayerObjective.PLACE_ROAD);
                waitForNextAction(PlayerObjective.PLACE_ROAD);
            }
            case INVENTION -> {
                cardsToSelect = 2;
                waitForNextAction(PlayerObjective.SELECT_CARDS);
                player.addResources(selectedResources);
            }
            case MONOPOLY -> {
                cardsToSelect = 1;
                waitForNextAction(PlayerObjective.SELECT_CARDS);
                final ResourceType resourceType = selectedResources.keySet().iterator().next();
                for (final Player player : getOtherPlayers()) {
                    final int amount = player.getResources().getOrDefault(resourceType, 0);
                    player.removeResource(resourceType, amount);
                    getPlayer().addResource(resourceType, amount);
                }
            }
            default -> {
                System.out.printf("No action for development card type %s registered%n", developmentCard);
                return;
            }
        }
        waitForNextAction(PlayerObjective.REGULAR_TURN);
        getPlayer().removeDevelopmentCard(developmentCard);
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
        // check for port
        final var ratio = player.getTradeRatio(offerType);
        if (offerAmount != ratio) {
            return false;
        }
        if (!player.removeResource(offerType, offerAmount)) {
            return false;
        }
        player.addResource(request, 1);
        return true;
    }

    /**
     * Offers the trade to all other players that can accept the trade.
     *
     * @param offer   the offered resources
     * @param request the requested resources
     */
    public void offerTrade(final Map<ResourceType, Integer> offer, final Map<ResourceType, Integer> request) {
        gameController.offerTrade(player, offer, request);
    }

    /**
     * Checks whether this player can accept a trade offer from the other player.
     * A player cannot accept a trade offer from himself or if he has not enough
     * resources.
     *
     * @param otherPlayer the player to trade with
     * @param request     the resources to request
     * @return whether this player can accept a trade offer from the other player
     */
    public boolean canAcceptTradeOffer(final Player otherPlayer, final Map<ResourceType, Integer> request) {
        return !player.equals(otherPlayer) && player.hasResources(request);
    }

    protected void setPlayerTradeOffer(final Player player, final Map<ResourceType, Integer> offer,
            final Map<ResourceType, Integer> request) {
        this.tradingPlayer = player;
        this.playerTradingOffer = offer;
        this.playerTradingRequest = request;
    }

    protected void resetPlayerTradeOffer() {
        this.tradingPlayer = null;
        this.playerTradingOffer = null;
        this.playerTradingRequest = null;
    }

    private TradePayload getPlayerTradingPayload() {
        if (tradingPlayer == null || playerTradingOffer == null || playerTradingRequest == null) {
            return null;
        }
        return new TradePayload(playerTradingOffer, playerTradingRequest, false, tradingPlayer);
    }

    /**
     * Accepts the trade offer from the other player if one exists.
     */
    public void acceptTradeOffer() throws IllegalActionException {
        if (tradingPlayer == null || playerTradingOffer == null || playerTradingRequest == null) {
            throw new IllegalActionException("No trade offer to accept");
        }
        if (!player.hasResources(playerTradingRequest)) {
            throw new IllegalActionException("Player does not have the requested resources");
        }
        if (!tradingPlayer.hasResources(playerTradingOffer)) {
            throw new IllegalActionException("Other player does not have the offered resources");
        }

        player.removeResources(playerTradingRequest);
        tradingPlayer.addResources(playerTradingRequest);
        player.addResources(playerTradingOffer);
        tradingPlayer.removeResources(playerTradingOffer);
    }

    // Robber methods

    /**
     * Selects the resources to drop when a 7 is rolled. Also invokes
     * {@link #endTurn()} to proceed to the next player.
     *
     * @param resourcesToDrop the resources to drop
     */
    public void selectResourcesToDrop(final Map<ResourceType, Integer> resourcesToDrop) {
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
    }

    /**
     * Selects the player and resource to steal from when a 7 is rolled.
     *
     * @param playerToStealFrom the player to steal from
     * @param resourceToSteal   the resource to steal
     */
    public void selectPlayerAndResourceToSteal(final Player playerToStealFrom, final ResourceType resourceToSteal)
            throws IllegalActionException {
        if (!playerToStealFrom.hasResources(Map.of(resourceToSteal, 1))) {
            throw new IllegalActionException("Player does not have the selected resource");
        }
        // remove resource from player
        playerToStealFrom.removeResources(Map.of(resourceToSteal, 1));
        // add resource to player
        player.addResource(resourceToSteal, 1);
    }

    /**
     * Sets the robber position.
     *
     * @param position the position to set the robber to
     */
    public void setRobberPosition(final TilePosition position) {
        gameController.getState().getGrid().setRobberPosition(position);
    }

    /**
     * Returns all players that are next to the robber and not the current player.
     *
     * @return all players that are next to the robber and not the current player.
     */
    public List<Player> getPlayersToStealFrom() {
        return gameController.getState().getGrid().getTileAt(gameController.getState().getGrid().getRobberPosition())
                .getIntersections().stream()
                .filter(Intersection::hasSettlement)
                .map(i -> i.getSettlement().owner())
                .filter(Predicate.not(player::equals))
                .filter(otherPlayer -> !otherPlayer.getResources().isEmpty())
                .collect(Collectors.toUnmodifiableList());
    }
}
