import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// 机器人聊天界面的美化子类
public class RobotChatFrameDecorated extends RobotChatFrame {
//public class RobotChatFrameDecorated extends RobotChatFrame2 {
//我们后面打算让RobotChatFrameDecorated继承RobotChatFrame2。现在跑不通，先暂时不继承。
    // 选项栏面板
    private JPanel optionsPanel;
    // 标记选项栏是否显示
    private boolean isOptionsShown = false;

    // 调用父类构造方法
    public RobotChatFrameDecorated(String robotName) {
        super(robotName);
        initUI();
    }

    // 另一个构造方法
    public RobotChatFrameDecorated(String robotName, java.util.concurrent.atomic.AtomicInteger id, Runnable onBack) {
        super(robotName, id, onBack);
        initUI();
    }

    // 初始化界面的方法
    private void initUI() {
        System.out.println("↓↓↓RobotChatFrameDecorated的initUI方法正在运行ing");

        System.out.println("初始化1：美化右上角的返回按钮");
        // 美化左上角的返回按钮
        customizeBackButton();

        System.out.println("初始化2：绘制背景");
        // 聊天界面背景图片
        addBackgroundImage();

        System.out.println("初始化3：设置机器人头像的点击事件");
        // 设置机器人头像的点击事件
        setupRobotAvatarClick();
        System.out.println("↑↑↑RobotChatFrameDecorated的initUI方法运行结束");
    }

    // 美化返回按钮的方法
    private void customizeBackButton() {
        // 修改按钮的文字和样式
        backButton.setText("← 返回主界面");
        backButton.setFont(new Font("微软雅黑", Font.BOLD, 14));

        // 设置按钮的背景颜色
        backButton.setBackground(new Color(70, 130, 180));
        backButton.setForeground(Color.WHITE);

        // 设置按钮的边框
        backButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(30, 144, 255), 2),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        // 设置按钮不显示焦点框
        backButton.setFocusPainted(false);
    }

    private void addBackgroundImage(){
        System.out.println("添加背景图片ing...");
        // 获取窗口的内容面板
        Container contentPane = getContentPane();

        // 创建带背景图片的面板
        JPanel backgroundPanel = new JPanel() {
            // 重写绘制方法，添加背景图片
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                // 创建深紫色渐变背景
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(40, 20, 60),      // 深紫色
                        getWidth(), getHeight(), new Color(25, 10, 40)  // 更深的紫色
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // 设置布局
        backgroundPanel.setLayout(new BorderLayout());

        // 把原来的内容添加到背景面板中
        Component originalContent = contentPane;

        // 关键步骤1：将原内容面板设置为透明
        if (originalContent instanceof JComponent) {
            ((JComponent) originalContent).setOpaque(false);
        }

        // 关键步骤2：递归设置所有子组件透明，但保持文本组件可读性
        setTransparentRecursive(originalContent);

        // 额外步骤：确保输入框等文本区域有合适的背景色
        ensureTextComponentsReadable(originalContent);

        backgroundPanel.add(originalContent, BorderLayout.CENTER);

        // 设置窗口的内容面板为新的背景面板
        setContentPane(backgroundPanel);

        // 设置聊天面板为透明，这样可以看到背景
        if (chatPanel != null) {
            chatPanel.setOpaque(false);
            System.out.println("已将聊天面板设置为透明");
        }
    }

    // 确保文本组件有合适的背景色以便阅读
    private void ensureTextComponentsReadable(Component component) {
        if (component instanceof JTextComponent) {
            // 文本输入框等组件设置为浅色背景，深色文字
            JTextComponent textComp = (JTextComponent) component;
            textComp.setOpaque(true);
            textComp.setBackground(new Color(150, 255, 255, 230)); // 半透明白色背景
            textComp.setForeground(new Color(30, 30, 30)); // 深灰色文字
            
        }
        else if (component instanceof JButton) {
            // 按钮样式调整
            JButton button = (JButton) component;
            button.setOpaque(true);
            button.setBackground(new Color(80, 60, 120)); // 紫色系按钮
            button.setForeground(Color.WHITE); // 白色文字
        }
        else if (component instanceof JLabel) {
            // 标签文字颜色调整为浅色，以便在深色背景上显示
            JLabel label = (JLabel) component;
            label.setForeground(new Color(220, 220, 240)); // 浅紫色文字
        }
        else if (component instanceof JPanel) {
            // 检查是否是聊天气泡面板
            JPanel panel = (JPanel) component;

            // 通过背景色判断是否是聊天气泡（用户气泡或机器人气泡）
            Color bg = panel.getBackground();
            if (bg.equals(new Color(180, 230, 255)) || // 用户气泡背景色
                    bg.equals(new Color(240, 240, 240))) { // 机器人气泡背景色

                // 创建粉色到淡蓝色的渐变色边框
                Border gradientBorder = new Border() {
                    @Override
                    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        // 创建从粉色到淡蓝色的渐变
                        GradientPaint gradient = new GradientPaint(
                                x, y, new Color(255, 150, 200), // 粉色
                                x + width, y + height, new Color(150, 220, 230) // 淡蓝色
                        );

                        g2d.setPaint(gradient);
                        g2d.setStroke(new BasicStroke(5)); // 5像素宽
                        g2d.drawRect(x, y, width - 1, height - 1);

                        g2d.dispose();
                    }

                    @Override
                    public Insets getBorderInsets(Component c) {
                        return new Insets(5, 5, 5, 5); // 5像素边框
                    }

                    @Override
                    public boolean isBorderOpaque() {
                        return false;
                    }
                };

                panel.setBorder(gradientBorder);
            }
        }

        // 递归处理子组件
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                ensureTextComponentsReadable(child);
            }
        }
    }

    // 修改现有的递归透明设置方法，避免影响文本组件的可读性
    private void setTransparentRecursive(Component component) {
        if (component instanceof JComponent) {
            JComponent jcomp = (JComponent) component;

            // 跳过文本组件和按钮，这些需要保持不透明以确保可读性
            if (!(component instanceof JTextComponent) &&
                    !(component instanceof JButton) &&
                    !component.equals(chatPanel)) {
                jcomp.setOpaque(false);
            }
        }

        // 递归设置子组件
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                setTransparentRecursive(child);
            }
        }
    }

    // 设置机器人头像点击事件的方法
    private void setupRobotAvatarClick() {
        // 在窗口显示后查找机器人头像
        SwingUtilities.invokeLater(() -> {
            // 在整个窗口中查找机器人头像
            findRobotAvatar(getContentPane());
        });
    }

    // 递归查找机器人头像的方法
    private void findRobotAvatar(Container container) {
        // 遍历容器中的所有组件
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                // 判断是否是机器人头像
                if (isRobotAvatar(label)) {
                    // 设置头像的点击事件
                    setupAvatarClickEvent(label);
                    return; // 找到就返回
                }
            }

            // 如果是容器，继续递归查找
            if (comp instanceof Container) {
                findRobotAvatar((Container) comp);
            }
        }
    }

    // 判断一个标签是否是机器人头像
    private boolean isRobotAvatar(JLabel label) {
        // 通过图标判断，如果图标和robotIcon相同就是机器人头像
        if (robotIcon != null && label.getIcon() != null) {
            // 简单的判断方法：比较图标的高度
            return label.getIcon().getIconHeight() == robotIcon.getIconHeight();
        }
        return false;
    }

    // 设置头像点击事件
    private void setupAvatarClickEvent(JLabel avatarLabel) {
        // 设置鼠标手型，表示可以点击
        avatarLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 添加鼠标点击监听器
        avatarLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 切换选项栏的显示/隐藏
                toggleOptionsPanel(e.getXOnScreen(), e.getYOnScreen());
            }
        });
    }

    // 切换选项栏的显示/隐藏
    private void toggleOptionsPanel(int x, int y) {
        if (!isOptionsShown) {
            // 如果选项栏没有显示，就显示它
            showOptionsPanel(x, y);
        } else {
            // 如果选项栏已经显示，就隐藏它
            hideOptionsPanel();
        }
    }

    // 显示选项栏
    private void showOptionsPanel(int x, int y) {
        // 如果选项栏还没创建，先创建它
        if (optionsPanel == null) {
            createOptionsPanel();
        }

        // 计算选项栏的位置（在点击位置下方显示）
        int panelX = x - getLocationOnScreen().x;
        int panelY = y - getLocationOnScreen().y + 50; // 在点击位置下方50像素

        // 确保选项栏不超出窗口边界
        if (panelX + optionsPanel.getWidth() > getWidth()) {
            panelX = getWidth() - optionsPanel.getWidth() - 10;
        }

        // 设置选项栏的位置
        optionsPanel.setLocation(panelX, panelY);

        // 把选项栏添加到窗口的顶层
        getLayeredPane().add(optionsPanel, JLayeredPane.POPUP_LAYER);

        // 标记选项栏为显示状态
        isOptionsShown = true;

        // 添加窗口点击监听，用于点击外部时隐藏选项栏
        addWindowClickListener();
    }

    // 创建选项栏
    private void createOptionsPanel() {
        // 创建选项栏面板
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));

        // 设置背景颜色
        optionsPanel.setBackground(new Color(40, 40, 60));

        // 设置边框
        optionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.CYAN, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // 设置固定大小
        optionsPanel.setPreferredSize(new Dimension(150, 200));
        optionsPanel.setSize(150, 200);

        // 选项栏标题
        JLabel titleLabel = new JLabel("选项");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        optionsPanel.add(titleLabel);

        // 添加分隔线
        optionsPanel.add(Box.createVerticalStrut(10));
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.GRAY);
        optionsPanel.add(separator);
        optionsPanel.add(Box.createVerticalStrut(10));

        // 添加几个选项按钮
        String[] options = {"参数设置", "历史记录", "帮助", "关于"};

        for (String optionText : options) {
            JButton optionButton = createOptionButton(optionText);
            optionsPanel.add(optionButton);
            optionsPanel.add(Box.createVerticalStrut(5));
        }
    }

    // 创建选项按钮
    private JButton createOptionButton(String text) {
        JButton button = new JButton(text);

        // 设置按钮样式
        button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(60, 60, 80));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(130, 30));

        // 添加点击事件
        button.addActionListener(e -> {
            // 点击按钮时隐藏选项栏
            hideOptionsPanel();

            // 跳转到新页面
            openNewPage(text);
        });

        return button;
    }

    // 隐藏选项栏
    private void hideOptionsPanel() {
        if (optionsPanel != null && isOptionsShown) {
            // 移除选项栏
            getLayeredPane().remove(optionsPanel);
            isOptionsShown = false;

            // 重绘窗口
            repaint();
        }
    }

    // 添加窗口点击监听
    private void addWindowClickListener() {
        // 获取窗口的内容面板
        Container contentPane = getContentPane();

        // 添加鼠标监听器
        contentPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 如果选项栏显示中，并且点击的不是选项栏
                if (isOptionsShown && optionsPanel != null) {
                    // 获取点击位置
                    Point clickPoint = e.getPoint();

                    // 转换到选项栏的坐标系
                    Point panelPoint = SwingUtilities.convertPoint(
                            contentPane, clickPoint, getLayeredPane());

                    // 判断点击位置
                    Rectangle panelBounds = optionsPanel.getBounds();
                    if (!panelBounds.contains(panelPoint)) {
                        // 如果点击在选项栏外部，隐藏选项栏
                        hideOptionsPanel();
                    }
                }
            }
        });
    }

    // 打开新页面的方法
    private void openNewPage(String pageName) {
        // 创建新窗口
        JFrame newPage = new JFrame(pageName);
        newPage.setSize(400, 300);
        newPage.setLocationRelativeTo(this);
        // 创建内容面板
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());

        // 提示文字
        JLabel messageLabel = new JLabel("这是 " + pageName + " 页面", SwingConstants.CENTER);
        messageLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        contentPanel.add(messageLabel, BorderLayout.CENTER);

        // 关闭按钮
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> newPage.dispose());
        contentPanel.add(closeButton, BorderLayout.SOUTH);

        // 设置窗口内容
        newPage.setContentPane(contentPanel);

        // 显示窗口
        newPage.setVisible(true);
    }

    // 测试方法，和父类的测试一样，注释掉吧。反正也跑不通
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            RobotChatFrameDecorated frame = new RobotChatFrameDecorated("测试机器人");
//            frame.setVisible(true);
//        });
//    }
}
