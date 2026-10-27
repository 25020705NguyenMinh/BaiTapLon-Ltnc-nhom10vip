package vn.edu.auction.client;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // SỬA: không tự load FXML nữa, giao cho Navigator
        Navigator.setStage(stage);
        stage.setResizable(false);
        Navigator.show("login-view.fxml", "title.login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
