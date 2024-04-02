package projekt.controller.gui;

import com.google.common.collect.Iterators;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.testfx.api.FxToolkit;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import projekt.Config;
import projekt.SubmissionExecutionHandler;
import projekt.controller.GameController;
import projekt.controller.PlayerController;
import projekt.controller.PlayerObjective;
import projekt.model.*;
import projekt.view.gameControls.PlayerActionsBuilder;

import java.lang.reflect.Method;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.*;

@TestForSubmission
public class PlayerActionsControllerTest {

    private final SubmissionExecutionHandler executionHandler = SubmissionExecutionHandler.getInstance();
    private PlayerActionsController playerActionsController;
    private Context baseContext;

    @BeforeAll
    public static void start() throws TimeoutException {
        FxToolkit.registerPrimaryStage();
    }

    private void setup(String testedMethodName,
                       AtomicBoolean calledEnableButton,
                       AtomicBoolean calledDisableButton,
                       PlayerObjective playerObjective,
                       PlayerState playerState) throws NoSuchMethodException {
        reset();

        HexGrid hexGrid = new HexGridImpl(1);
        hexGrid.setRobberPosition(new TilePosition(0, 0));
        List<Player> players = IntStream.range(0, Config.MAX_PLAYERS)
            .mapToObj(i -> new PlayerImpl.Builder(i).build(hexGrid))
            .toList();
        Player activePlayer = players.get(0);
        GameState gameState = new GameState(hexGrid, players);
        GameController gameController = new GameController(gameState);
        PlayerController playerController = new PlayerController(gameController, activePlayer);
        Property<PlayerController> playerControllerProperty = new SimpleObjectProperty<>(playerController);
        GameBoardController gameBoardController = new GameBoardController(gameState,
            playerControllerProperty,
            new SimpleIntegerProperty(6),
            new SimpleObjectProperty<>(),
            new SimpleIntegerProperty(10));

        executionHandler.substituteMethod(PlayerActionsController.class.getDeclaredMethod("getPlayerObjective"),
            invocation -> playerObjective);
        executionHandler.substituteMethod(PlayerActionsController.class.getDeclaredMethod("getPlayerState"),
            invocation -> playerState);
        playerActionsController = new PlayerActionsController(gameBoardController, playerControllerProperty);

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
        setup(updateBuildVillageButtonStateMethod.getName(),
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
        setup(updateBuildVillageButtonStateMethod.getName(),
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
        setup(updateBuildVillageButtonStateMethod.getName(),
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
}
