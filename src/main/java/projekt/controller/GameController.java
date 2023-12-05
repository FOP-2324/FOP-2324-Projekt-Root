package projekt.controller;

import projekt.Config;
import projekt.model.HexGrid;

import java.util.stream.IntStream;

public class GameController {

    private static HexGrid GAME_BOARD;

    public GameController() {
        GAME_BOARD = new HexGrid();
    }

    public static HexGrid getGameBoard() {
        return GAME_BOARD;
    }

    public int castDice() {
        return IntStream.rangeClosed(1, Config.NUMBER_OF_DICE)
            .map(i -> Config.RANDOM.nextInt(1, Config.DICE_SIDES + 1))
            .sum();
    }
}
