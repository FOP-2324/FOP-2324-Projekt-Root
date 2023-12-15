package projekt.controller;

import projekt.Config;
import projekt.model.HexGrid;
import projekt.model.Intersection;
import projekt.model.Player;
import projekt.model.Position;
import projekt.model.buildings.Settlement;
import projekt.model.tiles.Tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GameController {

    private static GameController INSTANCE;
    private final HexGrid gameBoard;
    private final List<Player> players = new ArrayList<>();
    private Tile banditTile = null;


    private GameController() {
        gameBoard = new HexGrid();
    }

    public static GameController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GameController();
        }

        return INSTANCE;
    }

    public HexGrid getGameBoard() {
        return gameBoard;
    }

    public Player newPlayer() {
        Player player = new Player();
        players.add(player);
        return player;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public Tile getBanditTile() {
        return banditTile;
    }

    public void setBanditTile(Tile tile) {
        this.banditTile = tile;
    }

    public int castDice() {
        return IntStream.rangeClosed(1, Config.NUMBER_OF_DICE)
            .map(i -> Config.RANDOM.nextInt(1, Config.DICE_SIDES + 1))
            .sum();
    }

    public void resourcePhase() {
        int diceValue = castDice();

        if (diceValue != 7) {
            Set<? extends Settlement> settlements = players.stream()
                .flatMap(player -> player.getStructures()
                    .stream()
                    .filter(structure -> structure instanceof Settlement)
                    .map(structure -> (Settlement) structure))
                .collect(Collectors.toSet());

            gameBoard.getTiles()
                .stream()
                .filter(tile -> tile.getYield() == diceValue)
                .forEach(tile -> {
                    Set<Position> adjacentIntersections = tile.getAdjacentIntersections()
                        .values()
                        .stream()
                        .map(Intersection::getPosition)
                        .collect(Collectors.toSet());
                    settlements.forEach(settlement -> {
                        if (adjacentIntersections.contains(settlement.getPosition())) {
                            settlement.getOwner().addResource(tile.getResource(), 1);
                        }
                    });
                });
        } else {
            // bandit becomes active
        }
    }
}
