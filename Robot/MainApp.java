import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MainApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Button btn = new Button("Hello, JavaFX!");
        Scene scene = new Scene(btn, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX in VSC");
        primaryStage.show();
    }
}
