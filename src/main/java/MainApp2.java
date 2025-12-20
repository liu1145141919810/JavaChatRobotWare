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

public class MainApp2 extends MainApp {

    private MediaPlayer mediaPlayer;

    @Override
    public void start(Stage stage) {

        // 1. 指定媒体文件路径（改成你自己的）
        File mediaFile = new File("E:\\Code\\Gitbox\\JAVA\\practice\\resource\\Genshin.mp4"); 
        // 也可以是 test.mp3

        Media media = new Media(mediaFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnReady(() -> {
            mediaPlayer.play();
        });
        // 设置循环播放
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);

        // 2. 视频显示组件（音频播放也可以有，但不会显示画面）
        MediaView mediaView = new MediaView(mediaPlayer);
        mediaView.fitWidthProperty().bind(stage.widthProperty());
        mediaView.fitHeightProperty().bind(stage.heightProperty());
        mediaView.setPreserveRatio(true);
        mediaView.setPreserveRatio(true);

        // 3. 控制按钮
        mediaView.setOnMouseClicked(e -> {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                mediaPlayer.stop();
                mediaPlayer.dispose();
                start_main(stage);
            }
        });

        // 4. 布局
        VBox root = new VBox(10);
        root.getChildren().addAll(mediaView);

        // 5. 场景与窗口
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("原神启动 - 点击视频进入聊天界面");
        stage.setScene(scene);

        stage.show();
    }
    private VBox createRobotDisplay(VBox originalRoot) {
        VBox displayBox = new VBox(20);
        displayBox.setAlignment(Pos.CENTER);
        displayBox.setPadding(new Insets(30));

        // 获取原始图片组件
        ImageView originalImage = null;
        for (javafx.scene.Node node : originalRoot.getChildren()) {
            if (node instanceof ImageView) {
                originalImage = (ImageView) node;
                break;
            }
        }

        if (originalImage != null) {
            // 创建相框效果
            StackPane framePane = new StackPane();

            // 木板相框背景
            Pane woodFrame = new Pane();
            woodFrame.setStyle("-fx-background-color: linear-gradient(to bottom right, #8B4513, #A0522D, #8B4513);" +
                    "-fx-background-radius: 15;" +
                    "-fx-border-color: #654321;" +
                    "-fx-border-width: 10;" +
                    "-fx-border-radius: 15;");
            woodFrame.setPrefSize(300, 400);

            // 调整机器人图片大小
            originalImage.setFitWidth(220);
            originalImage.setFitHeight(330);
            originalImage.setPreserveRatio(true);

            // 添加到相框
            framePane.getChildren().addAll(woodFrame, originalImage);

            // 添加标题
            Label titleLabel = new Label("CYBER-BIOLOGY UNIT");
            titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 26));
            titleLabel.setTextFill(Color.CYAN);
            titleLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,255,255,0.8), 10, 0, 0, 0);");

            displayBox.getChildren().addAll(titleLabel, framePane);
        }

        return displayBox;
    }
    public void start_main(Stage primaryStage) {
        super.start(primaryStage);
        Scene originalScene = primaryStage.getScene();
        VBox originalRoot = (VBox) originalScene.getRoot();

        // 创建新的主布局
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20, 20, 20, 20));

        // 设置外层边框效果
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #0f0c29, #302b63, #24243e);" +
                "-fx-border-color: #00ffff;" +
                "-fx-border-width: 3;" +
                "-fx-border-radius: 10;");

        // 创建中间内容区域
        StackPane centerPane = new StackPane();
        centerPane.setPadding(new Insets(15));

        // 中间区域背景
        centerPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #ff00ff;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 10;");

        // 创建机器人显示区域，带相框
        VBox robotDisplay = createRobotDisplay(originalRoot);
        centerPane.getChildren().add(robotDisplay);

        // 创建控制面板
        VBox controlPanel = createControlPanel(originalRoot);

        // 创建作者信息栏
        HBox authorBar = createAuthorBar();

        // 把各个部分放到主布局中
        mainLayout.setCenter(centerPane);
        mainLayout.setTop(controlPanel);
        BorderPane.setMargin(controlPanel, new Insets(0, 0, 20, 0));
        mainLayout.setBottom(authorBar);
        BorderPane.setAlignment(authorBar, Pos.BOTTOM_RIGHT);

        // 创建新场景
        Scene newScene = new Scene(mainLayout, 650, 750);
        primaryStage.setScene(newScene);
        primaryStage.setTitle("第二小组Chat bot - 美化版");
    }
    // 创建控制面板
    private VBox createControlPanel(VBox originalRoot) {
        VBox controlBox = new VBox(15);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setPadding(new Insets(20));
        controlBox.setStyle("-fx-background-color: rgba(40, 40, 80, 0.5);" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: linear-gradient(to right, #00ffff, #ff00ff);" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 10;");

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

        // 创建组件列表，用于后面从originalRoot中移除
        List<Node> nodesToRemove = new ArrayList<>();
        if (choiceBox != null) {
            nodesToRemove.add(choiceBox);
            // 美化选择框
            choiceBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8);" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 14;" +
                    "-fx-font-weight: bold;" +
                    "-fx-border-color: #00ffff;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 5;" +
                    "-fx-padding: 5 10;");
        }

        if (selectLabel != null) {
            nodesToRemove.add(selectLabel);
            // 美化标签
            selectLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
            selectLabel.setTextFill(Color.WHITE);
        }

        if (startBtn != null) {
            nodesToRemove.add(startBtn);
            // 美化开始按钮
            startBtn.setText("启 动 系 统");
            startBtn.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
            startBtn.setPrefSize(200, 45);
            startBtn.setStyle("-fx-background-color: linear-gradient(to right, #00ffff, #0080ff);" +
                    "-fx-text-fill: black;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 25;" +
                    "-fx-border-color: white;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 25;");

            // 鼠标悬停效果
            Button finalStartBtn = startBtn;//final副本并非对象本身的副本，而是类似C++里边的指针，即对象引用（指针）的副本
            startBtn.setOnMouseEntered(e -> {
                finalStartBtn.setStyle("-fx-background-color: linear-gradient(to right, #00ffaa, #0080ff);" +
                        "-fx-text-fill: black;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 25;" +
                        "-fx-border-color: #ffff00;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 25;");
            });

            Button finalStartBtn1 = startBtn;
            startBtn.setOnMouseExited(e -> {
                finalStartBtn1.setStyle("-fx-background-color: linear-gradient(to right, #00ffff, #0080ff);" +
                        "-fx-text-fill: black;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 25;" +
                        "-fx-border-color: white;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 25;");
            });
        }

        // 从原布局中移除这些组件，防止可能会抛出的ConcurrentModificationException
        originalRoot.getChildren().removeAll(nodesToRemove);

        // 将组件添加到controlBox中
        if (selectLabel != null) {
            controlBox.getChildren().add(selectLabel);
        }
        if (choiceBox != null) {
            controlBox.getChildren().add(choiceBox);
        }
        if (startBtn != null) {
            controlBox.getChildren().add(startBtn);
        }

        // 返回创建的控制面板
        return controlBox;

    }

    // 创建作者信息栏
    private HBox createAuthorBar () {
        HBox authorBox = new HBox(10);
        authorBox.setAlignment(Pos.CENTER_RIGHT);
        authorBox.setPadding(new Insets(15));
        authorBox.setStyle("-fx-background-color: rgba(30, 30, 60, 0.7);" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #00ffff;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 8;");

        // 作者信息
        VBox infoBox = new VBox(5);

        Label teamLabel = new Label("桌面机器人");
        teamLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        teamLabel.setTextFill(Color.LIGHTGREEN);

        Label memberLabel = new Label("成员：刘栋旭、梁志僮、李林润");
        memberLabel.setFont(Font.font("Microsoft YaHei", 12));
        memberLabel.setTextFill(Color.LIGHTGRAY);

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
