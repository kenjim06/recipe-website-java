package application.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;

public final class PageFrame {
    private PageFrame() {
    }

    public static Parent wrap(Parent page) {
        if (page instanceof Region region) {
            region.setMaxSize(900, Double.MAX_VALUE);
        }

        BorderPane frame = new BorderPane(page);
        frame.getStyleClass().add("page-frame");
        frame.setMaxSize(1000, Double.MAX_VALUE);

        StackPane canvas = new StackPane(frame);
        canvas.getStyleClass().add("application-canvas");
        StackPane.setMargin(frame, new Insets(18.0));
        return canvas;
    }
}
