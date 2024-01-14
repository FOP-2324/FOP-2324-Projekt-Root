package projekt.controller;

import javafx.scene.paint.Color;
import projekt.Config;
import projekt.model.HexGrid;
import projekt.model.HexGridImpl;
import projekt.model.Player;
import projekt.model.PlayerImpl;
import projekt.model.tiles.Tile;
import projekt.view.GameBoardBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class GameController extends SceneController {

    private static GameController INSTANCE;
    private final HexGrid grid;
    private final List<Player> players = new ArrayList<>();
    private Tile banditTile = null;

    private GameController(final HexGrid grid) {
        super(new GameBoardBuilder(HexGridController.getHexGridController(grid).getView()));
        this.grid = grid;
    }

    public static GameController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GameController(new HexGridImpl(Config.GRID_RADIUS));
        }

        return INSTANCE;
    }

    public HexGrid getGrid() {
        return grid;
    }

    public Player newPlayer() {
        final Player player = new PlayerImpl(grid, Color.AQUA);
        players.add(player);
        return player;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public Tile getBanditTile() {
        return banditTile;
    }

    public void setBanditTile(final Tile tile) {
        this.banditTile = tile;
    }

    public int castDice() {
        return IntStream.rangeClosed(1, Config.NUMBER_OF_DICE)
            .map(i -> Config.RANDOM.nextInt(1, Config.DICE_SIDES + 1))
            .sum();
    }

    public void resourcePhase() {
        final int diceValue = castDice();

//        if (diceValue != 7) {
//            final Set<? extends Settlement> settlements = players.stream()
//                .flatMap(player -> player.getStructures()
//                    .stream()
//                    .filter(structure -> structure instanceof Settlement)
//                    .map(structure -> (Settlement) structure))
//                .collect(Collectors.toSet());
//
//            grid.getTiles()
//                .stream()
//                .filter(tile -> tile.getYield() == diceValue)
//                .forEach(tile -> {
//                    final Set<TilePosition> adjacentIntersections = tile.getAdjacentIntersections()
//                        .values()
//                        .stream()
//                        .map(Intersection::getPosition)
//                        .collect(Collectors.toSet());
//                    settlements.forEach(settlement -> {
//                        if (adjacentIntersections.contains(settlement.getPosition())) {
//                            settlement.getOwner().addResource(tile.getResource(), 1);
//                        }
//                    });
//                });
//        } else {
//            // bandit becomes active
//        }
    }

    @Override
    public String getTitle() {
        return "Siedler von Catan";
    }
}
