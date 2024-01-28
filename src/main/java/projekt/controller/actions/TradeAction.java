package projekt.controller.actions;

import java.util.Map.Entry;

import projekt.controller.PlayerController;
import projekt.model.ResourceType;
import projekt.model.TradePayload;

public record TradeAction(TradePayload payload) implements PlayerAction {

    @Override
    public void execute(PlayerController pc) throws IllegalActionException {
        if (payload.withBank()) {
            Entry<ResourceType, Integer> offer = payload.offer().entrySet().stream().findFirst().get();
            pc.tradeWithBank(offer.getKey(), offer.getValue(), payload.request().keySet().stream().findFirst().get());
        } else {
            // TODO: implement trade with other players
        }
    }

}
