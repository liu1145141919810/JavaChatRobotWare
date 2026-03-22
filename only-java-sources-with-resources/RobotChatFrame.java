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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JavaFX 版本的聊天窗口
 * 负责聊天界面展示、消息发送、数据库交互
 */
public class RobotChatFrame extends Stage {

    /** 机器人名称，用于界面显示 */
    protected String robotName;

    /** 聊天消息容器，存放所有聊天气泡 */
    protected VBox chatBox;

    /** 消息输入框 */
    protected TextField inputField;

    /** 发送按钮 */
    protected Button sendButton;

    /** 返回按钮，返回上一级界面 */
    protected Button backButton;

    /** 返回主界面的回调函数 */
    protected Runnable onBack;

    /** 机器人参数 ID（可用于区分不同机器人） */
    protected int param;

    /** 当前聊天会话 ID（在数据库中对应一条会话记录） */
    protected int sessionId;

    /** 用户头像 Image 对象 */
    protected Image userImage;

    /** 机器人头像 Image 对象 */
    protected Image robotImage;

    /** 滚动面板，用于放置 chatBox 并支持滚动 */
    protected ScrollPane scrollPane;

    /**
     * 构造函数（仅用于测试或无参数情况）
     * @param robotName 机器人名称
     */
    public RobotChatFrame(String robotName) {
        this.robotName = robotName;
        this.param = -1;    // 默认无机器人参数
        this.sessionId = -1; // 默认无数据库会话
        setupUI();          // 初始化 UI
    }

    /**
     * 主构造函数，带机器人参数 ID 和返回回调
     * @param robotName 机器人名称
     * @param id        机器人参数 ID（AtomicInteger，可为空）
     * @param onBack    返回主界面回调
     */
    public RobotChatFrame(String robotName, AtomicInteger id, Runnable onBack) {
        this.robotName = robotName;
        this.onBack = onBack;
        this.param = id != null ? id.get() : -1;

        // 尝试加载机器人头像，如果失败使用透明占位
        this.robotImage = loadImageSafe("Robot_App/Robot" + this.param + "/static/figure.jpg");
        // 尝试加载用户头像
        this.userImage = loadImageSafe("Images/user.jpg");

        // 在数据库中创建一条聊天会话记录
        this.sessionId = createChatSession();

        setupUI();          // 初始化 UI 布局

        // 加载最近 50 条历史聊天记录
        loadHistory(50);
    }

    /**
     * 在数据库中创建一条聊天会话记录
     * @return session_id 成功返回新会话 ID，失败返回 -1
     */
    protected int createChatSession() {
        if (param < 0) return -1;

        String sql =
                "INSERT INTO chat_sessions (robot_id, robot_name, start_time) " +
                "VALUES (?, ?, ?) RETURNING session_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, this.param);                        // 设置机器人 ID
            pstmt.setString(2, this.robotName);                // 设置机器人名称
            pstmt.setTimestamp(3, Timestamp.from(Instant.now())); // 设置会话开始时间

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("session_id"); // 返回数据库生成的 session_id
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("创建聊天会话失败");
        }
        return -1;
    }

    /** 初始化整体 UI 布局 */
    protected void setupUI() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // 顶部栏（返回按钮 + 机器人头像 + 名称）
        root.setTop(createTopBar());

        // 中部聊天区
        chatBox = new VBox(8);       // 气泡间距 8px
        chatBox.setFillWidth(true);
        chatBox.setPadding(new Insets(5));

        scrollPane = new ScrollPane(chatBox);
        scrollPane.setFitToWidth(true);                       // 宽度自适应
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // 不显示横向滚动条
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // 纵向滚动条自动显示
        root.setCenter(scrollPane);

        // 底部输入栏（输入框 + 发送按钮）
        root.setBottom(createBottomBar());

        Scene scene = new Scene(root, 520, 640);
        setTitle("与" + robotName + "聊天");
        setScene(scene);

        // 窗口关闭时触发，更新数据库会话结束时间
        setOnCloseRequest(this::handleWindowClose);
    }

    /** 窗口关闭时更新会话结束时间并执行返回回调 */
    protected void handleWindowClose(WindowEvent event) {
        updateSessionEndTime(); // 更新数据库会话结束时间
        if (onBack != null) {
            onBack.run();       // 执行返回主界面回调
        }
    }

    /** 顶部栏（返回按钮 + 头像 + 名称） */
    protected HBox createTopBar() {
        backButton = new Button("返回");
        backButton.setOnAction(e -> {
            if (onBack != null) onBack.run(); // 执行返回回调
            close();                         // 关闭当前窗口
        });

        // 创建头像 ImageView
        ImageView avatar = createAvatarView(robotImage, 50, 70);

        // 显示机器人名称
        Label nameLabel = new Label(robotName);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        HBox top = new HBox(10, backButton, avatar, nameLabel);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(10));
        top.setStyle("-fx-background-color: #f0f0f0;"); // 背景颜色
        return top;
    }

    /** 底部输入栏（输入框 + 发送按钮） */
    protected HBox createBottomBar() {
        inputField = new TextField();
        inputField.setPromptText("请输入消息...");

        sendButton = new Button("发送");
        sendButton.setDefaultButton(true);       // 按 Enter 触发
        sendButton.setOnAction(e -> sendMessage()); // 点击发送按钮
        inputField.setOnAction(e -> sendMessage()); // 回车发送消息

        HBox bottom = new HBox(8, inputField, sendButton);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(10));
        HBox.setHgrow(inputField, Priority.ALWAYS); // 输入框自动扩展宽度
        return bottom;
    }

    /** 发送消息并触发机器人回复 */
    protected void sendMessage() {
        String message = inputField.getText();
        if (message == null || message.trim().isEmpty()) return; // 空消息不处理

        inputField.clear(); // 清空输入框

        // 显示并保存用户消息
        addMessage(message, true);
        saveMessageToDatabase(message, "user");

        // 异步生成机器人回复
        new Thread(() -> {
            try { Thread.sleep(300); } catch (InterruptedException ignored) {} // 模拟思考延迟

            String reply = generateRobotReply(message);
            Platform.runLater(() -> addMessage(reply, false)); // 更新 UI
            saveMessageToDatabase(reply, "robot");           // 保存到数据库
        }).start();
    }

    /** 向界面中添加一条聊天气泡 */
    protected void addMessage(String message, boolean isUser) {
        Platform.runLater(() -> {
            HBox line = new HBox(8);                  // 每条消息横向布局
            line.setAlignment(isUser ? Pos.TOP_RIGHT : Pos.TOP_LEFT);

            ImageView avatar = createAvatarView(isUser ? userImage : robotImage, 70, 95);

            Label bubble = new Label(message);        // 消息气泡文本
            bubble.setWrapText(true);
            bubble.setMaxWidth(360);                  // 气泡最大宽度

            VBox bubbleBox = new VBox(bubble);        // 气泡容器
            bubbleBox.setPadding(new Insets(8, 12, 8, 12));
            bubbleBox.setStyle(isUser
                    ? "-fx-background-color: #b4e1ff; -fx-background-radius: 12;" // 用户气泡
                    : "-fx-background-color: #f0f0f0; -fx-background-radius: 12;"); // 机器人气泡

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);   // 占位调整布局

            if (isUser) {
                line.getChildren().addAll(spacer, bubbleBox, avatar); // 用户消息靠右
            } else {
                line.getChildren().addAll(avatar, bubbleBox, spacer); // 机器人消息靠左
            }

            chatBox.getChildren().add(line);
            scrollPane.setVvalue(1.0); // 滚动到底部
        });
    }

    /** 从数据库加载历史消息 */
    protected void loadHistory(int limit) {
        if (sessionId == -1) return;

        String sql =
                "SELECT message_content, sender_type " +
                "FROM chat_messages WHERE session_id = ? " +
                "ORDER BY timestamp ASC LIMIT ?";

        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, sessionId);
                pstmt.setInt(2, limit);

                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    addMessage(rs.getString(1), "user".equals(rs.getString(2))); // 根据发送者类型显示消息
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /** 保存消息到数据库 */
    protected void saveMessageToDatabase(String message, String senderType) {
        if (sessionId == -1) return;

        String sql =
                "INSERT INTO chat_messages (session_id, sender_type, message_content, timestamp) " +
                "VALUES (?, ?, ?, ?)";

        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, sessionId);
                pstmt.setString(2, senderType);
                pstmt.setString(3, message);
                pstmt.setTimestamp(4, Timestamp.from(Instant.now())); // 当前时间
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /** 生成机器人回复（可被子类覆盖实现更智能回复） */
    protected String generateRobotReply(String userMessage) {
        String[] replies = {
                "这是一个很有趣的问题！",
                "让我想想怎么回答你...",
                "你说得很有道理。",
                "关于这个话题，我有一些想法。",
                "谢谢你的分享！"
        };
        return replies[userMessage.length() % replies.length]; // 简单策略：根据长度选择回复
    }

    /** 更新会话结束时间，用于标记聊天结束 */
    protected void updateSessionEndTime() {
        if (sessionId == -1) return;

        String sql = "UPDATE chat_sessions SET end_time = ? WHERE session_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.from(Instant.now())); // 当前时间作为结束时间
            pstmt.setInt(2, sessionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** 安全加载图片（失败返回透明占位） */
    protected Image loadImageSafe(String path) {
        try {
            return new Image(new FileInputStream(path)); // 尝试本地路径
        } catch (Exception ignored) {
            try (InputStream is = getClass().getResourceAsStream("/" + path)) {
                if (is != null) return new Image(is);    // 尝试资源路径
            } catch (Exception ignored2) {}
        }
        // 返回 1x1 透明图片占位
        return new Rectangle(1, 1, Color.TRANSPARENT).snapshot(null, null);
    }

    /** 创建头像 ImageView */
    protected ImageView createAvatarView(Image img, double w, double h) {
        ImageView iv = new ImageView(img);
        iv.setFitWidth(w);
        iv.setFitHeight(h);
        iv.setPreserveRatio(true); // 保持纵横比
        return iv;
    }
}
