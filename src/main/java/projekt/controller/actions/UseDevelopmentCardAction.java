package projekt.controller.actions;

import projekt.controller.PlayerController;
import projekt.controller.PlayerObjective;
import projekt.model.DevelopmentCardType;
import projekt.model.Player;
import projekt.model.ResourceType;
import projekt.view.gameControls.SelectResourceDialog;

public record UseDevelopmentCardAction(DevelopmentCardType developmentCard) implements PlayerAction {

    @Override
    public void execute(PlayerController pc) throws IllegalActionException {
        switch (developmentCard) {
            case KNIGHT -> {
                pc.waitForNextAction(PlayerObjective.SELECT_ROBBER_TILE);
                pc.waitForNextAction(PlayerObjective.SELECT_CARD_TO_STEAL);
            }
            case ROAD_BUILDING -> {
                pc.waitForNextAction(PlayerObjective.PLACE_ROAD);
                pc.waitForNextAction(PlayerObjective.PLACE_ROAD);
            }
            case INVENTION -> {
                final SelectResourceDialog dialog = new SelectResourceDialog();
                dialog.showAndWait().ifPresentOrElse(
                    result -> pc.getPlayer().addResource(result, 1),
                    () -> pc.triggerAction(new EndTurnAction()));
                dialog.showAndWait().ifPresentOrElse(
                    result -> pc.getPlayer().addResource(result, 1),
                    () -> pc.triggerAction(new EndTurnAction()));
            }
            case MONOPOLY -> {
                final SelectResourceDialog dialog = new SelectResourceDialog();
                ResourceType resourceType = dialog.showAndWait().orElse(null);
                for (Player player : pc.getOtherPlayers()) {
                    int amount = player.getResources().getOrDefault(resourceType, 0);
                    player.removeResource(resourceType, amount);
                    pc.getPlayer().addResource(resourceType, amount);
                }
            }
            default -> {
                System.out.printf("No action for development card type %s registered%n", developmentCard);
                return;
            }
        }
        pc.waitForNextAction(PlayerObjective.REGULAR_TURN);
        pc.getPlayer().removeDevelopmentCard(developmentCard);
    }
}
