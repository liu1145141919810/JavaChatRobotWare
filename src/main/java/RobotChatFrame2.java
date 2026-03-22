import com.ark.example.TestConnect;
import com.desktoprobot.util.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * JavaFX 版本的 RobotChatFrame2，继承自 RobotChatFrame
 * 功能增强：增加“重启机器人会话”按钮，并使用 TestConnect 生成智能回复
 */
public class RobotChatFrame2 extends RobotChatFrame {
    /** 重启按钮，用于清空机器人会话并重新开始 */
    protected Button restartButton;
    /** 测试机器人连接对象，用于生成智能回复 */
    protected TestConnect testConnect;
    /**
     * 构造函数（仅机器人名称）
     * @param robotName 机器人名称
     */
    public RobotChatFrame2(String robotName) {
        super(robotName);           // 调用父类构造函数初始化界面
        testConnect = new TestConnect(param); // 初始化机器人连接对象
    }
    /**
     * 构造函数（带机器人参数ID和返回回调）
     * @param robotName 机器人名称
     * @param id        机器人参数ID
     * @param onBack    返回主界面回调
     */
    public RobotChatFrame2(String robotName, AtomicInteger id, Runnable onBack) {
        super(robotName, id, onBack);          // 调用父类构造函数初始化界面和数据库会话
        testConnect = new TestConnect(param);  // 初始化机器人连接对象
    }
    /**
     * 重写父类 generateRobotReply 方法，使用 TestConnect 生成智能回复
     * @param userMessage 用户输入消息
     * @return 机器人回复文本
     */
    @Override
    protected String generateRobotReply(String userMessage) {
        try {
            // 如果 testConnect 可用，则调用 output 方法生成回复
            return testConnect != null ? testConnect.output(userMessage) : super.generateRobotReply(userMessage);
        } catch (Exception e) {
            // 出现异常时回退使用父类默认回复
            return super.generateRobotReply(userMessage);
        }
    }
    /**
     * 重写父类 createTopBar 方法，增加“重启”按钮
     * @return 顶部 HBox
     */
    @Override
    protected HBox createTopBar() {
        // 创建机器人头像 ImageView
        ImageView avatar = createAvatarView(robotImage, 50, 70);
        // 创建机器人名称 Label
        Label nameLabel = new Label(robotName);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        // 创建返回按钮
        backButton = new Button("返回");
        backButton.setOnAction(e -> {
            if (onBack != null) {
                try { 
                    onBack.run();  // 执行返回主界面回调
                } catch (Exception ex) { 
                    ex.printStackTrace(); 
                }
            }
            close(); // 关闭当前聊天窗口
        });
        // 创建重启按钮
        restartButton = new Button("重启");
        restartButton.setOnAction(e -> {
            // 异步清理该机器人在数据库中的所有聊天会话
            new Thread(() -> {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM chat_sessions WHERE robot_id = ?")) {

                    pstmt.setInt(1, param);      // 设置机器人参数ID
                    int rows = pstmt.executeUpdate();
                    System.out.println("重启清理完成，删除行: " + rows); // 打印删除记录数
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    System.err.println("重启清理失败: " + ex.getMessage());
                }
            }).start();
            // 执行返回主界面回调，由 MainApp 决定如何重新进入聊天
            if (onBack != null) {
                try { onBack.run(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            close(); // 关闭当前聊天窗口
        });
        // 按钮容器（返回 + 重启）
        HBox buttons = new HBox(8, backButton, restartButton);
        buttons.setAlignment(Pos.CENTER_LEFT);
        // 空白占位，自动撑开布局
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        // 顶部整体 HBox（按钮 + 空白 + 头像 + 名称）
        HBox top = new HBox(10, buttons, spacer, avatar, nameLabel);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(10));
        top.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: transparent transparent #ddd transparent;");
        return top;
    }
}
