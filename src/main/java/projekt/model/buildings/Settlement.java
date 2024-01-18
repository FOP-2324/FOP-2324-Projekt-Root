package projekt.model.buildings;

import projekt.model.Player;

public record Settlement(Player owner, Type type) {
    public static enum Type {
        VILLAGE(1),
        CITY(2);

        public final int resourceAmount;

        Type(final int resourceAmount) {
            this.resourceAmount = resourceAmount;
        }
    }
}
