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
class Param{
    private String name;
    private int id;
    private String Parapath;
    public Param(String name, int id,String Parapath) {
        this.name = name;
        this.id = id;
        this.Parapath = Parapath;
    }
    public String getName() {
        return name;
    }
    public int getId() {
        return id;
    }
    public String getParapath() {
        return Parapath;
    }
}
public class MainApp extends Application {
    private ArrayList<Param> ChatParam;//机器人参数表
    private HashMap<String,Integer> map;

    public static void main(String[] args) {
        launch(args);
    }


    public MainApp(){
        super();
        ChatParam = new ArrayList<>();
        // 初始化机器人参数
        ChatParam.add(new Param("牢大", 1,String.format("src/main/resources/images/Robot_APP/Robot%d", 1)));//路径要拓展到项目根目录才行
        ChatParam.add(new Param("喜多",2,String.format("src/main/resources/images/Robot_APP/Robot%d", 2)));
        map = new HashMap<>();
        for (Param param : ChatParam) {
            map.put(param.getName(), param.getId());
        }
    }
   

    @Override
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(false);
        // 创建选择列表（ChoiceBox）和显示标签
        ChoiceBox<String> choice = new ChoiceBox<>(FXCollections.observableArrayList(
             ChatParam.stream()
                     .map(Param::getName)  // 获取每个 Param 的 name
                     .collect(Collectors.toList()) // 收集到一个列表中
            ));
        choice.setValue(ChatParam.get(0).getName());//初始设置为第一个
         
        // 根据选择的名称获取对应的 ID
        Label selectionLabel = new Label("已选择: " + choice.getValue());
         // 启动按钮
        Button btn = new Button("开始");

         // 使用 VBox 居中布局
        VBox root = new VBox(12, choice, selectionLabel, btn);
        root.setAlignment(Pos.CENTER);
        Image image=new Image("file:"+ChatParam.get(0).getParapath()+"/static/figure.jpg");
        System.out.println("file:"+ChatParam.get(0).getParapath()+"/static/figure.jpg");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(110);
        imageView.setFitHeight(160);
        root.getChildren().add(imageView);
        AtomicInteger id = new AtomicInteger(1);

        choice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectionLabel.setText("已选择: " + newVal);
            id.set(map.get(newVal)); // 使用新选择的名称获取 ID
            Image imagenew = new Image("file:" + ChatParam.get(id.get() - 1).getParapath() + "/static/figure.jpg"); // 注意 id - 1，因为数组索引从 0 开始
            System.out.println("file:" + ChatParam.get(id.get() - 1).getParapath() + "/static/figure.jpg");
            // 更新 imageView 显示的图片
            imageView.setImage(imagenew);
        });
        btn.setOnAction(e -> {
            String selected = choice.getValue();
            // 隐藏主窗口但不退出
            primaryStage.hide();

            // 直接创建 JavaFX 版本的聊天窗口
            RobotChatFrame3 frame = new RobotChatFrame3(selected, id, () -> {
                // 回到 JavaFX 主舞台
                Platform.runLater(primaryStage::show);
            });
            frame.show();
        });
        primaryStage.setOnCloseRequest(event -> {
            // 允许正常关闭；如需全局退出可调用 Platform.exit()
            Platform.exit();
        });
        //Scene展示
        Scene scene = new Scene(root, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("第二小组Chat bot");
        //primaryStage.show();
    }
}
