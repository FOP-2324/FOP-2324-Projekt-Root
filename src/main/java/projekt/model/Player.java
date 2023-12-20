package projekt.model;

import projekt.Config;
import projekt.model.buildings.City;
import projekt.model.buildings.Road;
import projekt.model.buildings.Structure;
import projekt.model.buildings.Village;

import java.util.*;

public class Player {

    protected final Map<ResourceType, Integer> resources = new HashMap<>();
    protected final Map<Structure.Type, List<Structure>> structures = new HashMap<>();
    protected final Map<DevelopmentCardType, Integer> developmentCards = new HashMap<>();

    public Player() {
        for (ResourceType resourceType : ResourceType.values()) {
            this.resources.put(resourceType, 0);
        }
        for (Structure.Type structureType : Structure.Type.values()) {
            this.structures.put(structureType, new ArrayList<>());
        }
        for (DevelopmentCardType developmentCardType : DevelopmentCardType.values()) {
            developmentCards.put(developmentCardType, 0);
        }
    }

    public int getVictoryPoints() {
        int structureVictoryPoints = structures.entrySet()
            .stream()
            .filter(entry -> entry.getKey() == Structure.Type.VILLAGE || entry.getKey() == Structure.Type.CITY)
            .mapToInt(entry -> switch (entry.getKey()) {
                case VILLAGE -> entry.getValue().size();
                case CITY -> 2 * entry.getValue().size();
                default -> 0;
            })
            .sum();

        return structureVictoryPoints;
    }

    public void addResource(ResourceType resourceType, int amount) {
        resources.compute(resourceType, (k, v) -> (v != null ? v : 0) + amount);
    }

    public void removeResource(ResourceType resourceType, int amount) {
        resources.compute(resourceType, (k, v) -> v != null ? v - amount : 0);
    }

    public int getResourceAmount(ResourceType resourceType) {
        return resources.get(resourceType);
    }

    public List<? extends Structure> getStructures() {
        return structures.values()
            .stream()
            .flatMap(Collection::stream)
            .toList();
    }

    public <T extends Structure> List<? extends T> getStructures(Structure.Type structureType) {
        return (List<? extends T>) structures.get(structureType);
    }

    public Map<DevelopmentCardType, Integer> getDevelopmentCards() {
        return Collections.unmodifiableMap(developmentCards);
    }

    public int getDevelopmentCards(DevelopmentCardType developmentCardType) {
        return developmentCards.get(developmentCardType);
    }

    public boolean canBuild(Structure.Type structureType) {
        Map<ResourceType, Integer> requiredResources = Structure.getRequiredResources(structureType);
        for (ResourceType resourceType : requiredResources.keySet()) {
            if (resources.get(resourceType) < requiredResources.get(resourceType)) {
                return false;
            }
        }
        return true;
    }

    public void buildRoad(Intersection nodeA, Intersection nodeB) {
        Structure.Type structureType = Structure.Type.ROAD;
        if (!canBuild(structureType)) {
            throw new IllegalStateException("Player lacks sufficient resources to build a village");
        } else if (structures.get(structureType).size() >= Config.MAP_LIMIT.apply(structureType)) {
            throw new IllegalStateException("Player owns too many villages");
        }

        structures.get(structureType).add(new Road(nodeA, nodeB, this));
        Structure.getRequiredResources(structureType).forEach(this::removeResource);
    }

    public void buildVillage(Position position) {
        Structure.Type structureType = Structure.Type.VILLAGE;
        if (!canBuild(structureType)) {
            throw new IllegalStateException("Player lacks sufficient resources to build a village");
        } else if (structures.get(structureType).size() >= Config.MAP_LIMIT.apply(structureType)) {
            throw new IllegalStateException("Player owns too many villages");
        }

        structures.get(structureType).add(new Village(position, this));
        Structure.getRequiredResources(structureType).forEach(this::removeResource);
    }

    public void buildCity(Village village) {
        Structure.Type structureType = Structure.Type.CITY;
        if (!canBuild(structureType)) {
            throw new IllegalStateException("Player lacks sufficient resources to build a city");
        } else if (structures.get(structureType).size() >= Config.MAP_LIMIT.apply(structureType)) {
            throw new IllegalStateException("Player owns too many cities");
        }

        structures.get(structureType).add(new City(village.getPosition(), this));
        structures.get(Structure.Type.VILLAGE).remove(village);
        Structure.getRequiredResources(structureType).forEach(this::removeResource);
    }

    public void playDevelopmentCard(DevelopmentCardType developmentCard) {
        if (developmentCards.get(developmentCard) <= 0) {
            throw new IllegalStateException("Player does not have cards of type " + developmentCard);
        }
    }
}
