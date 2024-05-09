package projekt.controller.gui;

import com.google.common.collect.Iterators;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.testfx.framework.junit5.ApplicationTest;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import projekt.Config;
import projekt.SubmissionExecutionHandler;
import projekt.controller.*;
import projekt.controller.actions.*;
import projekt.model.*;
import projekt.model.buildings.Edge;
import projekt.util.Utils;
import projekt.view.IntersectionBuilder;
import projekt.view.gameControls.PlayerActionsBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.*;

@TestForSubmission
public class PlayerActionsControllerTest extends ApplicationTest {

    private final SubmissionExecutionHandler executionHandler = SubmissionExecutionHandler.getInstance();
    private Stage stage;
    private PlayerActionsController playerActionsController;
    private Context baseContext;

    private HexGrid hexGrid;
    private List<Player> players;
    private Player activePlayer;
    private GameState gameState;
    private GameController gameController;
    private PlayerController activePlayerController;
    private Property<PlayerController> activePlayerControllerProperty;
    private GameBoardController gameBoardController;

    @Override
    public void start(Stage stage) {
        Utils.transformSubmission();

        stage.setWidth(1280);
        stage.setHeight(720);
        stage.show();

        this.stage = stage;
    }

    @BeforeEach
    public void setup() throws ReflectiveOperationException {
        reset();

//        Field playerControllersField;
//        Field aiControllersField;
//        try {
//            playerControllersField = GameController.class.getDeclaredField("playerControllers");
//            aiControllersField = GameController.class.getDeclaredField("aiControllers");
//            playerControllersField.trySetAccessible();
//            aiControllersField.trySetAccessible();
//        } catch (ReflectiveOperationException e) {
//            throw new RuntimeException(e);
//        }

        hexGrid = new HexGridImpl(2);
        hexGrid.setRobberPosition(new TilePosition(0, 0));

        players = IntStream.range(0, Config.MAX_PLAYERS)
            .mapToObj(i -> new PlayerImpl.Builder(i).build(hexGrid))
            .toList();
        activePlayer = players.get(0);

        gameState = new GameState(hexGrid, players);

        gameController = new GameController(gameState);
        gameController.initPlayerControllers();
//        Map<Player, PlayerController> playerControllerSpies = (Map<Player, PlayerController>) playerControllersField.get(gameController);
//        players.forEach(player -> playerControllerSpies.computeIfPresent(player, (k, v) -> Mockito.spy(v)));
//        List<AiController> aiControllers = (List<AiController>) aiControllersField.get(gameController);
//        aiControllersField.set(gameController, aiControllers.stream().map(Mockito::spy).collect(Collectors.toList()));

        activePlayerController = gameController.getPlayerControllers().get(activePlayer);
        activePlayerControllerProperty = new SimpleObjectProperty<>(activePlayerController);

        gameBoardController = new GameBoardController(gameState,
            activePlayerControllerProperty,
            new SimpleIntegerProperty(6),
            new SimpleObjectProperty<>(),
            new SimpleIntegerProperty(10));

        playerActionsController = new PlayerActionsController(gameBoardController, activePlayerControllerProperty);
        Field playerControllerPropertyField = PlayerActionsController.class.getDeclaredField("playerControllerProperty");
        playerControllerPropertyField.trySetAccessible();
        playerControllerPropertyField.set(playerActionsController, activePlayerControllerProperty);
    }

    @AfterEach
    public void reset() {
        executionHandler.resetMethodInvocationLogging();
        executionHandler.resetMethodDelegation();
        executionHandler.resetMethodSubstitution();
    }

    @Nested
    public class H3_1 {

        private void setupForButtonState(String testedMethodName,
                                         AtomicBoolean calledEnableButton,
                                         AtomicBoolean calledDisableButton,
                                         PlayerObjective playerObjective,
                                         PlayerState playerState) throws NoSuchMethodException {
            executionHandler.substituteMethod(PlayerActionsController.class.getDeclaredMethod("getPlayerObjective"),
                invocation -> playerObjective);
            executionHandler.substituteMethod(PlayerActionsController.class.getDeclaredMethod("getPlayerState"),
                invocation -> playerState);

            String testedMethodNameSlug = testedMethodName.substring(6, testedMethodName.length() - 5);
            executionHandler.substituteMethod(PlayerActionsBuilder.class.getDeclaredMethod("enable" + testedMethodNameSlug),
                invocation -> {
                    calledEnableButton.set(true);
                    return null;
                });
            executionHandler.substituteMethod(PlayerActionsBuilder.class.getDeclaredMethod("disable" + testedMethodNameSlug),
                invocation -> {
                    calledDisableButton.set(true);
                    return null;
                });

            baseContext = contextBuilder()
                .add("player", activePlayer)
                .add("playerObjective", playerObjective)
                .add("playerState", playerState)
                .build();
        }

        private void setupForButtonAction(PlayerObjective playerObjective, PlayerState playerState) throws ReflectiveOperationException {
            executionHandler.substituteMethod(PlayerActionsController.class.getDeclaredMethod("getPlayerObjective"),
                invocation -> playerObjective);
            executionHandler.substituteMethod(PlayerActionsController.class.getDeclaredMethod("getPlayerState"),
                invocation -> playerState);
            PlayerActionsControllerTest.super.interact(() -> stage.setScene(new Scene(gameBoardController.buildView())));

            baseContext = contextBuilder()
                .add("player", activePlayer)
                .add("playerObjective", playerObjective)
                .add("playerState", playerState)
                .build();
        }

        @Test
        public void testUpdateBuildVillageButtonState () throws ReflectiveOperationException {
            Method updateBuildVillageButtonStateMethod = PlayerActionsController.class.getDeclaredMethod("updateBuildVillageButtonState");
            updateBuildVillageButtonStateMethod.trySetAccessible();

            // wrong objective, no buildable intersections
            AtomicBoolean calledEnableBuildVillageButton = new AtomicBoolean();
            AtomicBoolean calledDisableBuildVillageButton = new AtomicBoolean();
            setupForButtonState(updateBuildVillageButtonStateMethod.getName(),
                calledEnableBuildVillageButton,
                calledDisableBuildVillageButton,
                PlayerObjective.IDLE,
                new PlayerState(
                    Collections.emptySet(),
                    Collections.emptySet(),
                    Collections.emptySet(),
                    Collections.emptyList(),
                    null,
                    0,
                    Collections.emptyMap()));

            executionHandler.disableMethodDelegation(updateBuildVillageButtonStateMethod);
            call(() -> updateBuildVillageButtonStateMethod.invoke(playerActionsController), baseContext, result ->
                "PlayerActionsController.updateBuildVillageButtonState threw an uncaught exception");
            assertFalse(calledEnableBuildVillageButton.get(), baseContext, result ->
                "PlayerActionsController.updateBuildVillageButtonState called builder.enableBuildVillageButton");
            assertTrue(calledDisableBuildVillageButton.get(), baseContext, result ->
                "PlayerActionsController.updateBuildVillageButtonState did not call builder.disableBuildVillageButton");

            // wrong objective, one buildable intersection
            calledEnableBuildVillageButton.set(false);
            calledDisableBuildVillageButton.set(false);
            setupForButtonState(updateBuildVillageButtonStateMethod.getName(),
                calledEnableBuildVillageButton,
                calledDisableBuildVillageButton,
                PlayerObjective.IDLE,
                new PlayerState(
                    new AbstractSet<>() {
                        @Override
                        public Iterator<Intersection> iterator() {
                            return Iterators.singletonIterator(null);
                        }

                        @Override
                        public int size() {
                            return 1;
                        }
                    },
                    Collections.emptySet(),
                    Collections.emptySet(),
                    Collections.emptyList(),
                    null,
                    0,
                    Collections.emptyMap()));
            call(() -> updateBuildVillageButtonStateMethod.invoke(playerActionsController), baseContext, result ->
                "PlayerActionsController.updateBuildVillageButtonState threw an uncaught exception");
            assertFalse(calledEnableBuildVillageButton.get(), baseContext, result ->
                "PlayerActionsController.updateBuildVillageButtonState called builder.enableBuildVillageButton");
            assertTrue(calledDisableBuildVillageButton.get(), baseContext, result ->
                "PlayerActionsController.updateBuildVillageButtonState did not call builder.disableBuildVillageButton");

            // correct objective, one buildable intersection
            calledEnableBuildVillageButton.set(false);
            calledDisableBuildVillageButton.set(false);
            setupForButtonState(updateBuildVillageButtonStateMethod.getName(),
                calledEnableBuildVillageButton,
                calledDisableBuildVillageButton,
                PlayerObjective.REGULAR_TURN,
                new PlayerState(
                    new AbstractSet<>() {
                        @Override
                        public Iterator<Intersection> iterator() {
                            return Iterators.singletonIterator(null);
                        }

                        @Override
                        public int size() {
                            return 1;
                        }
                    },
                    Collections.emptySet(),
                    Collections.emptySet(),
                    Collections.emptyList(),
                    null,
                    0,
                    Collections.emptyMap()));
            call(() -> updateBuildVillageButtonStateMethod.invoke(playerActionsController), baseContext, result ->
                "PlayerActionsController.updateBuildVillageButtonState threw an uncaught exception");
            assertTrue(calledEnableBuildVillageButton.get(), baseContext, result ->
                "PlayerActionsController.updateBuildVillageButtonState did not call builder.enableBuildVillageButton");
            assertFalse(calledDisableBuildVillageButton.get(), baseContext, result ->
                "PlayerActionsController.updateBuildVillageButtonState called builder.disableBuildVillageButton");

            // correct objective, one buildable intersection
            calledEnableBuildVillageButton.set(false);
            calledDisableBuildVillageButton.set(false);
            setupForButtonState(updateBuildVillageButtonStateMethod.getName(),
                calledEnableBuildVillageButton,
                calledDisableBuildVillageButton,
                PlayerObjective.PLACE_VILLAGE,
                new PlayerState(
                    new AbstractSet<>() {
                        @Override
                        public Iterator<Intersection> iterator() {
                            return Iterators.singletonIterator(null);
                        }

                        @Override
                        public int size() {
                            return 1;
                        }
                    },
                    Collections.emptySet(),
                    Collections.emptySet(),
                    Collections.emptyList(),
                    null,
                    0,
                    Collections.emptyMap()));
            call(() -> updateBuildVillageButtonStateMethod.invoke(playerActionsController), baseContext, result ->
                "PlayerActionsController.updateBuildVillageButtonState threw an uncaught exception");
            assertTrue(calledEnableBuildVillageButton.get(), baseContext, result ->
                "PlayerActionsController.updateBuildVillageButtonState did not call builder.enableBuildVillageButton");
            assertFalse(calledDisableBuildVillageButton.get(), baseContext, result ->
                "PlayerActionsController.updateBuildVillageButtonState called builder.disableBuildVillageButton");
        }

        @Test
        public void testUpdateUpgradeVillageButtonState () throws ReflectiveOperationException {
            Method updateUpgradeVillageButtonStateMethod = PlayerActionsController.class.getDeclaredMethod("updateUpgradeVillageButtonState");
            updateUpgradeVillageButtonStateMethod.trySetAccessible();

            // wrong objective, no buildable intersections
            AtomicBoolean calledEnableUpgradeVillageButton = new AtomicBoolean();
            AtomicBoolean calledDisableUpgradeVillageButton = new AtomicBoolean();
            setupForButtonState(updateUpgradeVillageButtonStateMethod.getName(),
                calledEnableUpgradeVillageButton,
                calledDisableUpgradeVillageButton,
                PlayerObjective.IDLE,
                new PlayerState(
                    Collections.emptySet(),
                    Collections.emptySet(),
                    Collections.emptySet(),
                    Collections.emptyList(),
                    null,
                    0,
                    Collections.emptyMap()));

            executionHandler.disableMethodDelegation(updateUpgradeVillageButtonStateMethod);
            call(() -> updateUpgradeVillageButtonStateMethod.invoke(playerActionsController), baseContext, result ->
                "PlayerActionsController.updateUpgradeVillageButtonState threw an uncaught exception");
            assertFalse(calledEnableUpgradeVillageButton.get(), baseContext, result ->
                "PlayerActionsController.updateUpgradeVillageButtonState called builder.enableUpgradeVillageButton");
            assertTrue(calledDisableUpgradeVillageButton.get(), baseContext, result ->
                "PlayerActionsController.updateUpgradeVillageButtonState did not call builder.disableUpgradeVillageButton");

            // wrong objective, one buildable intersection
            calledEnableUpgradeVillageButton.set(false);
            calledDisableUpgradeVillageButton.set(false);
            setupForButtonState(updateUpgradeVillageButtonStateMethod.getName(),
                calledEnableUpgradeVillageButton,
                calledDisableUpgradeVillageButton,
                PlayerObjective.IDLE,
                new PlayerState(
                    Collections.emptySet(),
                    new AbstractSet<>() {
                        @Override
                        public Iterator<Intersection> iterator() {
                            return Iterators.singletonIterator(null);
                        }

                        @Override
                        public int size() {
                            return 1;
                        }
                    },
                    Collections.emptySet(),
                    Collections.emptyList(),
                    null,
                    0,
                    Collections.emptyMap()));
            call(() -> updateUpgradeVillageButtonStateMethod.invoke(playerActionsController), baseContext, result ->
                "PlayerActionsController.updateUpgradeVillageButtonState threw an uncaught exception");
            assertFalse(calledEnableUpgradeVillageButton.get(), baseContext, result ->
                "PlayerActionsController.updateUpgradeVillageButtonState called builder.enableUpgradeVillageButton");
            assertTrue(calledDisableUpgradeVillageButton.get(), baseContext, result ->
                "PlayerActionsController.updateUpgradeVillageButtonState did not call builder.disableUpgradeVillageButton");

            // correct objective, one buildable intersection
            calledEnableUpgradeVillageButton.set(false);
            calledDisableUpgradeVillageButton.set(false);
            setupForButtonState(updateUpgradeVillageButtonStateMethod.getName(),
                calledEnableUpgradeVillageButton,
                calledDisableUpgradeVillageButton,
                PlayerObjective.REGULAR_TURN,
                new PlayerState(
                    Collections.emptySet(),
                    new AbstractSet<>() {
                        @Override
                        public Iterator<Intersection> iterator() {
                            return Iterators.singletonIterator(null);
                        }

                        @Override
                        public int size() {
                            return 1;
                        }
                    },
                    Collections.emptySet(),
                    Collections.emptyList(),
                    null,
                    0,
                    Collections.emptyMap()));
            call(() -> updateUpgradeVillageButtonStateMethod.invoke(playerActionsController), baseContext, result ->
                "PlayerActionsController.updateUpgradeVillageButtonState threw an uncaught exception");
            assertTrue(calledEnableUpgradeVillageButton.get(), baseContext, result ->
                "PlayerActionsController.updateUpgradeVillageButtonState did not call builder.enableUpgradeVillageButton");
            assertFalse(calledDisableUpgradeVillageButton.get(), baseContext, result ->
                "PlayerActionsController.updateUpgradeVillageButtonState called builder.disableUpgradeVillageButton");
        }

        @Test
        public void testUpdateBuildRoadButtonState () throws ReflectiveOperationException {
            Method updateBuildRoadButtonStateMethod = PlayerActionsController.class.getDeclaredMethod("updateBuildRoadButtonState");
            updateBuildRoadButtonStateMethod.trySetAccessible();

            // wrong objective, no buildable intersections
            AtomicBoolean calledEnableBuildRoadButton = new AtomicBoolean();
            AtomicBoolean calledDisableBuildRoadButton = new AtomicBoolean();
            setupForButtonState(updateBuildRoadButtonStateMethod.getName(),
                calledEnableBuildRoadButton,
                calledDisableBuildRoadButton,
                PlayerObjective.IDLE,
                new PlayerState(
                    Collections.emptySet(),
                    Collections.emptySet(),
                    Collections.emptySet(),
                    Collections.emptyList(),
                    null,
                    0,
                    Collections.emptyMap()));

            executionHandler.disableMethodDelegation(updateBuildRoadButtonStateMethod);
            call(() -> updateBuildRoadButtonStateMethod.invoke(playerActionsController), baseContext, result ->
                "PlayerActionsController.updateBuildRoadButtonState threw an uncaught exception");
            assertFalse(calledEnableBuildRoadButton.get(), baseContext, result ->
                "PlayerActionsController.updateBuildRoadButtonState called builder.enableBuildRoadButton");
            assertTrue(calledDisableBuildRoadButton.get(), baseContext, result ->
                "PlayerActionsController.updateBuildRoadButtonState did not call builder.disableBuildRoadButton");

            // wrong objective, one buildable intersection
            calledEnableBuildRoadButton.set(false);
            calledDisableBuildRoadButton.set(false);
            setupForButtonState(updateBuildRoadButtonStateMethod.getName(),
                calledEnableBuildRoadButton,
                calledDisableBuildRoadButton,
                PlayerObjective.IDLE,
                new PlayerState(
                    Collections.emptySet(),
                    Collections.emptySet(),
                    new AbstractSet<>() {
                        @Override
                        public Iterator<Edge> iterator() {
                            return Iterators.singletonIterator(null);
                        }

                        @Override
                        public int size() {
                            return 1;
                        }
                    },
                    Collections.emptyList(),
                    null,
                    0,
                    Collections.emptyMap()));
            call(() -> updateBuildRoadButtonStateMethod.invoke(playerActionsController), baseContext, result ->
                "PlayerActionsController.updateBuildRoadButtonState threw an uncaught exception");
            assertFalse(calledEnableBuildRoadButton.get(), baseContext, result ->
                "PlayerActionsController.updateBuildRoadButtonState called builder.enableBuildRoadButton");
            assertTrue(calledDisableBuildRoadButton.get(), baseContext, result ->
                "PlayerActionsController.updateBuildRoadButtonState did not call builder.disableBuildRoadButton");

            // correct objective, one buildable intersection
            calledEnableBuildRoadButton.set(false);
            calledDisableBuildRoadButton.set(false);
            setupForButtonState(updateBuildRoadButtonStateMethod.getName(),
                calledEnableBuildRoadButton,
                calledDisableBuildRoadButton,
                PlayerObjective.REGULAR_TURN,
                new PlayerState(
                    Collections.emptySet(),
                    Collections.emptySet(),
                    new AbstractSet<>() {
                        @Override
                        public Iterator<Edge> iterator() {
                            return Iterators.singletonIterator(null);
                        }

                        @Override
                        public int size() {
                            return 1;
                        }
                    },
                    Collections.emptyList(),
                    null,
                    0,
                    Collections.emptyMap()));
            call(() -> updateBuildRoadButtonStateMethod.invoke(playerActionsController), baseContext, result ->
                "PlayerActionsController.updateBuildRoadButtonState threw an uncaught exception");
            assertTrue(calledEnableBuildRoadButton.get(), baseContext, result ->
                "PlayerActionsController.updateBuildRoadButtonState did not call builder.enableBuildRoadButton");
            assertFalse(calledDisableBuildRoadButton.get(), baseContext, result ->
                "PlayerActionsController.updateBuildRoadButtonState called builder.disableBuildRoadButton");

            // correct objective, one buildable intersection
            calledEnableBuildRoadButton.set(false);
            calledDisableBuildRoadButton.set(false);
            setupForButtonState(updateBuildRoadButtonStateMethod.getName(),
                calledEnableBuildRoadButton,
                calledDisableBuildRoadButton,
                PlayerObjective.PLACE_ROAD,
                new PlayerState(
                    Collections.emptySet(),
                    Collections.emptySet(),
                    new AbstractSet<>() {
                        @Override
                        public Iterator<Edge> iterator() {
                            return Iterators.singletonIterator(null);
                        }

                        @Override
                        public int size() {
                            return 1;
                        }
                    },
                    Collections.emptyList(),
                    null,
                    0,
                    Collections.emptyMap()));
            call(() -> updateBuildRoadButtonStateMethod.invoke(playerActionsController), baseContext, result ->
                "PlayerActionsController.updateBuildRoadButtonState threw an uncaught exception");
            assertTrue(calledEnableBuildRoadButton.get(), baseContext, result ->
                "PlayerActionsController.updateBuildRoadButtonState did not call builder.enableBuildRoadButton");
            assertFalse(calledDisableBuildRoadButton.get(), baseContext, result ->
                "PlayerActionsController.updateBuildRoadButtonState called builder.disableBuildRoadButton");
        }

        @Test
        public void testBuildVillageButtonAction () throws ReflectiveOperationException, InterruptedException {
            Method buildVillageButtonActionMethod = PlayerActionsController.class.getDeclaredMethod("buildVillageButtonAction", ActionEvent.class);
            Intersection buildableIntersection = hexGrid.getIntersections().values().stream().findAny().orElseThrow();
            setupForButtonAction(
                PlayerObjective.IDLE,
                new PlayerState(
                    Set.of(buildableIntersection),
                    Collections.emptySet(),
                    Collections.emptySet(),
                    Collections.emptyList(),
                    null,
                    0,
                    Collections.emptyMap()));

            PlayerActionsBuilder playerActionsBuilder = (PlayerActionsBuilder) playerActionsController.getBuilder();
            playerActionsBuilder.build();
            Field buildVillageNodeField = PlayerActionsBuilder.class.getDeclaredField("buildVillageNode");
            buildVillageNodeField.trySetAccessible();
            Button buildVillageButton = (Button) buildVillageNodeField.get(playerActionsBuilder);

            // Spy IntersectionControllers
            HexGridController hexGridController = gameBoardController.getHexGridController();
            Field intersectionControllersField = HexGridController.class.getDeclaredField("intersectionControllers");
            intersectionControllersField.trySetAccessible();
            Map<Intersection, IntersectionController> originalIntersectionControllers = (Map<Intersection, IntersectionController>) intersectionControllersField.get(hexGridController);
            Map<Intersection, IntersectionController> intersectionControllerSpies = originalIntersectionControllers.entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey(), Mockito.spy(entry.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            intersectionControllersField.set(hexGridController, intersectionControllerSpies);

            executionHandler.disableMethodDelegation(buildVillageButtonActionMethod);
            buildVillageButton.fire();

            Field paneField = IntersectionBuilder.class.getDeclaredField("pane");
            paneField.trySetAccessible();
            for (Intersection intersection : hexGrid.getIntersections().values()) {
                IntersectionController intersectionControllerSpy = intersectionControllerSpies.get(intersection);
                if (intersection == buildableIntersection) {
                    call(() -> Mockito.verify(intersectionControllerSpy, Mockito.atLeastOnce()).highlight(ArgumentMatchers.any()), baseContext, result ->
                        "An exception occurred while checking that IntersectionController.highlight was called: " + result.cause());

                    PlayerActionsControllerTest.super.interact(() -> clickOn((Node) paneField.get(intersectionControllerSpy.getBuilder()), MouseButton.PRIMARY));
                    assertTrue(activePlayerController.blockingGetNextAction() instanceof BuildVillageAction, baseContext, result ->
                        "The BuildVillageAction was not added to the active PlayerController");
                } else {
                    call(() -> Mockito.verify(intersectionControllerSpy, Mockito.never()).highlight(ArgumentMatchers.any()), baseContext, result ->
                        "An exception occurred while checking that IntersectionController.highlight was not called: " + result.cause());
                }
            }
        }

        @Test
        public void testUpgradeVillageButtonAction () throws ReflectiveOperationException, InterruptedException {
            Method upgradeVillageButtonActionMethod = PlayerActionsController.class.getDeclaredMethod("upgradeVillageButtonAction", ActionEvent.class);
            Intersection upgradableIntersection = hexGrid.getIntersections().values().stream().findAny().orElseThrow();
            setupForButtonAction(
                PlayerObjective.IDLE,
                new PlayerState(
                    Collections.emptySet(),
                    Set.of(upgradableIntersection),
                    Collections.emptySet(),
                    Collections.emptyList(),
                    null,
                    0,
                    Collections.emptyMap()));

            PlayerActionsBuilder playerActionsBuilder = (PlayerActionsBuilder) playerActionsController.getBuilder();
            playerActionsBuilder.build();
            Field upgradeVillageNodeField = PlayerActionsBuilder.class.getDeclaredField("upgradeVillageNode");
            upgradeVillageNodeField.trySetAccessible();
            Button upgradeVillageButton = (Button) upgradeVillageNodeField.get(playerActionsBuilder);

            // Spy IntersectionControllers
            HexGridController hexGridController = gameBoardController.getHexGridController();
            Field intersectionControllersField = HexGridController.class.getDeclaredField("intersectionControllers");
            intersectionControllersField.trySetAccessible();
            Map<Intersection, IntersectionController> originalIntersectionControllers = (Map<Intersection, IntersectionController>) intersectionControllersField.get(hexGridController);
            Map<Intersection, IntersectionController> intersectionControllerSpies = originalIntersectionControllers.entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey(), Mockito.spy(entry.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            intersectionControllersField.set(hexGridController, intersectionControllerSpies);

            executionHandler.disableMethodDelegation(upgradeVillageButtonActionMethod);
            upgradeVillageButton.fire();

            Field paneField = IntersectionBuilder.class.getDeclaredField("pane");
            paneField.trySetAccessible();
            for (Intersection intersection : hexGrid.getIntersections().values()) {
                IntersectionController intersectionControllerSpy = intersectionControllerSpies.get(intersection);
                if (intersection == upgradableIntersection) {
                    call(() -> Mockito.verify(intersectionControllerSpy, Mockito.atLeastOnce()).highlight(ArgumentMatchers.any()), baseContext, result ->
                        "An exception occurred while checking that IntersectionController.highlight was called: " + result.cause());

                    PlayerActionsControllerTest.super.interact(() -> clickOn((Node) paneField.get(intersectionControllerSpy.getBuilder()), MouseButton.PRIMARY));
                    assertTrue(activePlayerController.blockingGetNextAction() instanceof UpgradeVillageAction, baseContext, result ->
                        "The UpgradeVillageAction was not added to the active PlayerController");
                } else {
                    call(() -> Mockito.verify(intersectionControllerSpy, Mockito.never()).highlight(ArgumentMatchers.any()), baseContext, result ->
                        "An exception occurred while checking that IntersectionController.highlight was not called: " + result.cause());
                }
            }
        }

        @Test
        public void testBuildRoadButtonAction () throws ReflectiveOperationException, InterruptedException {
            Method buildRoadButtonActionMethod = PlayerActionsController.class.getDeclaredMethod("buildRoadButtonAction", ActionEvent.class);
            Edge buildableRoad = hexGrid.getEdge(new TilePosition(0, 0), new TilePosition(0, 1));
            setupForButtonAction(
                PlayerObjective.IDLE,
                new PlayerState(
                    Collections.emptySet(),
                    Collections.emptySet(),
                    Set.of(buildableRoad),
                    Collections.emptyList(),
                    null,
                    0,
                    Collections.emptyMap()));

            PlayerActionsBuilder playerActionsBuilder = (PlayerActionsBuilder) playerActionsController.getBuilder();
            playerActionsBuilder.build();
            Field buildRoadNodeField = PlayerActionsBuilder.class.getDeclaredField("buildRoadNode");
            buildRoadNodeField.trySetAccessible();
            Button buildRoadButton = (Button) buildRoadNodeField.get(playerActionsBuilder);

            // Spy EdgeControllers
            HexGridController hexGridController = gameBoardController.getHexGridController();
            Field edgeControllersField = HexGridController.class.getDeclaredField("edgeControllers");
            edgeControllersField.trySetAccessible();
            Map<Edge, EdgeController> originalEdgeControllers = (Map<Edge, EdgeController>) edgeControllersField.get(hexGridController);
            Map<Edge, EdgeController> edgeControllerSpies = originalEdgeControllers.entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey(), Mockito.spy(entry.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            edgeControllersField.set(hexGridController, edgeControllerSpies);

            executionHandler.disableMethodDelegation(buildRoadButtonActionMethod);
            buildRoadButton.fire();

            Field lineField = EdgeController.class.getDeclaredField("line");
            lineField.trySetAccessible();
            for (Edge edge : hexGrid.getEdges().values()) {
                EdgeController edgeControllerSpy = edgeControllerSpies.get(edge);
                if (edge == buildableRoad) {
                    call(() -> Mockito.verify(edgeControllerSpy, Mockito.atLeastOnce()).highlight(ArgumentMatchers.any()), baseContext, result ->
                        "An exception occurred while checking that EdgeController.highlight was called: " + result.cause());

                    PlayerActionsControllerTest.super.interact(() -> clickOn((Node) lineField.get(edgeControllerSpy), MouseButton.PRIMARY));
                    assertTrue(activePlayerController.blockingGetNextAction() instanceof BuildRoadAction, baseContext, result ->
                        "The BuildRoadAction was not added to the active PlayerController");
                } else {
                    call(() -> Mockito.verify(edgeControllerSpy, Mockito.never()).highlight(ArgumentMatchers.any()), baseContext, result ->
                        "An exception occurred while checking that EdgeController.highlight was not called: " + result.cause());
                }
            }
        }
    }

    @Nested
    public class H3_2 {

        private static List<Method> unconditionalMethods;
        private static Map<Class<? extends PlayerAction>, Method> nonAiMethods;
        private Map<Method, AtomicBoolean> methodCalled;

        @BeforeAll
        public static void methodSetup() throws ReflectiveOperationException {
            unconditionalMethods = new ArrayList<>();
            unconditionalMethods.add(PlayerActionsController.class.getDeclaredMethod("removeAllHighlights"));
            unconditionalMethods.add(PlayerActionsController.class.getDeclaredMethod("drawEdges"));
            unconditionalMethods.add(PlayerActionsController.class.getDeclaredMethod("drawIntersections"));
            unconditionalMethods.add(HexGridController.class.getDeclaredMethod("drawTiles"));
            unconditionalMethods.add(PlayerActionsBuilder.class.getDeclaredMethod("disableAllButtons"));
            unconditionalMethods.add(PlayerActionsController.class.getDeclaredMethod("updatePlayerInformation"));

            nonAiMethods = new HashMap<>();
            nonAiMethods.put(EndTurnAction.class, PlayerActionsBuilder.class.getDeclaredMethod("enableEndTurnButton"));
            nonAiMethods.put(RollDiceAction.class, PlayerActionsBuilder.class.getDeclaredMethod("enableRollDiceButton"));
            nonAiMethods.put(TradeAction.class, PlayerActionsBuilder.class.getDeclaredMethod("enableTradeButton"));
            nonAiMethods.put(PlayDevelopmentCardAction.class, PlayerActionsController.class.getDeclaredMethod("updateUseDevelopmentCardButtonState"));
            nonAiMethods.put(BuyDevelopmentCardAction.class, PlayerActionsController.class.getDeclaredMethod("updateBuyDevelopmentCardButtonState"));
            nonAiMethods.put(BuildVillageAction.class, PlayerActionsController.class.getDeclaredMethod("updateBuildVillageButtonState"));
            nonAiMethods.put(UpgradeVillageAction.class, PlayerActionsController.class.getDeclaredMethod("updateUpgradeVillageButtonState"));
            nonAiMethods.put(BuildRoadAction.class, PlayerActionsController.class.getDeclaredMethod("updateBuildRoadButtonState"));
            nonAiMethods.put(SelectRobberTileAction.class, HexGridController.class.getDeclaredMethod("highlightTiles", Consumer.class));
            nonAiMethods.put(StealCardAction.class, PlayerActionsController.class.getDeclaredMethod("selectCardToStealAction"));
            nonAiMethods.put(SelectCardsAction.class, PlayerActionsController.class.getDeclaredMethod("selectResources", int.class));
            nonAiMethods.put(AcceptTradeAction.class, PlayerActionsController.class.getDeclaredMethod("acceptTradeOffer"));
        }

        @BeforeEach
        public void setup() {
            reset();

            methodCalled = new HashMap<>();
            unconditionalMethods.forEach(method -> {
                methodCalled.put(method, new AtomicBoolean());
                executionHandler.substituteMethod(method, invocation -> {
                    methodCalled.get(method).set(true);
                    return null;
                });
            });
            nonAiMethods.values().forEach(method -> {
                methodCalled.put(method, new AtomicBoolean());
                executionHandler.substituteMethod(method, invocation -> {
                    methodCalled.get(method).set(true);
                    return null;
                });
            });
        }

        private void testUpdateUIBasedOnObjective(PlayerObjective playerObjective, boolean isAi) throws ReflectiveOperationException {
            executionHandler.substituteMethod(PlayerActionsController.class.getDeclaredMethod("getPlayerObjective"),
                invocation -> playerObjective);
            PlayerState playerState = new PlayerState(
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptyList(),
                null,
                1,
                Collections.emptyMap()
            );
            executionHandler.substituteMethod(PlayerActionsController.class.getDeclaredMethod("getPlayerState"),
                invocation -> playerState);
//            PlayerActionsControllerTest.super.interact(() -> stage.setScene(new Scene(gameBoardController.buildView())));

            baseContext = contextBuilder()
                .add("player", activePlayer)
                .add("playerObjective", playerObjective)
                .add("playerState", playerState)
                .build();

            Method updateUIBasedOnObjectiveMethod = PlayerActionsController.class.getDeclaredMethod("updateUIBasedOnObjective", PlayerObjective.class);
            updateUIBasedOnObjectiveMethod.trySetAccessible();
            executionHandler.disableMethodDelegation(updateUIBasedOnObjectiveMethod);
            updateUIBasedOnObjectiveMethod.invoke(playerActionsController, playerObjective);

            for (Method method : unconditionalMethods) {
                Context context = contextBuilder()
                    .add(baseContext)
                    .add("checked method", method.getName())
                    .build();
                assertTrue(methodCalled.get(method).get(), context, result ->
                    "updateUIBasedOnPlayerObjective did not call the checked method");
            }
            for (Map.Entry<Class<? extends PlayerAction>, Method> entry : nonAiMethods.entrySet()) {
                Class<? extends PlayerAction> playerActionClass = entry.getKey();
                Method method = entry.getValue();
                Context context = contextBuilder()
                    .add(baseContext)
                    .add("checked PlayerAction", playerActionClass)
                    .add("checked method", method.getName())
                    .build();

                if (isAi) {
                    assertFalse(methodCalled.get(method).get(), context, result ->
                        "updateUIBasedOnPlayerObjective should not have called the checked method since the player is AI");
                } else {
                    if (playerObjective.getAllowedActions().contains(playerActionClass)) {
                        assertTrue(methodCalled.get(method).get(), context, result ->
                            "updateUIBasedOnPlayerObjective should have called the checked method");
                    } else {
                        assertFalse(methodCalled.get(method).get(), context, result ->
                            "updateUIBasedOnPlayerObjective should not have called the checked method");
                    }
                }
            }
        }

        @ParameterizedTest
        @EnumSource(PlayerObjective.class)
        public void testWithPlayer(PlayerObjective playerObjective) throws ReflectiveOperationException {
            testUpdateUIBasedOnObjective(playerObjective, false);
        }

        @ParameterizedTest
        @EnumSource(PlayerObjective.class)
        public void testWithAi(PlayerObjective playerObjective) throws ReflectiveOperationException {
            executionHandler.substituteMethod(PlayerImpl.class.getDeclaredMethod("isAi"), invocation -> true);
            testUpdateUIBasedOnObjective(playerObjective, true);
        }
    }
}
