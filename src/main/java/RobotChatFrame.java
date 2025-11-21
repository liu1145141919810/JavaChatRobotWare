import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import com.desktoprobot.util.DatabaseConnection;

public class RobotChatFrame extends JFrame {
    protected String robotName;
    protected JPanel chatPanel;
    protected JTextField inputField;
    protected JButton sendButton;
    protected JButton backButton;
    protected Runnable onBack;
    protected int param; // 机器人参数 ID
    protected int sessionId; // 聊天会话ID，用于关联同一次聊天的多条记录
    
    // 头像
    protected ImageIcon userIcon;
    protected ImageIcon robotIcon;
    
    public RobotChatFrame(String robotName) {
        this.robotName = robotName;
        setupUI();
    }

    // 新构造器：接受返回回调（在 Swing 线程中调用）
    public RobotChatFrame(String robotName,AtomicInteger id,Runnable onBack) {
        this.robotName = robotName;
        this.onBack = onBack;
        this.param=id.get();
        robotIcon = new ImageIcon("Robot_App/Robot"+id.get()+"/static/figure.jpg");
        userIcon = new ImageIcon("Images/user.jpg");
        String filepath="Images/user.jpg";
        File file=new File(filepath);
        System.out.println("Robot_App/Robot"+id.get()+"/static/figure.jpg");
        System.out.println("Absolute path: " + file.getAbsolutePath());

        // 初始化数据库会话
        this.sessionId = createChatSession();

        setupUI();

        // 加载历史消息
        loadHistory(50);
    }

    //创建新的聊天会话并返回会话ID
    protected int createChatSession() {
        String sql = "INSERT INTO chat_sessions (robot_id, robot_name, start_time) VALUES (?, ?, ?) RETURNING session_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            //根据chat_sessions (robot_id, robot_name, start_time)的格式来一个个读取数据到当前会话窗口
            pstmt.setInt(1, this.param);
            pstmt.setString(2, this.robotName);
            pstmt.setTimestamp(3, new Timestamp(new Date().getTime()));

            var rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("session_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // 如果创建会话失败，返回-1，后续保存消息时会跳过数据库操作
            System.err.println("创建聊天会话失败: " + e.getMessage());
        }
        return -1;
    }

    protected void setupUI() {
        // 设置窗口
        setTitle("与" + robotName + "聊天");
        // don't exit the JVM when the Swing window is closed — dispose so JavaFX can be shown again
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);
        
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // 顶部面板 - 只显示机器人名称
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // 中间面板 - 聊天内容
        JPanel middlePanel = createMiddlePanel();
        mainPanel.add(middlePanel, BorderLayout.CENTER);
        
        // 底部面板 - 输入区域
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);

        // 窗口关闭时也调用 onBack，确保可以返回到 JavaFX 界面而不是终止程序
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // 更新会话结束时间
                updateSessionEndTime();


                if (onBack != null) {
                    try {
                        onBack.run();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                // 保证在 EDT 上释放资源
                SwingUtilities.invokeLater(() -> dispose());
            }
        });
    }
    
    // 创建顶部面板
    protected JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(240, 240, 240));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 返回按钮
        backButton = new JButton("返回");
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
        
        // 添加到 BorderLayout
        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(robotNameLabel, BorderLayout.CENTER);
        topPanel.add(robotIconLabel, BorderLayout.EAST);

        return topPanel;
    }
    
    // 创建底部输入面板
    protected JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 输入区域
        JPanel inputArea = new JPanel(new BorderLayout(5, 0));
        
        // 输入框
        inputField = new JTextField();
        inputField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        // 发送按钮
        sendButton = new JButton("发送");
        sendButton.setBackground(new Color(0, 120, 215));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        // 发送按钮点击事件
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        // 输入框回车发送
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        inputArea.add(inputField, BorderLayout.CENTER);
        inputArea.add(sendButton, BorderLayout.EAST);
        
        bottomPanel.add(inputArea, BorderLayout.CENTER);
        
        return bottomPanel;
    }
    
    // 发送消息
    protected void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            // 添加用户消息（右侧）
            addMessage(message, true);

            // 保存用户消息到数据库
            saveMessageToDatabase(message, "user");

            // 清空输入框
            inputField.setText("");
            
            // 机器人回复（左侧）
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500); // 假装机器人思考了一下

                        // 生成机器人回复（这里可以替换为实际的AI回复逻辑）
                        String robotReply = generateRobotReply(message);
                        addMessage(robotReply, false); // 机器人回复空白消息

                        // 保存机器人回复到数据库
                        saveMessageToDatabase(robotReply, "robot");

                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    protected JPanel createMiddlePanel() {
        JPanel middlePanel = new JPanel(new BorderLayout());
        
        // 聊天面板 - 使用垂直盒子布局（从上到下排列）
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(Color.WHITE);
        chatPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        
        // 不使用垂直 glue，这样消息按顺序自上而下排列并能滚动
        // chatPanel.add(Box.createVerticalGlue());
        
        // 添加滚动条，允许垂直滚动
        JScrollPane scrollPane = new JScrollPane(chatPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        middlePanel.add(scrollPane, BorderLayout.CENTER);
        
        return middlePanel;
    }
// ...existing code...

    // 添加消息到聊天面板
    protected void addMessage(String message, boolean isUser) {
        // 创建单条消息的整体面板
        JPanel messageContainer = new JPanel();
        messageContainer.setLayout(new BoxLayout(messageContainer, BoxLayout.X_AXIS));
        messageContainer.setBackground(Color.WHITE);
        messageContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // 头像
        ImageIcon avatar = resizeIcon(isUser ? userIcon : robotIcon, 60, 80);
        JLabel avatarLabel = new JLabel(avatar);
        
        // 消息文本区：根据文字自动换行并测量首选大小
        JTextArea messageText = new JTextArea(message);
        messageText.setEditable(false);
        messageText.setLineWrap(true);
        messageText.setWrapStyleWord(true);
        messageText.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        messageText.setOpaque(false);
        
        // 计算最大气泡宽度（相对于窗口，使用固定上限以保证换行）
        int maxBubbleWidth = 320; // 可根据需要调整或基于窗口宽度计算
        int paddingH = 20;
        int paddingV = 12;
        
        // 为文本区设定一个宽度约束以便正确计算首选高度
        messageText.setSize(maxBubbleWidth, Short.MAX_VALUE);
        Dimension pref = messageText.getPreferredSize();
        messageText.setPreferredSize(pref);
        
        // 消息气泡面板
        JPanel bubblePanel = new JPanel(new BorderLayout());
        bubblePanel.setBackground(isUser ? new Color(180, 230, 255) : new Color(240, 240, 240));
        bubblePanel.setBorder(BorderFactory.createEmptyBorder(paddingV/2, paddingH/2, paddingV/2, paddingH/2));
        bubblePanel.add(messageText, BorderLayout.CENTER);
        bubblePanel.setMaximumSize(new Dimension(pref.width + paddingH, pref.height + paddingV));
        bubblePanel.setPreferredSize(new Dimension(pref.width + paddingH, pref.height + paddingV));
        
        // 设置消息在左侧还是右侧
        if (isUser) {
            // 用户消息在右侧：加入弹性空间使其靠右
            messageContainer.add(Box.createHorizontalGlue());
            messageContainer.add(bubblePanel);
            messageContainer.add(Box.createHorizontalStrut(8));
            messageContainer.add(avatarLabel);
        } else {
            // 机器人消息在左侧
            messageContainer.add(avatarLabel);
            messageContainer.add(Box.createHorizontalStrut(8));
            messageContainer.add(bubblePanel);
            messageContainer.add(Box.createHorizontalGlue());
        }
        
        // 添加垂直间距，让消息之间有空隙
        chatPanel.add(Box.createVerticalStrut(8));
        chatPanel.add(messageContainer);
        
        // 刷新界面并在 EDT 中滚动到底部
        chatPanel.revalidate();
        chatPanel.repaint();
        
        SwingUtilities.invokeLater(() -> {
            Container p = chatPanel.getParent();
            if (p instanceof JViewport) {
                JScrollPane sp = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, chatPanel);
                if (sp != null) {
                    JScrollBar vertical = sp.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                }
            } else {
                // 兜底获取父级滚动条
                JScrollPane sp = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, chatPanel);
                if (sp != null) {
                    JScrollBar vertical = sp.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                }
            }
        });
    }
    // 调整图标大小
    protected ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }

    //从数据库中加载历史消息
    protected void loadHistory(int limit){
        //加载最大消息数为limit的消息
        System.out.println("加载历史消息，会话ID: " + this.sessionId + ", 限制: " + limit);

        if (this.sessionId == -1) {
            System.out.println("会话ID无效，跳过加载历史消息");
            return;
        }

        //按时间正序排列，这样先显示旧消息
        // 联表查询，通过session_id关联，获取指定机器人的所有历史消息
        String sql = "SELECT cm.message_content, cm.sender_type " +
                "FROM chat_messages cm " +
                "JOIN chat_sessions cs ON cm.session_id = cs.session_id " +
                "WHERE cs.robot_id = ? " +
                "ORDER BY cm.timestamp ASC LIMIT ?";
        //System.out.println("执行的SQL: " + sql);


        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            // 设置参数，配合上面的？占位符来补全sql命令语句
            pstmt.setInt(1, this.param);
            pstmt.setInt(2, limit);

            ResultSet history = pstmt.executeQuery();//接受聊天历史
            int count = 0;

            // 遍历结果集,使用addmessage()
            while (history.next()) {
                count++;
                // 按列名获取数据
                String content = history.getString("message_content");
                String senderType = history.getString("sender_type");

                //到这里的时候，其实已经保证id是当前对话id了。
                if ("user".equals(senderType)){
                    addMessage(content,true);
                }
                else if ("robot".equals(senderType)){
                    addMessage(content,false);
                }

            }
            System.out.println("成功加载 " + count + " 条历史消息");


        } catch (SQLException e) {
            e.printStackTrace();
             System.err.println("加载历史消息失败: " + e.getMessage());
        }
        System.out.println("=== 历史消息加载完成 ===");
    }



    //保存消息到数据库
    protected void saveMessageToDatabase(String message, String senderType) {
        if (sessionId == -1) return; // 如果会话创建失败，跳过保存

        String sql = "INSERT INTO chat_messages (session_id, sender_type, message_content, timestamp) VALUES (?, ?, ?, ?)";

        new Thread(() -> { // 在新线程中执行数据库操作，避免阻塞UI
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, sessionId);
                pstmt.setString(2, senderType);
                pstmt.setString(3, message);
                pstmt.setTimestamp(4, new Timestamp(new Date().getTime()));
                pstmt.executeUpdate();

                System.out.println("消息保存成功: " + senderType + " - " + message);

            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("保存消息失败: " + e.getMessage());
            }
        }).start();
    }


    //生成机器人回复（示例逻辑，您可以替换为实际的AI对话逻辑）
    protected String generateRobotReply(String userMessage) {
        // 这里可以调用您的AI服务或使用简单的规则生成回复
        String[] replies = {
                "这是一个很有趣的问题！",
                "让我想想怎么回答你...",
                "我明白了，你能告诉我更多吗？",
                "关于" + userMessage + "，我的看法是...",
                "谢谢你的分享！"
        };

        // 简单示例：根据消息长度选择回复
        int index = userMessage.length() % replies.length;
        return replies[index];
    }

    //更新会话结束时间

    protected void updateSessionEndTime() {
        if (sessionId == -1) return;

        String sql = "UPDATE chat_sessions SET end_time = ? WHERE session_id = ?";

        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setTimestamp(1, new Timestamp(new Date().getTime()));
                pstmt.setInt(2, sessionId);
                pstmt.executeUpdate();

                System.out.println("会话结束时间已更新");

            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("更新会话结束时间失败: " + e.getMessage());
            }
        }).start();
    }


    // 测试
    public static void main(String[] args) {
        // 设置界面风格
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                RobotChatFrame chatFrame = new RobotChatFrame("小助手");
                chatFrame.setVisible(true);
            }
        });
    }
}
