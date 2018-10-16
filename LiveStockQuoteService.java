package martin;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class LiveStockQuoteService extends Application {

    public static void main(String[] args) {
        launch(args);
        System.exit(0);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        TabPane tabPane = new TabPane();
        Tab tab = new Tab();
        tab = RealTimeStockChart.multiChart();
        tabPane.getTabs().add(tab);
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(tabPane);
        Scene scene = new Scene(borderPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
