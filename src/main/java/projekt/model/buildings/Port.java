package projekt.model.buildings;

import projekt.model.Intersection;
import projekt.model.ResourceType;

public record Port(int ratio, Intersection intersection, ResourceType resourceType) {

    /**
     * Constructs a new port with n:1 ratio, meaning that n resources of the same
     * type can be traded for 1 resource of any type.
     *
     * @param ratio amount of same-type resources to trade
     */
    public Port(int ratio, Intersection intersection) {
        this(ratio, intersection, null);
    }
}
