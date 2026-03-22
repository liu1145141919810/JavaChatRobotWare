import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Param
 * --------------------------------------------------
 * 机器人参数实体类
 *
 * 用于封装：
 * 1. 机器人显示名称
 * 2. 机器人唯一编号 ID
 * 3. 机器人资源路径（头像、配置文件等）
 */
class Param {
    /** 机器人名称 */
    private String name;
    /** 机器人 ID */
    private int id;
    /** 机器人资源目录路径 */
    private String Parapath;
    /**
     * 构造方法
     *
     * @param name     机器人名称
     * @param id       机器人编号
     * @param Parapath 机器人资源路径
     */
    public Param(String name, int id, String Parapath) {
        this.name = name;
        this.id = id;
        this.Parapath = Parapath;
    }
    /** @return 机器人名称 */
    public String getName() {
        return name;
    }
    /** @return 机器人编号 */
    public int getId() {
        return id;
    }
    /** @return 机器人资源路径 */
    public String getParapath() {
        return Parapath;
    }
}
/**
 * MainApp
 * --------------------------------------------------
 * JavaFX 程序主入口类
 *
 * 功能说明：
 * 1. 提供机器人选择界面（ChoiceBox）
 * 2. 根据选择动态切换机器人头像
 * 3. 点击“开始”进入对应机器人的聊天窗口
 *
 * 设计说明：
 * - 使用 JavaFX Application 作为程序入口
 * - 使用 ChoiceBox 实现机器人选择
 * - 使用 ImageView 动态展示机器人形象
 */
public class MainApp extends Application {
    /** 机器人参数列表（相当于配置表） */
    private ArrayList<Param> ChatParam;
    /** 名称 -> ID 的映射表，方便通过名称快速获取机器人编号 */
    private HashMap<String, Integer> map;
    /**
     * JavaFX 程序主入口
     */
    public static void main(String[] args) {
        launch(args);
    }
    /**
     * 构造方法
     * ------------------------------------------------
     * 初始化机器人参数列表与映射表
     */
    public MainApp() {
        super();
        ChatParam = new ArrayList<>();
        // 初始化机器人参数
        // 注意：路径需指向项目根目录下的资源文件
        ChatParam.add(new Param( "牢大",1,String.format("src/main/resources/images/Robot_APP/Robot%d", 1)));
        ChatParam.add(new Param("喜多",2,String.format("src/main/resources/images/Robot_APP/Robot%d", 2)));
        // 构建名称到 ID 的映射
        map = new HashMap<>();
        for (Param param : ChatParam) {
            map.put(param.getName(), param.getId());
        }
    }
    /**
     * JavaFX 启动方法
     *
     * @param primaryStage 主舞台
     */
    @Override
    public void start(Stage primaryStage) {
        // 设置：关闭子窗口时不自动退出整个应用
        Platform.setImplicitExit(false);
        // ====== 机器人选择框 ======
        ChoiceBox<String> choice = new ChoiceBox<>(
                FXCollections.observableArrayList(
                        ChatParam.stream()
                                .map(Param::getName) // 提取机器人名称
                                .collect(Collectors.toList())
                )
        );
        // 默认选中第一个机器人
        choice.setValue(ChatParam.get(0).getName());
        // 显示当前选择的机器人
        Label selectionLabel = new Label("已选择: " + choice.getValue());
        // 启动聊天按钮
        Button btn = new Button("开始");
        // ====== 主布局（垂直居中） ======
        VBox root = new VBox(12, choice, selectionLabel, btn);
        root.setAlignment(Pos.CENTER);
        // ====== 机器人头像显示 ======
        Image image = new Image(
                "file:" + ChatParam.get(0).getParapath() + "/static/figure.jpg"
        );
        System.out.println(
                "file:" + ChatParam.get(0).getParapath() + "/static/figure.jpg"
        );
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(110);
        imageView.setFitHeight(160);
        root.getChildren().add(imageView);
        // 使用 AtomicInteger 保存当前机器人 ID（供 Lambda 使用）
        AtomicInteger id = new AtomicInteger(1);
        // ====== 监听机器人选择变化 ======
        choice.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    // 更新标签显示
                    selectionLabel.setText("已选择: " + newVal);
                    // 根据名称获取对应 ID
                    id.set(map.get(newVal));
                    // 加载新机器人的头像
                    Image imagenew = new Image("file:" +
                            ChatParam.get(id.get() - 1).getParapath() +
                            "/static/figure.jpg");
                    System.out.println("file:" +
                            ChatParam.get(id.get() - 1).getParapath() +
                            "/static/figure.jpg"
                    );
                    // 更新 ImageView 显示
                    imageView.setImage(imagenew);
                });
        // ====== 点击“开始”按钮 ======
        btn.setOnAction(e -> {
            String selected = choice.getValue();
            // 隐藏主窗口（不退出程序）
            primaryStage.hide();
            // 创建并显示聊天窗口
            RobotChatFrame3 frame = new RobotChatFrame3(
                    selected,
                    id,
                    () -> {// 子窗口关闭时回到主界面
                    Platform.runLater(primaryStage::show);});
            frame.show();});
        // ====== 主窗口关闭事件 ======
        primaryStage.setOnCloseRequest(event -> {
            // 完全退出 JavaFX 应用
            Platform.exit();
        });
        // ====== 场景与舞台设置 ======
        Scene scene = new Scene(root, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("第二小组Chat bot");
        // 注意：此处未直接 show，可由外部控制
        // primaryStage.show();
    }
}
