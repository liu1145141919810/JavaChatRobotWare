import com.desktoprobot.util.DatabaseConnection;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JavaFX 版本的聊天窗口，替换原 Swing 实现。
 */
public class RobotChatFrame extends Stage {
    protected String robotName;
    protected VBox chatBox;
    protected TextField inputField;
    protected Button sendButton;
    protected Button backButton;
    protected Runnable onBack;
    protected int param; // 机器人参数 ID
    protected int sessionId; // 聊天会话ID，用于关联同一次聊天的多条记录

    protected Image userImage;
    protected Image robotImage;
    protected ScrollPane scrollPane;

    public RobotChatFrame(String robotName) {
        this.robotName = robotName;
        this.param = -1;
        this.sessionId = -1;
        setupUI();
    }

    public RobotChatFrame(String robotName, AtomicInteger id, Runnable onBack) {
        this.robotName = robotName;
        this.onBack = onBack;
        this.param = id != null ? id.get() : -1;

        // 加载头像
        this.robotImage = loadImageSafe("Robot_App/Robot" + this.param + "/static/figure.jpg");
        this.userImage = loadImageSafe("Images/user.jpg");

        // 初始化数据库会话
        this.sessionId = createChatSession();

        setupUI();

        // 加载历史消息
        loadHistory(50);
    }

    /** 创建新的聊天会话并返回会话ID */
    protected int createChatSession() {
        if (param < 0) return -1;
        String sql = "INSERT INTO chat_sessions (robot_id, robot_name, start_time) VALUES (?, ?, ?) RETURNING session_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, this.param);
            pstmt.setString(2, this.robotName);
            pstmt.setTimestamp(3, Timestamp.from(Instant.now()));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("session_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("创建聊天会话失败: " + e.getMessage());
        }
        return -1;
    }

    protected void setupUI() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // 顶部
        root.setTop(createTopBar());

        // 中部聊天区
        chatBox = new VBox(8);
        chatBox.setFillWidth(true);
        chatBox.setPadding(new Insets(5, 5, 5, 5));

        scrollPane = new ScrollPane(chatBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        root.setCenter(scrollPane);

        // 底部输入区
        root.setBottom(createBottomBar());

        Scene scene = new Scene(root, 520, 640);
        setTitle("与" + robotName + "聊天");
        setScene(scene);

        setOnCloseRequest(this::handleWindowClose);
    }

    protected void handleWindowClose(WindowEvent event) {
        updateSessionEndTime();
        if (onBack != null) {
            try { onBack.run(); } catch (Exception ex) { ex.printStackTrace(); }
        }
        // 默认允许关闭
    }

    /** 顶部栏，子类可重写 */
    protected HBox createTopBar() {
        backButton = new Button("返回");
        backButton.setOnAction(e -> {
            if (onBack != null) {
                try { onBack.run(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            close();
        });

        ImageView avatar = createAvatarView(robotImage, 50, 70);
        Label nameLabel = new Label(robotName);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        HBox top = new HBox(10, backButton, avatar, nameLabel);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(10));
        top.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: transparent transparent #ddd transparent;");
        return top;
    }

    /** 底部输入栏 */
    protected HBox createBottomBar() {
        inputField = new TextField();
        inputField.setPromptText("请输入消息...");

        sendButton = new Button("发送");
        sendButton.setDefaultButton(true);
        sendButton.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());

        HBox bottom = new HBox(8, inputField, sendButton);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10));
        HBox.setHgrow(inputField, Priority.ALWAYS);
        return bottom;
    }

    /** 发送消息和机器人回复 */
    protected void sendMessage() {
        String message = inputField.getText();
        if (message == null) message = "";
        message = message.trim();
        if (message.isEmpty()) return;

        String userMsg = message;
        inputField.clear();
        addMessage(userMsg, true);
        saveMessageToDatabase(userMsg, "user");

        // 异步生成机器人回复
        new Thread(() -> {
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            String reply = generateRobotReply(userMsg);
            Platform.runLater(() -> addMessage(reply, false));
            saveMessageToDatabase(reply, "robot");
        }).start();
    }

    /** 添加一条消息到聊天区 */
    protected void addMessage(String message, boolean isUser) {
        Platform.runLater(() -> {
            HBox line = new HBox(8);
            line.setAlignment(isUser ? Pos.TOP_RIGHT : Pos.TOP_LEFT);

            ImageView avatar = createAvatarView(isUser ? userImage : robotImage, 70, 95);

            Label bubble = new Label(message);
            bubble.setWrapText(true);
            bubble.setMaxWidth(360);
            bubble.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

            VBox bubbleBox = new VBox(bubble);
            bubbleBox.setPadding(new Insets(8, 12, 8, 12));
            bubbleBox.setMaxWidth(380);
            bubbleBox.setStyle(isUser ? "-fx-background-color: #b4e1ff; -fx-background-radius: 12;" : "-fx-background-color: #f0f0f0; -fx-background-radius: 12;");

            if (isUser) {
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                line.getChildren().addAll(spacer, bubbleBox, avatar);
            } else {
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                line.getChildren().addAll(avatar, bubbleBox, spacer);
            }

            chatBox.getChildren().add(line);
            // 滚动到底部
            scrollPane.layout();
            scrollPane.setVvalue(1.0);
        });
    }

    /** 加载历史消息 */
    protected void loadHistory(int limit) {
        if (sessionId == -1) {
            System.out.println("会话ID无效，跳过加载历史消息");
            return;
        }

        String sql = "SELECT cm.message_content, cm.sender_type " +
                "FROM chat_messages cm " +
                "JOIN chat_sessions cs ON cm.session_id = cs.session_id " +
                "WHERE cs.robot_id = ? " +
                "ORDER BY cm.timestamp ASC LIMIT ?";

        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, this.param);
                pstmt.setInt(2, limit);

                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    String content = rs.getString("message_content");
                    String senderType = rs.getString("sender_type");
                    boolean isUser = "user".equals(senderType);
                    addMessage(content, isUser);
                }
                System.out.println("历史消息加载完成");
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("加载历史消息失败: " + e.getMessage());
            }
        }).start();
    }

    /** 保存消息到数据库 */
    protected void saveMessageToDatabase(String message, String senderType) {
        if (sessionId == -1) return;
        String sql = "INSERT INTO chat_messages (session_id, sender_type, message_content, timestamp) VALUES (?, ?, ?, ?)";

        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, sessionId);
                pstmt.setString(2, senderType);
                pstmt.setString(3, message);
                pstmt.setTimestamp(4, Timestamp.from(Instant.now()));
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("保存消息失败: " + e.getMessage());
            }
        }).start();
    }

    /** 生成机器人回复（可被子类覆盖） */
    protected String generateRobotReply(String userMessage) {
        String[] replies = {
                "这是一个很有趣的问题！",
                "让我想想怎么回答你...",
                "我明白了，你能告诉我更多吗？",
                "关于" + userMessage + "，我的看法是...",
                "谢谢你的分享！"
        };
        int index = userMessage.length() % replies.length;
        return replies[index];
    }

    /** 更新会话结束时间 */
    protected void updateSessionEndTime() {
        if (sessionId == -1) return;
        String sql = "UPDATE chat_sessions SET end_time = ? WHERE session_id = ?";
        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setTimestamp(1, Timestamp.from(Instant.now()));
                pstmt.setInt(2, sessionId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("更新会话结束时间失败: " + e.getMessage());
            }
        }).start();
    }

    /** 加载图片，失败返回空占位 */
    protected Image loadImageSafe(String path) {
        try {
            FileInputStream fis = new FileInputStream(path);
            return new Image(fis);
        } catch (Exception ignored) {
            // 尝试 classpath
            try (InputStream is = getClass().getResourceAsStream("/" + path)) {
                if (is != null) return new Image(is);
            } catch (Exception ignored2) { }
        }
        // 占位透明图
        Rectangle r = new Rectangle(1, 1, Color.TRANSPARENT);
        return r.snapshot(null, null);
    }

    protected ImageView createAvatarView(Image img, double w, double h) {
        ImageView iv = new ImageView(img);
        iv.setFitWidth(w);
        iv.setFitHeight(h);
        iv.setPreserveRatio(true);
        return iv;
    }

    // 测试入口（独立运行）
    public static void main(String[] args) {
        javafx.application.Application.launch(TestChatApp.class, args);
    }
}

/**
 * 简单测试用 JavaFX Application，便于直接运行。
 */
class TestChatApp extends javafx.application.Application {
    @Override
    public void start(Stage stage) {
        RobotChatFrame2 frame = new RobotChatFrame2("小助手");
        frame.show();
    }
}
