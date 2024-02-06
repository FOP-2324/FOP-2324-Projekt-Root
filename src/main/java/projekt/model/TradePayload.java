package projekt.model;

import java.util.Map;

public record TradePayload(Map<ResourceType, Integer> offer, Map<ResourceType, Integer> request, boolean withBank,
        Player player) {

    @Override
    public final String toString() {
        return String.format("TradePayload [offer=%s, request=%s, withBank=%s, player=%s]", offer, request, withBank,
                player);
    }
}
