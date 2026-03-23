import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import com.desktoprobot.util.DatabaseConnection;

/**
 * JavaFX 版的美化聊天界面 RobotChatFrame3，继承自 RobotChatFrame2
 * 功能增强：
 *  1. 渐变背景
 *  2. 返回按钮与重启按钮美化
 *  3. 机器人头像点击弹出选项菜单，可打开小游戏/帮助/关于页面
 *  4. 聊天气泡增加渐变边框效果
 */
public class RobotChatFrame3 extends RobotChatFrame2 {

    /** 机器人头像的右键菜单 */
    private ContextMenu avatarMenu;

    /** 构造函数，仅机器人名称 */
    public RobotChatFrame3(String robotName) {
        super(robotName);   // 调用父类构造函数
        decorateUI();       // 对界面进行美化处理
    }

    /** 构造函数，带机器人参数ID和返回回调 */
    public RobotChatFrame3(String robotName, AtomicInteger id, Runnable onBack) {
        super(robotName, id, onBack);  // 调用父类构造函数
        decorateUI();                  // 对界面进行美化处理
    }

    /** 对 UI 进行装饰，包括背景、头像菜单、输入框和滚动区样式 */
    private void decorateUI() {
        applyGradientBackground();     // 设置渐变背景
        avatarMenu = buildAvatarMenu(); // 构建头像菜单
        styleInputsAndScroll();        // 美化输入框、发送按钮和滚动区域
    }

    /** 应用渐变背景，如果根节点不是 BorderPane，则用 StackPane 包装 */
    private void applyGradientBackground() {
        if (getScene() == null) return;
        if (getScene().getRoot() instanceof BorderPane) {
            BorderPane root = (BorderPane) getScene().getRoot();
            // 直接给根节点设置渐变背景
            root.setStyle("-fx-background-color: linear-gradient(to bottom right, #28143d, #190a2d);");
        } else {
            // 若根节点不是 BorderPane，则包装一层 StackPane 保留原布局
            StackPane wrapper = new StackPane(getScene().getRoot());
            wrapper.setStyle("-fx-background-color: linear-gradient(to bottom right, #28143d, #190a2d);");
            Scene newScene = new Scene(wrapper, getScene().getWidth(), getScene().getHeight());
            setScene(newScene);
        }
    }

    /** 构建机器人头像右键菜单，点击弹出小游戏、帮助、关于选项 */
    private ContextMenu buildAvatarMenu() {
        MenuItem params = new MenuItem("小游戏，下棋");
        MenuItem history = new MenuItem("小游戏，扔骰子");
        MenuItem help = new MenuItem("帮助");
        MenuItem about = new MenuItem("关于");

        // 点击不同菜单项时打开新窗口
        params.setOnAction(e -> openNewPage("小游戏，下棋"));
        history.setOnAction(e -> openNewPage("小游戏，扔骰子"));
        help.setOnAction(e -> openNewPage("帮助"));
        about.setOnAction(e -> openNewPage("关于"));

        return new ContextMenu(params, history, help, about);
    }

    /**
     * 重写顶部栏布局，增加美化效果和头像菜单功能
     * @return 顶部 HBox
     */
    @Override
    protected HBox createTopBar() {
        // 返回按钮美化
        backButton = new Button("← 返回主界面");
        backButton.setStyle(
                "-fx-background-color: linear-gradient(#3c6ca6, #2f5c93); " +
                "-fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 8 14; -fx-border-color: #1e90ff; -fx-border-width: 2; " +
                "-fx-background-radius: 6; -fx-border-radius: 6;"
        );
        backButton.setOnAction(e -> {
            if (onBack != null) {
                try { onBack.run(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            close(); // 关闭当前窗口
        });

        // 机器人头像，点击弹出菜单
        ImageView avatar = createAvatarView(robotImage, 50, 70);
        avatar.setOnMouseClicked(e -> {
            if (avatarMenu == null) avatarMenu = buildAvatarMenu();
            if (avatarMenu.isShowing()) avatarMenu.hide();
            else avatarMenu.show(avatar, e.getScreenX(), e.getScreenY());
        });

        // 重启按钮美化
        restartButton = new Button("重启");
        restartButton.setStyle(
                "-fx-background-color: linear-gradient(#3c6ca6, #2f5c93); " +
                "-fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 8 14; -fx-border-color: #1e90ff; -fx-border-width: 2; " +
                "-fx-background-radius: 6; -fx-border-radius: 6;"
        );
        restartButton.setOnAction(e -> {
            // 异步删除机器人在数据库中的聊天会话
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

            // 回到上层
            if (onBack != null) {
                try { onBack.run(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            close();
        });

        // 名称标签美化
        Label nameLabel = new Label(robotName);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f2f2f2;");

        // 占位器，让按钮和头像/名称分开对齐
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttons = new HBox(8, backButton, restartButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        HBox top = new HBox(10, buttons, spacer, avatar, nameLabel);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(10));
        top.setStyle("-fx-background-color: rgba(0,0,0,0.25); " +
                     "-fx-border-color: transparent transparent #444 transparent; " +
                     "-fx-border-width: 0 0 1 0;");
        return top;
    }

    /**
     * 重写聊天气泡显示方法，增加渐变边框和头像菜单功能
     * @param message 消息文本
     * @param isUser  是否是用户消息
     */
    @Override
    protected void addMessage(String message, boolean isUser) {
        javafx.application.Platform.runLater(() -> {
            HBox line = new HBox(8);
            line.setAlignment(isUser ? Pos.TOP_RIGHT : Pos.TOP_LEFT);

            // 创建头像，如果是机器人消息，头像可弹出菜单
            ImageView avatar = createAvatarView(isUser ? userImage : robotImage, 70, 95);
            if (!isUser) {
                avatar.setOnMouseClicked(e -> {
                    if (avatarMenu == null) avatarMenu = buildAvatarMenu();
                    if (avatarMenu.isShowing()) avatarMenu.hide();
                    else avatarMenu.show(avatar, e.getScreenX(), e.getScreenY());
                });
            }

            // 创建消息气泡
            Label bubble = new Label(message);
            bubble.setWrapText(true);
            bubble.setMaxWidth(360);
            bubble.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

            VBox bubbleBox = new VBox(bubble);
            bubbleBox.setPadding(new Insets(10, 14, 10, 14));
            bubbleBox.setMaxWidth(380);
            String bg = isUser ? "rgba(180,230,255,0.9)" : "rgba(240,240,240,0.9)";
            bubbleBox.setStyle(
                    "-fx-background-color: " + bg + ";" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-color: linear-gradient(#ff96c8, #96dceb);" +
                    "-fx-border-radius: 12;" +
                    "-fx-border-width: 3;"
            );

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            if (isUser) {
                line.getChildren().addAll(spacer, bubbleBox, avatar);
            } else {
                line.getChildren().addAll(avatar, bubbleBox, spacer);
            }

            chatBox.getChildren().add(line);
            scrollPane.layout();
            scrollPane.setVvalue(1.0); // 滚动到底部
        });
    }

    /** 美化输入框、发送按钮和滚动区域 */
    private void styleInputsAndScroll() {
        if (inputField != null) {
            inputField.setStyle(
                    "-fx-background-color: rgba(150,255,255,0.9); " +
                    "-fx-text-fill: #1e1e1e; -fx-background-radius: 8; -fx-border-radius: 8; " +
                    "-fx-prompt-text-fill: #555;"
            );
        }
        if (sendButton != null) {
            sendButton.setStyle(
                    "-fx-background-color: linear-gradient(#504080, #3d3269); " +
                    "-fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 14; " +
                    "-fx-border-color: transparent; -fx-font-weight: bold;"
            );
        }
        if (scrollPane != null) {
            scrollPane.setStyle(
                    "-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;"
            );
            if (chatBox != null) chatBox.setStyle("-fx-background-color: transparent;");
        }
    }

    /** 打开新的页面窗口（小游戏/帮助/关于），如果没有实现则显示占位信息 */
    private void openNewPage(String pageName) {
        Stage dialog = new Stage();
        dialog.initOwner(this);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(pageName);

        if (pageName.equals("小游戏，下棋")) {
            RobotChessGame chessGame = new RobotChessGame();
            dialog.setScene(new Scene(chessGame.getRoot(), 400, 450));
            dialog.show();
            return;
        } else if (pageName.equals("小游戏，扔骰子")) {
            DiceRollingGame diceGame = new DiceRollingGame();
            dialog.setScene(new Scene(diceGame.getRoot(), 300, 400));
            dialog.show();
            return;
        }

        // 占位页面（未实现的帮助或关于页面）
        Label message = new Label("这是 " + pageName + " 页面");
        message.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button close = new Button("关闭");
        close.setOnAction(e -> dialog.close());

        VBox box = new VBox(16, message, close);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));

        dialog.setScene(new Scene(box, 360, 240));
        dialog.show();
    }
}
