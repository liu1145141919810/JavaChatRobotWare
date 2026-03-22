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
 * DiceRollingGame
 * --------------------------------------------------
 * JavaFX 扔骰子小游戏（玩家 vs 机器人）
 *
 * 功能说明：
 * 1. 点击按钮后播放骰子滚动动画
 * 2. 玩家与机器人各自随机掷一次骰子
 * 3. 比较点数大小并累计比分
 * 4. 实时更新界面显示结果
 *
 * 设计特点：
 * - 使用 JavaFX 布局组件（BorderPane / VBox / HBox）
 * - 使用 Timeline 实现动画效果
 * - 使用 Shape（Rectangle + Circle）绘制骰子
 */
public class DiceRollingGame {
    /** 主布局容器，整个游戏界面的根节点 */
    private BorderPane root;
    /** 显示当前对局结果的文本标签 */
    private Label resultLabel;
    /** 显示玩家与机器人总分的标签 */
    private Label scoreLabel;
    /** 掷骰子按钮 */
    private Button rollButton;
    /** 骰子显示区域（图形化骰子） */
    private StackPane dicePane;
    /** 父窗口引用（可用于与主界面通信，目前未使用） */
    private RobotChatFrame3 parentFrame;
    /** 玩家当前得分 */
    private int playerScore = 0;
    /** 机器人当前得分 */
    private int robotScore = 0;
    /**
     * 构造方法
     * 初始化游戏界面
     */
    public DiceRollingGame() {
        initUI();
    }

    /**
     * 初始化游戏 UI 界面
     * ------------------------------------------------
     * 使用 BorderPane 进行整体布局：
     * Top    : 标题
     * Center : 骰子显示 + 结果文本
     * Bottom : 操作按钮 + 分数显示
     */
    private void initUI() {

        // 根布局
        root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2);");

        // ====== 顶部标题 ======
        Label title = new Label("🎲 扔骰子对战 🎲");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        VBox topBox = new VBox(10, title);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(15));
        root.setTop(topBox);

        // ====== 中央骰子显示区域 ======
        dicePane = createDiceView(1); // 初始显示 1 点

        resultLabel = new Label("点击按钮开始游戏");
        resultLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        VBox centerBox = new VBox(20, dicePane, resultLabel);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(20));
        root.setCenter(centerBox);

        // ====== 底部控制区域 ======
        rollButton = new Button("🎲 扔骰子");
        rollButton.setStyle(
                "-fx-background-color: #ff6b6b; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10 30; " +
                "-fx-background-radius: 20;"
        );
        rollButton.setOnAction(e -> rollDice());
        scoreLabel = new Label("你: 0  vs  机器人: 0");
        scoreLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        VBox bottomBox = new VBox(10, rollButton, scoreLabel);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(20));
        root.setBottom(bottomBox);
    }
    /**
     * 创建骰子视图
     *
     * @param number 骰子点数（1~6）
     * @return StackPane 表示一个完整骰子
     */
    private StackPane createDiceView(int number) {
        StackPane pane = new StackPane();
        // 骰子背景（白色圆角矩形）
        Rectangle bg = new Rectangle(80, 80);
        bg.setFill(Color.WHITE);
        bg.setArcWidth(15);
        bg.setArcHeight(15);
        bg.setStroke(Color.BLACK);
        bg.setStrokeWidth(2);

        // 根据点数生成黑点布局
        VBox dots = createDots(number);

        pane.getChildren().addAll(bg, dots);
        return pane;
    }
    /**
     * 根据骰子点数创建点阵布局
     *
     * @param number 骰子点数（1~6）
     * @return VBox 包含对应点阵
     */
    private VBox createDots(int number) {
        VBox container = new VBox(8);
        container.setAlignment(Pos.CENTER);
        /*各个点阵布局*/
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
    /**
     * 创建单个骰子黑点
     *
     * @return Circle 表示一个点
     */
    private Circle createDot() {
        Circle dot = new Circle(6);
        dot.setFill(Color.BLACK);
        return dot;
    }
    /**
     * 掷骰子主逻辑
     * ------------------------------------------------
     * 1. 播放骰子滚动动画
     * 2. 随机生成玩家点数
     * 3. 延迟后生成机器人点数
     * 4. 比较结果并更新比分
     */
    private void rollDice() {
        // 防止动画过程中重复点击
        rollButton.setDisable(true);
        resultLabel.setText("投掷中...");

        // 骰子滚动动画（快速变化点数）
        Timeline animation = new Timeline(
                new KeyFrame(Duration.millis(100), e -> {
                    int dice = (int) (Math.random() * 6) + 1;
                    updateDiceView(dice);
                })
        );
        animation.setCycleCount(10);
        // 动画结束后的逻辑
        animation.setOnFinished(e -> {
            // 玩家最终骰子点数
            int playerRoll = (int) (Math.random() * 6) + 1;
            updateDiceView(playerRoll);
            // 模拟机器人“思考”延迟
            Timeline robotDelay = new Timeline(
                    new KeyFrame(Duration.millis(800), event -> {
                        int robotRoll = (int) (Math.random() * 6) + 1;
                        String result;
                        // 判断胜负并累计分数
                        if (playerRoll > robotRoll) {
                            playerScore++;
                            result = "你赢了！ 你:" + playerRoll + " vs 机器人:" + robotRoll;
                        } else if (playerRoll < robotRoll) {
                            robotScore++;
                            result = "机器人赢了！ 你:" + playerRoll + " vs 机器人:" + robotRoll;
                        } else {
                            result = "平局！ 都是 " + playerRoll + " 点";
                        }
                        // 更新界面显示
                        resultLabel.setText(result);
                        scoreLabel.setText("你: " + playerScore + "  vs  机器人: " + robotScore);
                        // 重新启用按钮
                        rollButton.setDisable(false);
                    })
            );
            robotDelay.play();
        });
        animation.play();
    }
    /**
     * 更新骰子显示
     *
     * @param number 新的骰子点数
     */
    private void updateDiceView(int number) {
        StackPane newDice = createDiceView(number);
        VBox centerBox = (VBox) root.getCenter();
        // 替换原有骰子视图
        centerBox.getChildren().set(0, newDice);
        dicePane = newDice;
    }
    /**
     * 获取游戏根节点
     *
     * @return BorderPane 根布局
     */
    public BorderPane getRoot() {
        return root;
    }
}
