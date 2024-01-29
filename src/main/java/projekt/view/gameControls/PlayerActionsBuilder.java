package projekt.view.gameControls;

import java.util.function.Consumer;

import org.tudalgo.algoutils.student.annotation.DoNotTouch;
import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Builder;

public class PlayerActionsBuilder implements Builder<Region> {

    private final Consumer<ActionEvent> buildVillageButtonAction;
    private final Consumer<ActionEvent> upgradeVillageButtonAction;
    private final Consumer<ActionEvent> buildRoadButtonAction;
    private final Consumer<ActionEvent> buyDevelopmentCardButtonAction;
    private final Consumer<ActionEvent> endTurnButtonAction;
    private final Consumer<ActionEvent> rollDiceButtonAction;
    private final Consumer<ActionEvent> tradeButtonAction;
    private final Consumer<ActionEvent> abortButtonAction;
    private final HBox mainBox = new HBox();
    private Node abortNode;
    private Node buildRoadNode;
    private Node buildVillageNode;
    private Node upgradeVillageNode;
    private Node buyDevelopmentCardNode;
    private Node endTurnNode;
    private Node rollDiceNode;
    private Node tradeNode;

    @DoNotTouch
    public PlayerActionsBuilder(
            final Consumer<ActionEvent> buildVillageButtonAction,
            final Consumer<ActionEvent> upgradeVillageButtonAction,
            final Consumer<ActionEvent> buildRoadButtonAction,
            final Consumer<ActionEvent> buyDevelopmentCardButtonAction,
            final Consumer<ActionEvent> endTurnButtonAction,
            final Consumer<ActionEvent> rollDiceButtonAction,
            final Consumer<ActionEvent> tradeButtonAction,
            final Consumer<ActionEvent> abortButtonAction) {
        this.buildVillageButtonAction = buildVillageButtonAction;
        this.upgradeVillageButtonAction = upgradeVillageButtonAction;
        this.buildRoadButtonAction = buildRoadButtonAction;
        this.buyDevelopmentCardButtonAction = buyDevelopmentCardButtonAction;
        this.endTurnButtonAction = endTurnButtonAction;
        this.rollDiceButtonAction = rollDiceButtonAction;
        this.tradeButtonAction = tradeButtonAction;
        this.abortButtonAction = abortButtonAction;
    }

    @Override
    @StudentImplementationRequired
    public Region build() {
        mainBox.getChildren().clear();

        final Button buildRoadButton = new Button("Straße bauen");
        buildRoadButton.setOnAction(buildRoadButtonAction::accept);
        this.buildRoadNode = buildRoadButton;

        final Button buildVillageButton = new Button("Siedlung bauen");
        buildVillageButton.setOnAction(buildVillageButtonAction::accept);
        this.buildVillageNode = buildVillageButton;

        final Button upgradeVillageButton = new Button("Stadt bauen");
        upgradeVillageButton.setOnAction(upgradeVillageButtonAction::accept);
        this.upgradeVillageNode = upgradeVillageButton;

        final Button buyDevelopmentCardButton = new Button("Entwicklungskarte kaufen");
        buyDevelopmentCardButton.setOnAction(buyDevelopmentCardButtonAction::accept);
        this.buyDevelopmentCardNode = buyDevelopmentCardButton;

        final Button endTurnButton = new Button("Zug beenden");
        endTurnButton.setOnAction(endTurnButtonAction::accept);
        this.endTurnNode = endTurnButton;

        final Button rollDiceButton = new Button("Würfeln");
        rollDiceButton.setOnAction(rollDiceButtonAction::accept);
        this.rollDiceNode = rollDiceButton;

        final Button tradeButton = new Button("Handeln");
        tradeButton.setDisable(true);
        tradeButton.setOnAction(tradeButtonAction::accept);
        this.tradeNode = tradeButton;

        final Button abortButton = new Button("Abbrechen");
        abortButton.setDisable(true);
        abortButton.setOnAction(abortButtonAction::accept);
        this.abortNode = abortButton;

        mainBox.getChildren().addAll(tradeButton, buildRoadButton, buildVillageButton, upgradeVillageButton,
                buyDevelopmentCardButton, rollDiceButton, endTurnButton, abortButton);
        mainBox.setSpacing(5);
        mainBox.setPadding(new Insets(10));
        return mainBox;
    }

    public void disableAllButtons() {
        mainBox.getChildren().stream().filter(Button.class::isInstance).map(node -> (Button) node)
                .forEach(button -> button.setDisable(true));
    }

    public void enableAllButtons() {
        mainBox.getChildren().stream().filter(Button.class::isInstance).map(node -> (Button) node)
                .forEach(button -> button.setDisable(false));
    }

    public void disableAbortButton() {
        abortNode.setDisable(true);
    }

    public void enableAbortButton() {
        abortNode.setDisable(false);
    }

    public void disableBuildRoadButton() {
        buildRoadNode.setDisable(true);
    }

    public void enableBuildRoadButton() {
        buildRoadNode.setDisable(false);
    }

    public void disableBuildVillageButton() {
        buildVillageNode.setDisable(true);
    }

    public void enableBuildVillageButton() {
        buildVillageNode.setDisable(false);
    }

    public void disableUpgradeVillageButton() {
        upgradeVillageNode.setDisable(true);
    }

    public void enableUpgradeVillageButton() {
        upgradeVillageNode.setDisable(false);
    }

    public void disableBuyDevelopmentCardButton() {
        buyDevelopmentCardNode.setDisable(true);
    }

    public void enableBuyDevelopmentCardButton() {
        buyDevelopmentCardNode.setDisable(false);
    }

    public void disableEndTurnButton() {
        endTurnNode.setDisable(true);
    }

    public void enableEndTurnButton() {
        endTurnNode.setDisable(false);
    }

    public void disableRollDiceButton() {
        rollDiceNode.setDisable(true);
    }

    public void enableRollDiceButton() {
        rollDiceNode.setDisable(false);
    }

    public void disableTradeButton() {
        tradeNode.setDisable(true);
    }

    public void enableTradeButton() {
        tradeNode.setDisable(false);
    }
}
