import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.Node;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * MainApp2
 * --------------------------------------------------
 * 主界面美化版本（继承自 MainApp）
 *
 * 功能说明：
 * 1. 增加视频开场动画（可点击跳过）
 * 2. 对原 MainApp 的 UI 进行整体重构与美化
 * 3. 保留原有逻辑，仅复用并重新布局原组件
 *
 * 设计思想：
 * - 通过继承复用原 MainApp 的业务逻辑
 * - 不重复造轮子，只做 UI 层的包装与增强
 */
public class MainApp2 extends MainApp {

    /** 开场视频播放器 */
    private MediaPlayer mediaPlayer;

    /**
     * JavaFX 启动方法
     * ------------------------------------------------
     * 优先尝试播放开场视频
     * 若视频加载失败，则直接进入主界面
     */
    @Override
    public void start(Stage stage) {
        boolean mediaLoaded = false;
        try {
            // 加载本地视频资源
            File mediaFile = new File(".\\practice\\resource\\Genshin.mp4");
            if (mediaFile.exists()) {
                Media media = new Media(mediaFile.toURI().toString());
                mediaPlayer = new MediaPlayer(media);

                // 循环播放视频
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.setOnReady(() -> mediaPlayer.play());

                // 视频显示组件
                MediaView mediaView = new MediaView(mediaPlayer);
                mediaView.fitWidthProperty().bind(stage.widthProperty());
                mediaView.fitHeightProperty().bind(stage.heightProperty());
                mediaView.setPreserveRatio(true);

                // 点击视频进入主界面
                mediaView.setOnMouseClicked(e -> {
                    if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                        mediaPlayer.stop();
                        mediaPlayer.dispose();
                    }
                    start_main(stage);
                });

                // 简单布局，仅显示视频
                VBox root = new VBox(10);
                root.getChildren().add(mediaView);
                Scene scene = new Scene(root, 800, 600);

                stage.setTitle("原神启动 - 点击视频进入聊天界面");
                stage.setScene(scene);

                mediaLoaded = true;
            }
        } catch (Exception e) {
            System.err.println("视频资源无法加载，跳过开场动画：" + e.getMessage());
        }

        // 如果视频加载失败，直接进入主界面
        if (!mediaLoaded) {
            start_main(stage);
        }
        stage.show();
    }

    /**
     * 创建机器人展示区域（相框样式）
     *
     * @param originalRoot 原 MainApp 中的根布局
     * @return VBox 机器人展示区域
     */
    private VBox createRobotDisplay(VBox originalRoot) {
        VBox displayBox = new VBox(20);
        displayBox.setAlignment(Pos.CENTER);
        displayBox.setPadding(new Insets(30));

        // 从原始布局中提取 ImageView（机器人头像）
        ImageView originalImage = null;
        for (javafx.scene.Node node : originalRoot.getChildren()) {
            if (node instanceof ImageView) {
                originalImage = (ImageView) node;
                break;
            }
        }

        if (originalImage != null) {
            // 相框容器
            StackPane framePane = new StackPane();

            // 木质风格相框背景
            Pane woodFrame = new Pane();
            woodFrame.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #8B4513, #A0522D, #8B4513);" +
                    "-fx-background-radius: 15;" +
                    "-fx-border-color: #654321;" +
                    "-fx-border-width: 10;" +
                    "-fx-border-radius: 15;"
            );
            woodFrame.setPrefSize(300, 400);

            // 调整机器人图片大小
            originalImage.setFitWidth(220);
            originalImage.setFitHeight(330);
            originalImage.setPreserveRatio(true);

            // 放入相框
            framePane.getChildren().addAll(woodFrame, originalImage);

            // 顶部标题
            Label titleLabel = new Label("CYBER-BIOLOGY UNIT");
            titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 26));
            titleLabel.setTextFill(Color.CYAN);
            titleLabel.setStyle(
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,255,255,0.8), 10, 0, 0, 0);"
            );

            displayBox.getChildren().addAll(titleLabel, framePane);
        }

        return displayBox;
    }

    /**
     * 构建并展示主界面（在原 MainApp 基础上重新布局）
     */
    public void start_main(Stage primaryStage) {
        // 先调用父类 start，生成原始界面
        super.start(primaryStage);

        Scene originalScene = primaryStage.getScene();
        VBox originalRoot = (VBox) originalScene.getRoot();

        // 主布局（BorderPane）
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20, 20, 20, 20));

        // 外层整体风格
        mainLayout.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #0f0c29, #302b63, #24243e);" +
                "-fx-border-color: #00ffff;" +
                "-fx-border-width: 3;" +
                "-fx-border-radius: 10;"
        );

        // 中央显示区域
        StackPane centerPane = new StackPane();
        centerPane.setPadding(new Insets(15));
        centerPane.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.3);" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #ff00ff;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 10;"
        );

        // 机器人展示区域
        VBox robotDisplay = createRobotDisplay(originalRoot);
        centerPane.getChildren().add(robotDisplay);

        // 控制面板（选择 + 启动）
        VBox controlPanel = createControlPanel(originalRoot);

        // 作者信息栏
        HBox authorBar = createAuthorBar();

        // 布局放置
        mainLayout.setCenter(centerPane);
        mainLayout.setTop(controlPanel);
        BorderPane.setMargin(controlPanel, new Insets(0, 0, 20, 0));
        mainLayout.setBottom(authorBar);
        BorderPane.setAlignment(authorBar, Pos.BOTTOM_RIGHT);

        // 应用新场景
        Scene newScene = new Scene(mainLayout, 650, 750);
        primaryStage.setScene(newScene);
        primaryStage.setTitle("第二小组Chat bot - 美化版");
    }

    /**
     * 创建顶部控制面板
     * （复用原 MainApp 的 ChoiceBox / Label / Button）
     */
    private VBox createControlPanel(VBox originalRoot) {
        VBox controlBox = new VBox(15);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setPadding(new Insets(20));
        controlBox.setStyle(
                "-fx-background-color: rgba(40, 40, 80, 0.5);" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: linear-gradient(to right, #00ffff, #ff00ff);" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 10;"
        );

        // 从原布局中提取组件
        ChoiceBox<String> choiceBox = null;
        Label selectLabel = null;
        Button startBtn = null;

        for (javafx.scene.Node node : originalRoot.getChildren()) {
            if (node instanceof ChoiceBox) {
                choiceBox = (ChoiceBox<String>) node;
            } else if (node instanceof Label && !(node instanceof Button)) {
                selectLabel = (Label) node;
            } else if (node instanceof Button) {
                startBtn = (Button) node;
            }
        }

        // 记录待移除节点，避免并发修改异常
        List<Node> nodesToRemove = new ArrayList<>();

        if (choiceBox != null) {
            nodesToRemove.add(choiceBox);
            choiceBox.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 0.8);" +
                    "-fx-font-size: 14;" +
                    "-fx-font-weight: bold;" +
                    "-fx-border-color: #00ffff;" +
                    "-fx-border-radius: 5;"
            );
        }

        if (selectLabel != null) {
            nodesToRemove.add(selectLabel);
            selectLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
            selectLabel.setTextFill(Color.WHITE);
        }

        if (startBtn != null) {
            nodesToRemove.add(startBtn);
            startBtn.setText("启 动 系 统");
            startBtn.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
            startBtn.setPrefSize(200, 45);

            // 鼠标悬停效果（引用语义说明）
            Button finalStartBtn = startBtn;
            startBtn.setOnMouseEntered(e -> {
                finalStartBtn.setStyle(
                        "-fx-background-color: linear-gradient(to right, #00ffaa, #0080ff);" +
                        "-fx-border-color: #ffff00;"
                );
            });

            Button finalStartBtn1 = startBtn;
            startBtn.setOnMouseExited(e -> {
                finalStartBtn1.setStyle(
                        "-fx-background-color: linear-gradient(to right, #00ffff, #0080ff);" +
                        "-fx-border-color: white;"
                );
            });
        }

        // 从原布局中移除组件
        originalRoot.getChildren().removeAll(nodesToRemove);

        // 添加到新控制面板
        if (selectLabel != null) controlBox.getChildren().add(selectLabel);
        if (choiceBox != null) controlBox.getChildren().add(choiceBox);
        if (startBtn != null) controlBox.getChildren().add(startBtn);

        return controlBox;
    }

    /**
     * 创建底部作者信息栏
     */
    private HBox createAuthorBar () {
        // 作者信息区域
        HBox authorBox = new HBox(10);
        authorBox.setAlignment(Pos.CENTER_RIGHT);
        authorBox.setPadding(new Insets(15));
        authorBox.setStyle(
                "-fx-background-color: rgba(30, 30, 60, 0.7);" +
                "-fx-border-color: #00ffff;" +
                "-fx-border-radius: 8;"
        );

        VBox infoBox = new VBox(5);
        // 团队名称
        Label teamLabel = new Label("桌面机器人");
        teamLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        teamLabel.setTextFill(Color.LIGHTGREEN);
        // 成员名单
        Label memberLabel = new Label("成员：刘栋旭、梁志僮、李林润");
        memberLabel.setFont(Font.font("Microsoft YaHei", 12));
        memberLabel.setTextFill(Color.LIGHTGRAY);
        // 课程信息
        Label courseLabel = new Label("Java期末大作业");
        courseLabel.setFont(Font.font("Microsoft YaHei", 10));
        courseLabel.setTextFill(Color.GRAY);

        infoBox.getChildren().addAll(teamLabel, memberLabel, courseLabel);
        authorBox.getChildren().add(infoBox);

        return authorBox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
