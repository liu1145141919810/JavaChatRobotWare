import javax.swing.*;
import com.ark.example.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.desktoprobot.util.DatabaseConnection;
    public class RobotChatFrame2 extends RobotChatFrame {
        protected JButton restarButton;//----------------------------------
        protected TestConnect testConnect;

        public RobotChatFrame2(String robotName) {
               super(robotName);
        }
        public RobotChatFrame2(String robotName,AtomicInteger id,Runnable onBack) {
            super(robotName, id, onBack);
            testConnect = new TestConnect(param);
        }

        @Override
        protected String generateRobotReply(String userMessage) {
        // 这里可以调用您的AI服务或使用简单的规则生成回复
        return testConnect.output(userMessage);
    }

        @Override
        protected JPanel createTopPanel() {
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setBackground(new Color(240, 240, 240));
            topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            // 返回按钮
            restarButton= new JButton("重启!!");
            backButton = new JButton("返回");
            restarButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 优先调用 onBack，要求调用端（MainApp）在回调中使用 Platform.runLater 显示 JavaFX 窗口
                    if (onBack != null) {
                        try {
                            String sql = "DELETE FROM chat_sessions WHERE robot_id = ?";
                            testConnect = null;
                            new Thread(()->{
                                try (Connection conn = DatabaseConnection.getConnection();
                                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                                pstmt.setInt(1,param);
                                int rowsDeleted = pstmt.executeUpdate();
                                System.out.println("新建重启成功，删除内容: " + rowsDeleted);
                                } catch (SQLException excep) {
                                excep.printStackTrace();
                                    System.err.println("新建重启失败: " + excep.getMessage());
                                }
                            }).start();
                            System.out.println("重启按钮被点击");
                            onBack.run();
                        } catch (Exception ex) {
                            System.out.println("重启按钮回调异常: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                    // 隐藏并释放 Swing 窗口，但不要调用 System.exit
                    SwingUtilities.invokeLater(() -> {
                        //数据库内容释放
                        System.out.println("***");
                        setVisible(false);
                        dispose();
                    });
                }
            });
            backButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 优先调用 onBack，要求调用端（MainApp）在回调中使用 Platform.runLater 显示 JavaFX 窗口
                    if (onBack != null) {
                        try {
                            onBack.run();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    // 隐藏并释放 Swing 窗口，但不要调用 System.exit
                    SwingUtilities.invokeLater(() -> {
                        setVisible(false);
                        dispose();
                    });
                }
            });
            
            // 机器人名称
            JLabel robotNameLabel = new JLabel(robotName);
            robotNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            robotNameLabel.setHorizontalAlignment(SwingConstants.CENTER);

            ImageIcon newrobotIcon = resizeIcon(robotIcon, 50, 70);
            JLabel robotIconLabel = new JLabel(newrobotIcon);
            
            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            leftPanel.add(backButton);
            leftPanel.add(restarButton);

            // 添加到 BorderLayout
            topPanel.add(leftPanel, BorderLayout.WEST);
            topPanel.add(robotNameLabel, BorderLayout.CENTER);
            topPanel.add(robotIconLabel, BorderLayout.EAST);

            return topPanel;
        }
    }