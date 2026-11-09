package vn.edu.auction.client.ui;

import javafx.scene.Scene;
import javafx.scene.control.DialogPane;

public final class UiTheme {
    private static final String CSS = UiTheme.class.getResource(
            "/vn/edu/auction/client/css/theme.css").toExternalForm();

    private UiTheme() {}

    public static void apply(Scene scene) {
        scene.getStylesheets().remove(CSS);
        scene.getStylesheets().add(CSS);
    }

    public static void apply(DialogPane pane) {
        if (!pane.getStylesheets().contains(CSS)) pane.getStylesheets().add(CSS);
        if (!pane.getStyleClass().contains("auction-dialog")) pane.getStyleClass().add("auction-dialog");
    }
}
