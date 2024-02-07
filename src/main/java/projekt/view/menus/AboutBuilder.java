package projekt.view.menus;

import javafx.scene.Node;
import javafx.scene.control.TextArea;

public class AboutBuilder extends MenuBuilder {
    public AboutBuilder(final Runnable returnHandler) {
        super("About FOP-Project WiSe 23/24", returnHandler);
    }

    @Override
    protected Node initCenter() {
        final TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setText(
                """
                        Project for the subject \"Functional and Objectoriented programming concepts\" in the winter semester 2023/24.
                        Based on the game: Die Siedler von Catan
                        Icons from:
                            - https://github.com/Templarian/MaterialDesign
                            - Rocks/Ore: Created by Jan Niklas Prause from the Noun Project
                            - Roads: Created by Pause08 from the Noun Project
                            - Light bulb/Invention: Created by Pavitra from the Noun Project
                            - Knight: Created by Alex Tai from the Noun Project
                        """);
        return textArea;
    }

}
