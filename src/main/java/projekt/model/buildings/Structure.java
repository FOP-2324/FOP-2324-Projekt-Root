package projekt.model.buildings;

import projekt.Config;
import projekt.model.Player;
import projekt.model.ResourceType;

import java.util.Map;

public interface Structure {

    Player getOwner();

    Type getType();

    static Map<ResourceType, Integer> getRequiredResources(Type structureType) {
        return Config.BUILDING_COSTS.get(structureType);
    }

    enum Type {
        ROAD,
        VILLAGE,
        CITY
    }
}
