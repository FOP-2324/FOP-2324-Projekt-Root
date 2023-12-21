package projekt.view;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.util.Builder;

public class GameBoardBuilder implements Builder<Region> {
    private final Region hexGrid;

    public GameBoardBuilder(Region hexGrid) {
        this.hexGrid = hexGrid;
    }

    @Override
    public Region build() {
        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(hexGrid);
        return mainPane;
    }

}
