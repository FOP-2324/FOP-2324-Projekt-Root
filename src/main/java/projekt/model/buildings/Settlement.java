package projekt.model.buildings;

import projekt.model.Intersection;
import projekt.model.Player;

public record Settlement(Player owner, Intersection intersection, Type type) {
    public enum Type {
        VILLAGE,
        CITY
    }
}
