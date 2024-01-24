package projekt.controller.gui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.util.Builder;
import projekt.controller.PlayerController;
import projekt.controller.PlayerController.PlayerObjective;
import projekt.model.Intersection;
import projekt.model.Player;
import projekt.model.TilePosition;
import projekt.view.gameControls.PlayerActionsBuilder;
import projekt.model.buildings.Settlement;

public class PlayerActionsController implements Controller {
    private final PlayerActionsBuilder builder;
    private final GameBoardController gameBoardController;
    private final Property<PlayerController> playerControllerProperty;
    private final ChangeListener<PlayerObjective> playerObjectiveListener = (observableObjective, oldObjective,
            newObjective) -> {
        enableButtonBasedOnObjective(newObjective);
    };

    public PlayerActionsController(GameBoardController gameBoardController,
            Property<PlayerController> playerController) {
        this.gameBoardController = gameBoardController;
        this.playerControllerProperty = playerController;

        this.builder = new PlayerActionsBuilder(actionWrapper(this::buildVillageButtonAction, true),
                actionWrapper(this::upgradeVillageButtonAction, true), actionWrapper(this::buildRoadButtonAction, true),
                actionWrapper(this::buyDevelopmentCardButtonAction, false),
                actionWrapper(this::endTurnButtonAction, false),
                this::rollDiceButtonAction, this::tradeButtonAction, this::abortButtonAction);
    }

    private void enableButtonBasedOnObjective(PlayerObjective objective) {
        System.out.println("objective: " + objective);
        builder.disableAllButtons();
        switch (objective) {
            case REGULAR_TURN:
                this.updateBuildVillageButtonState();
                this.updateBuildCityButtonState();
                this.updateBuildRoadButtonState();
                builder.enableTradeButton();
                builder.enableEndTurnButton();
                break;
            case DROP_HALF_CARDS:
                builder.enableEndTurnButton();
                break;
            case SELECT_CARD_TO_STEAL:
                builder.enableEndTurnButton();
                break;
            case SELECT_ROBBER_TILE:
                builder.enableEndTurnButton();
                break;
            case PLACE_TWO_ROADS:
                if (getPlayer().getRoads().size() >= 2) {
                    builder.enableEndTurnButton();
                    break;
                }
                buildRoadButtonAction(null);
                break;
            case PLACE_TWO_VILLAGES:
                if (getPlayer().getSettlements().size() >= 2) {
                    builder.enableEndTurnButton();
                    break;
                }
                buildVillageButtonAction(null);
                break;
        }
    }

    private PlayerController getPlayerController() {
        return playerControllerProperty.getValue();
    }

    private Player getPlayer() {
        return getPlayerController().getPlayer();
    }

    private void drawIntersections() {
        getHexGridController().drawIntersections();
    }

    private void removeAllHighlights() {
        getHexGridController().unhighlightRoads();
        getHexGridController().getIntersectionControllers().forEach(ic -> ic.unhighlight());
    }

    private HexGridController getHexGridController() {
        return gameBoardController.getHexGridController();
    }

    private Consumer<ActionEvent> actionWrapper(Consumer<ActionEvent> handler, boolean abortable) {
        return e -> {
            removeAllHighlights();
            drawIntersections();
            if (abortable) {
                builder.disableAllButtons();
                builder.enableAbortButton();
            }

            handler.accept(e);
            gameBoardController.updatePlayerInformation(getPlayer());
        };
    }

    private Consumer<MouseEvent> buildActionWrapper(Consumer<MouseEvent> handler) {
        return e -> {
            handler.accept(e);

            removeAllHighlights();
            enableButtonBasedOnObjective(getPlayerController().getPlayerObjectiveProperty().getValue());
            gameBoardController.updatePlayerInformation(getPlayer());
        };
    }

    private void updateBuildVillageButtonState() {
        if (getPlayerController().canBuildVillage()) {
            builder.enableBuildVillageButton();
            return;
        }
        builder.disableBuildVillageButton();
    }

    private void buildVillageButtonAction(ActionEvent event) {
        Set<IntersectionController> intersectionControllers = getHexGridController()
                .getIntersectionControllers().stream()
                .filter(ic -> ic.getIntersection().getSettlement() == null)
                .filter(ic -> ic.getIntersection().getAdjacentIntersections().stream()
                        .allMatch(i -> i.getSettlement() == null))
                .collect(Collectors.toSet());

        if (!getPlayerController().getPlayerObjectiveProperty().getValue().equals(PlayerObjective.PLACE_TWO_VILLAGES)) {
            intersectionControllers = intersectionControllers.stream()
                    .filter(ic -> ic.getIntersection().getConnectedRoads().stream()
                            .anyMatch(road -> road.owner().equals(getPlayer())))
                    .collect(Collectors.toSet());
        } else if (getPlayer().getSettlements().size() >= 2) {
            return;
        }

        intersectionControllers.forEach(ic -> ic.highlight(buildActionWrapper(e -> {
            getPlayerController().buildVillage(ic.getIntersection());
            drawIntersections();
        })));
    }

    private void updateBuildCityButtonState() {
        if (getPlayerController().canUpgradeVillage()) {
            builder.enableUpgradeVillageButton();
            return;
        }
        builder.disableUpgradeVillageButton();
    }

    private void upgradeVillageButtonAction(ActionEvent event) {
        getPlayer().getSettlements().stream().filter(settlement -> settlement.type() == Settlement.Type.VILLAGE).map(
                settlement -> getHexGridController().getIntersectionControllersMap().get(settlement.intersection()))
                .forEach(ic -> ic.highlight(buildActionWrapper(e -> {
                    getPlayerController().upgradeVillage(ic.getIntersection());
                    drawIntersections();
                })));
    }

    private void updateBuildRoadButtonState() {
        if (getPlayerController().canBuildRoad()) {
            builder.enableBuildRoadButton();
            return;
        }
        builder.disableBuildRoadButton();
    }

    private void buildRoadButtonAction(ActionEvent event) {
        Set<Intersection> intersections;

        if (getPlayerController().getPlayerObjectiveProperty().getValue().equals(PlayerObjective.PLACE_TWO_ROADS)) {
            if (getPlayer().getRoads().size() >= 2) {
                return;
            }

            intersections = getHexGridController().getHexGrid().getIntersections().values().stream()
                    .filter(intersection -> getPlayer().getSettlements().contains(intersection.getSettlement()))
                    .filter(intersection -> intersection.getConnectedRoads().isEmpty())
                    .collect(Collectors.toSet());
        } else {
            intersections = getHexGridController().getHexGrid().getRoads(getPlayer()).values().stream()
                    .filter(road -> road.getConnectedRoads().size() < 4)
                    .flatMap(road -> road.getIntersections().stream()).collect(Collectors.toSet());
        }

        intersections.forEach(intersection -> intersection.getAdjacentIntersections().stream()
                .filter(intersection2 -> !intersection2.hasConnectingRoad(intersection))
                .forEach(intersection2 -> {
                    getHexGridController()
                            .highlightRoad(intersection, intersection2, buildActionWrapper(e -> {
                                final Set<TilePosition> positions = new HashSet<>(
                                        intersection.getAdjacentTilePositions());
                                positions.retainAll(intersection2.getAdjacentTilePositions());
                                List<TilePosition> positionsList = positions.stream().toList();
                                getPlayerController().buildRoad(positionsList.get(0), positionsList.get(1));
                                drawRoads();
                            }));
                }));
    }

    private void drawRoads() {
        getHexGridController().drawRoads();
    }

    private void buyDevelopmentCardButtonAction(ActionEvent event) {
        System.out.println("Buying development card");
    }

    private void endTurnButtonAction(ActionEvent event) {
        getPlayerController().endTurn();
    }

    private void rollDiceButtonAction(ActionEvent event) {
        System.out.println("Rolling dice");
    }

    private void tradeButtonAction(ActionEvent event) {
        System.out.println("Trading");
    }

    private void abortButtonAction(ActionEvent event) {
        removeAllHighlights();
        enableButtonBasedOnObjective(getPlayerController().getPlayerObjectiveProperty().getValue());
        builder.disableAbortButton();
    }

    @Override
    public Builder<Region> getBuilder() {
        return builder;
    }

    @Override
    public Region buildView() {
        Region view = builder.build();
        enableButtonBasedOnObjective(getPlayerController().getPlayerObjectiveProperty().getValue());
        playerControllerProperty.addListener((observable, oldValue, newValue) -> {
            System.out.println("New player: " + newValue.getPlayer().getColor());
            oldValue.getPlayerObjectiveProperty().removeListener(playerObjectiveListener);
            attachObjectiveListener(newValue);
            if (newValue.getPlayerObjectiveProperty().getValue() != null) {
                enableButtonBasedOnObjective(newValue.getPlayerObjectiveProperty().getValue());
            }
        });
        attachObjectiveListener(getPlayerController());
        return view;
    }

    private void attachObjectiveListener(PlayerController newValue) {
        newValue.getPlayerObjectiveProperty()
                .addListener(playerObjectiveListener);
    }
}
