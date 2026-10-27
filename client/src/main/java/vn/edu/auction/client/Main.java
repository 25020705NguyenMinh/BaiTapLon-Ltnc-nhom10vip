package vn.edu.auction.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource(
                        "/browse-view.fxml"
                )
        );

        Parent root = loader.load();
        Scene scene = new Scene(root, 950, 600);

        scene.getStylesheets().add(
                Objects.requireNonNull(
                        Main.class.getResource(
                                "/css/browse.css"
                        )
                ).toExternalForm()
        );

        stage.setTitle("Online Auction System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
