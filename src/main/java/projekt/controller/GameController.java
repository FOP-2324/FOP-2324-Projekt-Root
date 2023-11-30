package projekt.controller;

import projekt.Config;
import projekt.model.HexGrid;

import java.util.stream.IntStream;

public class GameController {

    private final HexGrid gameBoard;

    public GameController() {
        this.gameBoard = new HexGrid();
    }

    public HexGrid getGameBoard() {
        return this.gameBoard;
    }

    public int castDice() {
        return IntStream.rangeClosed(1, Config.NUMBER_OF_DICE)
            .map(i -> Config.RANDOM.nextInt(1, Config.DICE_SIDES + 1))
            .sum();
    }
}
