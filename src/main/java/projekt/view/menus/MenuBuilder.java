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
    private final String returnText;
    private final Runnable returnHandler;
    private final String title;

    public MenuBuilder(final String title, final String returnText, final Runnable quitHandler) {
        this.returnHandler = quitHandler;
        this.title = title;
        this.returnText = returnText;
    }

    public MenuBuilder(final String title, final Runnable returnHandler) {
        this(title, "Return", returnHandler);
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

        final Button returnButton = new Button(returnText);
        returnButton.setMaxWidth(600);
        returnButton.setOnAction(e -> returnHandler.run());
        buttonBox.getChildren().add(returnButton);

        HBox.setHgrow(returnButton, Priority.ALWAYS);

        root.setBottom(buttonBox);

        root.setCenter(initCenter());
    }

    protected abstract Node initCenter();
}
