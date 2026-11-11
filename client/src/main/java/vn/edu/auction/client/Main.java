package vn.edu.auction.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import vn.edu.auction.client.controller.NavigationController;
import vn.edu.auction.client.navigation.Navigator;

public class Main extends Application {
    @Override public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/vn/edu/auction/client/fxml/browse/browse-view.fxml"), I18n.getBundle());
        Scene scene = new Scene(loader.load(), 1120, 720);
        scene.getStylesheets().add(Main.class.getResource("/vn/edu/auction/client/css/browse.css").toExternalForm());
        vn.edu.auction.client.ui.UiTheme.apply(scene);
        Navigator.setStage(stage);
        NavigationController.setPrimaryStage(stage);
        stage.setTitle("Sàn đấu giá | Khám phá");
        stage.setMinWidth(880);
        stage.setMinHeight(620);
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) { launch(args); }
}
