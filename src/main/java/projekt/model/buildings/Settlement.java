package projekt.model.buildings;

import projekt.model.Intersection;
import projekt.model.Player;

public record Settlement(Player owner, Type type, Intersection intersection) {

    /**
     * Defines the different types of settlements.
     *
     * The icons are determined by the order of the enum values.
     * first value: village
     * second value: city
     * potential third value: metropolis //TODO: provide graphics for metropolis
     */
    public enum Type {
        VILLAGE(1),
        CITY(2);

        public final int resourceAmount;

        Type(final int resourceAmount) {
            this.resourceAmount = resourceAmount;
        }
    }
}
