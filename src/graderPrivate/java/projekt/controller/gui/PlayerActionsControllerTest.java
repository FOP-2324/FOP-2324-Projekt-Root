package projekt.controller.gui;

import com.google.common.collect.Iterators;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationTest;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import projekt.Config;
import projekt.MyApplication;
import projekt.SubmissionExecutionHandler;
import projekt.controller.GameController;
import projekt.controller.PlayerController;
import projekt.controller.PlayerObjective;
import projekt.model.*;
import projekt.model.buildings.Edge;
import projekt.util.Utils;
import projekt.view.IntersectionBuilder;
import projekt.view.gameControls.PlayerActionsBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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

//    @BeforeAll
    @Override
    public void start(Stage stage) throws Exception {
        super.start(stage);
        Utils.transformSubmission();

        stage.setWidth(1280);
        stage.setHeight(720);
        stage.show();

        this.stage = stage;
    }

    @BeforeEach
    public void setup() throws ReflectiveOperationException {
        reset();

        hexGrid = new HexGridImpl(1);
        hexGrid.setRobberPosition(new TilePosition(0, 0));
        players = IntStream.range(0, Config.MAX_PLAYERS)
            .mapToObj(i -> new PlayerImpl.Builder(i).build(hexGrid))
            .toList();
        activePlayer = players.get(0);
        gameState = new GameState(hexGrid, players);
        gameController = new GameController(gameState);
        activePlayerController = new PlayerController(gameController, activePlayer);
        activePlayerControllerProperty = new SimpleObjectProperty<>(activePlayerController);
        gameBoardController = new GameBoardController(gameState,
            activePlayerControllerProperty,
            new SimpleIntegerProperty(6),
            new SimpleObjectProperty<>(),
            new SimpleIntegerProperty(10));
        playerActionsController = new PlayerActionsController(gameBoardController, activePlayerControllerProperty);
    }

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

    private void setupForButtonAction(String testedMethodName, PlayerObjective playerObjective, PlayerState playerState) throws ReflectiveOperationException {
        executionHandler.substituteMethod(PlayerActionsController.class.getDeclaredMethod("getPlayerObjective"),
            invocation -> playerObjective);
        executionHandler.substituteMethod(PlayerActionsController.class.getDeclaredMethod("getPlayerState"),
            invocation -> playerState);
        super.interact(() -> stage.setScene(new Scene(gameBoardController.buildView())));

        baseContext = contextBuilder()
            .add("player", activePlayer)
            .add("playerObjective", playerObjective)
            .add("playerState", playerState)
            .build();
    }

    @AfterEach
    public void reset() {
        executionHandler.resetMethodInvocationLogging();
        executionHandler.resetMethodDelegation();
        executionHandler.resetMethodSubstitution();
    }

    @Test
    public void testUpdateBuildVillageButtonState() throws ReflectiveOperationException {
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
    public void testUpdateUpgradeVillageButtonState() throws ReflectiveOperationException {
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
    public void testUpdateBuildRoadButtonState() throws ReflectiveOperationException {
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
    public void testBuildVillageButtonAction() throws ReflectiveOperationException {
        Method buildVillageButtonActionMethod = PlayerActionsController.class.getDeclaredMethod("buildVillageButtonAction", ActionEvent.class);
        Set<Intersection> buildableIntersections = hexGrid.getIntersections().values().stream().limit(5).collect(Collectors.toUnmodifiableSet());
        setupForButtonAction(buildVillageButtonActionMethod.getName(),
            PlayerObjective.IDLE,
            new PlayerState(
                buildableIntersections,
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
            if (buildableIntersections.contains(intersection)) {
                Mockito.verify(intersectionControllerSpy).highlight(ArgumentMatchers.any());

                super.interact(() ->clickOn(intersectionControllerSpy.getBuilder().build(), MouseButton.PRIMARY));
            } else {
                Mockito.verify(intersectionControllerSpy, Mockito.never()).highlight(ArgumentMatchers.any());
            }
        }
    }

    @Test
    public void test() {
        final BorderPane root = new BorderPane();
        // Create a Scene object with the root node of the layout.
        final Scene scene = new Scene(root);
        StackPane stackPane = new StackPane();
        stackPane.setOnMouseClicked(mouseEvent -> System.out.println(mouseEvent));

        // -Title-
        // Get the Java version and JavaFX version.
        final String javaVersion = System.getProperty("java.version");
        final String javafxVersion = System.getProperty("javafx.version");
        // change the text of the label.
        final Label label = new Label();
        label.setText("Hello, JavaFX " + javafxVersion + "\nRunning on Java " + javaVersion + ".");
        // center the label.
        BorderPane.setAlignment(label, javafx.geometry.Pos.CENTER);
        root.setTop(label);
        root.setCenter(stackPane);

//        //-Button-
//        // Add a button to the scene.
//        final Button button = new Button("Click me to change font color!");
//        // Add an action to the button.
//        button.setOnAction(event -> {
//            // change the color of the label to a random color.
//            label.setTextFill(javafx.scene.paint.Color.color(Math.random(), Math.random(), Math.random()));
//        });
//        // also change the color of the button text to match the color of the label.
//        button.textFillProperty().bind(label.textFillProperty());
//        // make it fit the scene.
//        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
//        // add the button to the center of the root node.
//        root.setCenter(button);
        // Add the stylesheet to the scene.
//        final URL styles = getClass().getResource("styles.css");
//        if (styles == null) {
//            throw new IllegalStateException("Could not find styles.css");
//        }
//        scene.getStylesheets().add(styles.toExternalForm());

        // Set the title of the stage.
        stage.setTitle("JavaFX and Gradle");
        // Set the scene to the stage.
        super.interact(() -> stage.setScene(scene));
        // Show the stage.
        super.interact(() -> stage.show());
        clickOn(stackPane, MouseButton.PRIMARY);
    }
}
