package projekt.view.menus;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.testfx.framework.junit5.ApplicationTest;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import projekt.SubmissionExecutionHandler;
import projekt.model.PlayerImpl;
import projekt.util.Utils;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.*;

@TestForSubmission
public class CreateGameBuilderTest extends ApplicationTest {

    private final SubmissionExecutionHandler executionHandler = SubmissionExecutionHandler.getInstance();
    private Stage stage;
    private Context baseContext;

    private ObservableList<PlayerImpl.Builder> observablePlayers;
    private CreateGameBuilder createGameBuilder;

    @Override
    public void start(Stage stage) {
        Utils.transformSubmission();

        this.stage = stage;
    }

    @BeforeEach
    public void setup() {
        reset();

        observablePlayers = FXCollections.observableArrayList();
        createGameBuilder = new CreateGameBuilder(observablePlayers, () -> {}, () -> false);
        baseContext = contextBuilder()
            .add("observablePlayers", observablePlayers)
            .build();
    }

    @AfterEach
    public void reset() {
        executionHandler.resetMethodInvocationLogging();
        executionHandler.resetMethodDelegation();
        executionHandler.resetMethodSubstitution();
    }

    @Test
    public void testRemovePlayer() throws ReflectiveOperationException {
        int listSize = 5;
        int indexToRemove = 3;
        for (int i = 0; i < listSize; i++) {
            observablePlayers.add(new PlayerImpl.Builder(i + 1));
        }
        Context context = contextBuilder()
            .add("observablePlayers", observablePlayers)
            .add("index to remove", indexToRemove)
            .build();

        Method removePlayerMethod = CreateGameBuilder.class.getDeclaredMethod("removePlayer", int.class);
        removePlayerMethod.trySetAccessible();
        executionHandler.disableMethodDelegation(removePlayerMethod);
        removePlayerMethod.invoke(createGameBuilder, indexToRemove);

        assertEquals(listSize - 1, observablePlayers.size(), context, result ->
            "observablePlayers does not have the expected size after calling removePlayer");
        for (int i = 0; i < listSize - 1; i++) {
            final int finalI = i;
            assertEquals(i + 1, observablePlayers.get(i).getId(), context, result ->
                "The PlayerImpl.Builder object at index %d does not have the expected id".formatted(finalI));
        }
    }

    @Test
    public void testCreateAddPlayerButton() throws Throwable {
        Method createAddPlayerButtonMethod = CreateGameBuilder.class.getDeclaredMethod("createAddPlayerButton");
        createAddPlayerButtonMethod.trySetAccessible();
        executionHandler.disableMethodDelegation(createAddPlayerButtonMethod);
        Node node = (Node) createAddPlayerButtonMethod.invoke(createGameBuilder);

        Button button = null;
        if (node instanceof Button buttonNode) {
            button = buttonNode;
        } else if (node instanceof Parent parent) {
            button = parent.getChildrenUnmodifiable()
                .stream()
                .filter(n -> n instanceof Button)
                .findAny()
                .map(n -> (Button) n)
                .orElseThrow(() -> fail(emptyContext(), result -> "Could not find a button in the returned node"));
        } else {
            fail(emptyContext(), result -> "Could not find a button in the returned node");
        }
        button.fire();
        assertEquals(1, observablePlayers.size(), emptyContext(), result ->
            "Button action did not add a new Player.Builder object to the list");
    }

    @Test
    public void testCreateRemovePlayerButton() throws ReflectiveOperationException {
        int listSize = 3;
        int indexToRemove = listSize - 1;
        for (int i = 0; i < listSize; i++) {
            observablePlayers.add(new PlayerImpl.Builder(i + 1));
        }
        Context context = contextBuilder()
            .add("observablePlayers", observablePlayers)
            .add("index to remove", indexToRemove)
            .build();

        AtomicInteger removedPlayerIndex = new AtomicInteger(-1);
        executionHandler.substituteMethod(CreateGameBuilder.class.getDeclaredMethod("removePlayer", int.class), invocation -> {
            removedPlayerIndex.set(invocation.getIntParameter(0));
            return null;
        });

        Method createRemovePlayerButtonMethod = CreateGameBuilder.class.getDeclaredMethod("createRemovePlayerButton", int.class);
        createRemovePlayerButtonMethod.trySetAccessible();
        executionHandler.disableMethodDelegation(createRemovePlayerButtonMethod);
        Button button = (Button) createRemovePlayerButtonMethod.invoke(createGameBuilder, indexToRemove);
        button.fire();

        if (removedPlayerIndex.get() < 0) {
            assertFalse(observablePlayers.stream().anyMatch(builder -> builder.getId() == indexToRemove), context, result ->
                "The player with index %d was not removed from the observablePlayers list".formatted(indexToRemove));
        } else {
            assertEquals(indexToRemove, removedPlayerIndex.get(), context, result ->
                "The player with index %d was not removed from the observablePlayers list".formatted(indexToRemove));
        }
    }

    @Test
    public void testCreatePlayerColorPicker() throws ReflectiveOperationException {
        PlayerImpl.Builder builder1 = new PlayerImpl.Builder(1).color(Color.BLACK);
        PlayerImpl.Builder builder2 = new PlayerImpl.Builder(2).color(Color.GRAY);
        Context context = contextBuilder()
            .add(baseContext)
            .add("player one", builder1)
            .add("player two", builder2)
            .build();
        observablePlayers.add(builder1);
        observablePlayers.add(builder2);

        Method createPlayerColorPickerMethod = CreateGameBuilder.class.getDeclaredMethod("createPlayerColorPicker", PlayerImpl.Builder.class);
        createPlayerColorPickerMethod.trySetAccessible();
        executionHandler.disableMethodDelegation(createPlayerColorPickerMethod);
        Node node = (Node) createPlayerColorPickerMethod.invoke(createGameBuilder, builder1);

        final ColorPicker colorPicker;
        if (node instanceof ColorPicker colorPickerNode) {
            colorPicker = colorPickerNode;
        } else if (node instanceof Parent parent) {
            colorPicker = parent.getChildrenUnmodifiable()
                .stream()
                .filter(n -> n instanceof ColorPicker)
                .findAny()
                .map(n -> (ColorPicker) n)
                .orElseThrow(fail(context, result -> "Could not find a ColorPicker in the returned node"));
        } else {
            fail(context, result -> "Could not find a ColorPicker in the returned node");
            colorPicker = null;
        }

        colorPicker.setValue(Color.WHITE);
        colorPicker.fireEvent(new ActionEvent(colorPicker, colorPicker));

        assertEquals(Color.WHITE, builder1.getColor(), context, result ->
            "The color of player one was not updated or set to the expected value");

        System.out.println("""
            --- Manual interaction ---
            The following test should show an Alert window which tells the player
            that two players cannot have the same color.
            Please click "OK" to continue.""");
        colorPicker.setValue(Color.GRAY);
        super.interact(() -> colorPicker.fireEvent(new ActionEvent(colorPicker, colorPicker)));
        System.out.println("--- Interaction complete ---");
        assertEquals(Color.WHITE, builder1.getColor(), context, result ->
            "The color of player one was changed to that of player two");
    }

    @Test
    public void testCreateBotOrPlayerSelector() throws ReflectiveOperationException {
        PlayerImpl.Builder builder = new PlayerImpl.Builder(1);
        observablePlayers.add(builder);

        Method createBotOrPlayerSelectorMethod = CreateGameBuilder.class.getDeclaredMethod("createBotOrPlayerSelector", PlayerImpl.Builder.class);
        createBotOrPlayerSelectorMethod.trySetAccessible();
        executionHandler.disableMethodDelegation(createBotOrPlayerSelectorMethod);
        Node node = (Node) createBotOrPlayerSelectorMethod.invoke(createGameBuilder, builder);
        assertTrue(node instanceof Parent, baseContext, result -> "Cannot test node automatically");
        Parent parent = (Parent) node;
        super.interact(() -> {
            stage.setScene(new Scene(parent));
            stage.show();
            clickOn(parent, MouseButton.PRIMARY);
        });

        assertTrue(builder.isAi(), baseContext, result ->
            "Player was not set to AI");
    }
}
