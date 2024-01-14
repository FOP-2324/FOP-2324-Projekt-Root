package projekt.model;

import java.util.List;

public record GameState(List<Player> players, HexGrid grid) {

}
