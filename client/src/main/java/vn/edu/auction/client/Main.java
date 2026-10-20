package vn.edu.auction.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Label title = new Label("Online Auction System");

        Button button = new Button("Start Auction App");

        button.setOnAction(event ->
                title.setText("Application is running!")
        );

        VBox root = new VBox(15, title, button);
        root.setStyle("-fx-padding: 30;");

        Scene scene = new Scene(root, 500, 300);

        stage.setTitle("Online Auction System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}