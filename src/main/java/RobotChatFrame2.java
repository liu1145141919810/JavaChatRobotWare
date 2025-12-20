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
 * JavaFX 版本的 RobotChatFrame2，基于 RobotChatFrame。
 */
public class RobotChatFrame2 extends RobotChatFrame {
    protected Button restartButton;
    protected TestConnect testConnect;

    public RobotChatFrame2(String robotName) {
        super(robotName);
        testConnect = new TestConnect(param);
    }

    public RobotChatFrame2(String robotName, AtomicInteger id, Runnable onBack) {
        super(robotName, id, onBack);
        testConnect = new TestConnect(param);
    }

    @Override
    protected String generateRobotReply(String userMessage) {
        try{
        return testConnect != null ? testConnect.output(userMessage) : super.generateRobotReply(userMessage);
        } catch (Exception e){
           return super.generateRobotReply(userMessage);
        }
    }

    @Override
    protected HBox createTopBar() {
        // 头像 + 名称
        ImageView avatar = createAvatarView(robotImage, 50, 70);
        Label nameLabel = new Label(robotName);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        backButton = new Button("返回");
        backButton.setOnAction(e -> {
            if (onBack != null) {
                try { onBack.run(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            close();
        });

        restartButton = new Button("重启");
        restartButton.setOnAction(e -> {
            // 清空该机器人会话记录
            new Thread(() -> {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM chat_sessions WHERE robot_id = ?")) {
                    pstmt.setInt(1, param);
                    int rows = pstmt.executeUpdate();
                    System.out.println("重启清理完成，删除行: " + rows);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    System.err.println("重启清理失败: " + ex.getMessage());
                }
            }).start();

            // 回到上层（由 MainApp 决定如何重新进入）
            if (onBack != null) {
                try { onBack.run(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            close();
        });

        HBox buttons = new HBox(8, backButton, restartButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(10, buttons, spacer, avatar, nameLabel);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(10));
        top.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: transparent transparent #ddd transparent;");
        return top;
    }
}