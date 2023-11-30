package projekt.model;

public class Port {

    private final int ratio;
    private final Resource resource;

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
     * @param resource type of resource that
     */
    public Port(int ratio, Resource resource) {
        this.ratio = ratio;
        this.resource = resource;
    }

    public int getRatio() {
        return ratio;
    }

    public Resource getResource() {
        return resource;
    }

    // TODO: not sure this belongs here
    public boolean canTrade(Player player, Resource resource) {
        return (this.resource == null || this.resource.equals(resource)) && player.getAmount(resource) >= ratio;
    }
}
