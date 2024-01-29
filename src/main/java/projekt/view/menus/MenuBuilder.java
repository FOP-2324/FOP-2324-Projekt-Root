package projekt.view.menus;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Builder;

public abstract class MenuBuilder implements Builder<Region> {

    protected final BorderPane root = new BorderPane();
    private final Runnable quitHandler;
    private final String title;

    public MenuBuilder(final String title, final Runnable quitHandler) {
        this.quitHandler = quitHandler;
        this.title = title;
    }

    @Override
    public Region build() {
        init(title);
        return root;
    }

    protected void init(final String title) {
        final Label titleLabel = new Label(title);
        titleLabel.setPadding(new Insets(20, 20, 20, 20));
        titleLabel.setId("Title");
        titleLabel.setStyle("-fx-font-size: 50");

        root.setTop(titleLabel);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);

        final HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 20, 20, 20));
        buttonBox.setSpacing(10);

        final Button quitButton = new Button("Quit");
        quitButton.setMaxWidth(600);
        quitButton.setOnAction((e) -> quitHandler.run());
        buttonBox.getChildren().add(quitButton);

        HBox.setHgrow(quitButton, Priority.ALWAYS);

        root.setBottom(buttonBox);

        root.setCenter(initCenter());
    }

    protected abstract Node initCenter();
}
