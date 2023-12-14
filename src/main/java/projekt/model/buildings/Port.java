package projekt.model.buildings;

import projekt.model.Player;
import projekt.model.ResourceType;

public class Port {

    private final int ratio;
    private final ResourceType resourceType;

    /**
     * Constructs a new port with n:1 ratio, meaning that n resources of the same type can be traded for 1 resource of any type.
     * @param ratio amount of same-type resources to trade
     */
    public Port(int ratio) {
        this(ratio, null);
    }

    /**
     * Constructs a new port with n:1 ratio, meaning that n resources of the specified type can be traded for 1 resource of any type.
     * @param ratio    amount of same-type resources to trade
     * @param resourceType type of resource that
     */
    public Port(int ratio, ResourceType resourceType) {
        this.ratio = ratio;
        this.resourceType = resourceType;
    }

    public int getRatio() {
        return ratio;
    }

    public ResourceType getResource() {
        return resourceType;
    }

    // TODO: not sure this belongs here
    public boolean canTrade(Player player, ResourceType resourceType) {
        return (this.resourceType == null || this.resourceType.equals(resourceType)) && player.getResourceAmount(resourceType) >= ratio;
    }
}
