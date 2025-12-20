import java.awt.Robot;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * 扔骰子小游戏
 */
public class DiceRollingGame {
    private BorderPane root;
    private Label resultLabel;
    private Label scoreLabel;
    private Button rollButton;
    private StackPane dicePane;
    private RobotChatFrame3 parentFrame;
    
    private int playerScore = 0;
    private int robotScore = 0;

    public DiceRollingGame() {
        initUI();
    }

    private void initUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2);");
        
        Label title = new Label("🎲 扔骰子对战 🎲");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        VBox topBox = new VBox(10, title);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(15));
        root.setTop(topBox);
        
        dicePane = createDiceView(1);
        
        resultLabel = new Label("点击按钮开始游戏");
        resultLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        VBox centerBox = new VBox(20, dicePane, resultLabel);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(20));
        root.setCenter(centerBox);
        
        rollButton = new Button("🎲 扔骰子");
        rollButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 20;");
        rollButton.setOnAction(e -> rollDice());
        
        scoreLabel = new Label("你: 0  vs  机器人: 0");
        scoreLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        
        VBox bottomBox = new VBox(10, rollButton, scoreLabel);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(20));
        root.setBottom(bottomBox);
    }

    private StackPane createDiceView(int number) {
        StackPane pane = new StackPane();
        
        Rectangle bg = new Rectangle(80, 80);
        bg.setFill(Color.WHITE);
        bg.setArcWidth(15);
        bg.setArcHeight(15);
        bg.setStroke(Color.BLACK);
        bg.setStrokeWidth(2);
        
        VBox dots = createDots(number);
        pane.getChildren().addAll(bg, dots);
        return pane;
    }

    private VBox createDots(int number) {
        VBox container = new VBox(8);
        container.setAlignment(Pos.CENTER);
        
        switch (number) {
            case 1:
                container.getChildren().add(createDot());
                break;
            case 2:
                container.getChildren().addAll(createDot(), createDot());
                break;
            case 3:
                container.getChildren().addAll(createDot(), createDot(), createDot());
                break;
            case 4:
                HBox row1 = new HBox(30, createDot(), createDot());
                HBox row2 = new HBox(30, createDot(), createDot());
                container.getChildren().addAll(row1, row2);
                break;
            case 5:
                HBox r1 = new HBox(30, createDot(), createDot());
                HBox r2 = new HBox(createDot());
                HBox r3 = new HBox(30, createDot(), createDot());
                container.getChildren().addAll(r1, r2, r3);
                break;
            case 6:
                HBox rx1 = new HBox(25, createDot(), createDot(), createDot());
                HBox rx2 = new HBox(25, createDot(), createDot(), createDot());
                container.getChildren().addAll(rx1, rx2);
                break;
        }
        return container;
    }

    private Circle createDot() {
        Circle dot = new Circle(6);
        dot.setFill(Color.BLACK);
        return dot;
    }

    private void rollDice() {
        rollButton.setDisable(true);
        resultLabel.setText("投掷中...");
        
        Timeline animation = new Timeline(
            new KeyFrame(Duration.millis(100), e -> {
                int dice = (int) (Math.random() * 6) + 1;
                updateDiceView(dice);
            })
        );
        animation.setCycleCount(10);
        animation.setOnFinished(e -> {
            int playerRoll = (int) (Math.random() * 6) + 1;
            updateDiceView(playerRoll);
            
            Timeline robotDelay = new Timeline(new KeyFrame(Duration.millis(800), event -> {
                int robotRoll = (int) (Math.random() * 6) + 1;
                
                String result;
                if (playerRoll > robotRoll) {
                    playerScore++;
                    result = "你赢了！ 你:" + playerRoll + " vs 机器人:" + robotRoll;
                } else if (playerRoll < robotRoll) {
                    robotScore++;
                    result = "机器人赢了！ 你:" + playerRoll + " vs 机器人:" + robotRoll;
                } else {
                    result = "平局！ 都是 " + playerRoll + " 点";
                }
                
                resultLabel.setText(result);
                scoreLabel.setText("你: " + playerScore + "  vs  机器人: " + robotScore);
                rollButton.setDisable(false);
            }));
            robotDelay.play();
        });
        animation.play();
    }

    private void updateDiceView(int number) {
        StackPane newDice = createDiceView(number);
        VBox centerBox = (VBox) root.getCenter();
        centerBox.getChildren().set(0, newDice);
        dicePane = newDice;
    }

    public BorderPane getRoot() {
        return root;
    }
}
