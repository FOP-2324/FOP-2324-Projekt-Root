package projekt.model.buildings;

import projekt.model.Intersection;
import projekt.model.PlayerImpl;

public record Settlement(PlayerImpl owner, Intersection intersection, Type type) {
    enum Type {
        VILLAGE,
        CITY
    }
}
