package projekt.controller.actions;

import java.util.Map.Entry;

import projekt.controller.PlayerController;
import projekt.model.ResourceType;
import projekt.model.TradePayload;

public record TradeAction(TradePayload payload) implements PlayerAction {

    @Override
    public void execute(final PlayerController pc) throws IllegalActionException {
        if (payload.withBank()) {
            final Entry<ResourceType, Integer> offer = payload.offer().entrySet().iterator().next();
            pc.tradeWithBank(offer.getKey(), offer.getValue(), payload.request().keySet().iterator().next());
        } else {
            pc.offerTrade(payload.offer(), payload.request());
        }
    }
}
